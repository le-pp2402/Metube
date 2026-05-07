package com.phatpl.metube._services.identity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phatpl.metube._dtos.response.UserResponse;
import com.phatpl.metube._exceptions.ExpiredException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class JWTService {
    @Value("${SECRET_KEY}")
    private String secretKey;
    @Value("${EXPIRATION_TIME}")
    private Long EXPIRATION_TIME;


    public String createToken(UserResponse user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("learnvocabulary")
                .issueTime(new Date(System.currentTimeMillis()))
                .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("data", user)
                .claim("scope", getScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e.getMessage());
        }
        return jwsObject.serialize();
    }

    public String getScope(UserResponse user) {
        return (user.getIsAdmin() ? "ADMIN" : "USER");
    }

    public Boolean isExpired(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date()));
    }

    public JWTClaimsSet getAllClaims(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet();
    }

    public String getUsername(String token) {
        try {
            return getAllClaims(token).getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Boolean verifyToken(String token) {
        try {
            JWSVerifier jwsVerifier = new MACVerifier(secretKey.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (isExpired(token)) {
                throw new ExpiredException("token");
            }
            return signedJWT.verify(jwsVerifier);
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}


