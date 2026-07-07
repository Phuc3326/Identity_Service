package com.hufu.identity_service.service;

import com.hufu.identity_service.dto.request.AuthenticationRequest;
import com.hufu.identity_service.dto.request.IntrospectRequest;
import com.hufu.identity_service.dto.request.LogoutRequest;
import com.hufu.identity_service.dto.request.RefreshRequest;
import com.hufu.identity_service.dto.response.AuthenticationResponse;
import com.hufu.identity_service.dto.response.IntrospectResponse;
import com.hufu.identity_service.entity.InvalidatedToken;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.exception.ErrorCode;
import com.hufu.identity_service.repository.InvalidatedTokenRepository;
import com.hufu.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;

    @NonFinal
    @Value("${jwt.expirationTime}")
    int expirationTime;

    @NonFinal
    @Value("${jwt.refreshableTime}")
    int refreshableTime;

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws JOSEException {
        // Authenticate
        User user =
                userRepository
                        .findByUsername(request.getUsername())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Create token
        String token = generateToken(user);
        return AuthenticationResponse.builder().token(token).build();
    }

    private String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getUsername())
                        .issuer("hufu.com")
                        .issueTime(new Date())
                        .expirationTime(
                                new Date(
                                        Instant.now()
                                                .plus(expirationTime, ChronoUnit.SECONDS)
                                                .toEpochMilli()))
                        .jwtID(java.util.UUID.randomUUID().toString())
                        .claim("scope", buildScope(user))
                        .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner(signerKey.getBytes()));

        return jwsObject.serialize();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles()
                    .forEach(
                            role -> {
                                stringJoiner.add("ROLE_" + role.getName());
                                role.getPermissions()
                                        .forEach(
                                                permission ->
                                                        stringJoiner.add(permission.getName()));
                            });
        }

        return stringJoiner.toString();
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());

        if (isNotValidSignature(signedJWT)) {
            return IntrospectResponse.builder().valid(false).build();
        }

        if (isExpired(signedJWT.getJWTClaimsSet().getExpirationTime())) {
            return IntrospectResponse.builder().valid(false).build();
        }

        if (isLoggedOut(signedJWT.getJWTClaimsSet().getJWTID())) {
            return IntrospectResponse.builder().valid(false).build();
        }

        return IntrospectResponse.builder().valid(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());

        if (isNotValidSignature(signedJWT)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Date refreshExpiration =
                new Date(
                        signedJWT
                                .getJWTClaimsSet()
                                .getIssueTime()
                                .toInstant()
                                .plus(refreshableTime, ChronoUnit.SECONDS)
                                .toEpochMilli());
        if (isExpired(refreshExpiration)) {
            return;
        }

        if (isLoggedOut(signedJWT.getJWTClaimsSet().getJWTID())) {
            return;
        }

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(signedJWT.getJWTClaimsSet().getJWTID())
                        .expiry(refreshExpiration)
                        .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());

        if (isNotValidSignature(signedJWT)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Date refreshExpiration =
                new Date(
                        signedJWT
                                .getJWTClaimsSet()
                                .getIssueTime()
                                .toInstant()
                                .plus(refreshableTime, ChronoUnit.SECONDS)
                                .toEpochMilli());
        if (isExpired(refreshExpiration)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (isLoggedOut(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(signedJWT.getJWTClaimsSet().getJWTID())
                        .expiry(refreshExpiration)
                        .build();
        invalidatedTokenRepository.save(invalidatedToken);

        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String token = generateToken(user);
        return AuthenticationResponse.builder().token(token).build();
    }

    private boolean isNotValidSignature(SignedJWT signedJWT) throws JOSEException {
        JWSVerifier jwsVerifier = new MACVerifier(signerKey.getBytes());
        return !signedJWT.verify(jwsVerifier);
    }

    private boolean isExpired(Date expiryTime) {
        return expiryTime.before(new Date());
    }

    public boolean isLoggedOut(String id) {
        return invalidatedTokenRepository.existsById(id);
    }
}
