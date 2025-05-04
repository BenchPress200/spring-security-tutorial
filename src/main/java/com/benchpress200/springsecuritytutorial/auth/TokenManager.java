package com.benchpress200.springsecuritytutorial.auth;

import io.jsonwebtoken.ExpiredJwtException;
import java.net.URLDecoder;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class TokenManager {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_CLAIM_KEY = "userId";
    private static final String USER_ROLE_CLAIM_KEY = "role";
    private final String secret = "photiqueasdqwdqdczncxcuoaencqkdjsa";
    private final long EXPIRED_TIME = 3_600_000;
    private final SecretKey secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );

    /*
    * 어세스 토큰 발급
     */
    public String issueToken(
            final long userId,
            final String role
    ) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .claim(USER_ID_CLAIM_KEY, userId)
                .claim(USER_ROLE_CLAIM_KEY, role)
                .issuedAt(new Date(now))
                .signWith(secretKey)
                .expiration(new Date(now + EXPIRED_TIME))
                .compact();

        return URLEncoder.encode(TOKEN_PREFIX + accessToken, StandardCharsets.UTF_8);
    }

    /*
    * 어세스 토큰 만료 여부 체크
     */
    public boolean isExpired(String token) {
        token = URLDecoder.decode(token, StandardCharsets.UTF_8);

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());

        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    /*
    * 어세스 토큰 유저 아이디 추출
     */
    public long getUserId(String token) {
        token = URLDecoder.decode(token, StandardCharsets.UTF_8);

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(USER_ID_CLAIM_KEY, Long.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get(USER_ID_CLAIM_KEY, Long.class);
        }
    }

    /*
    * 어세스 토큰 역할 추출
     */
    public String getRole(String token) {
        token = URLDecoder.decode(token, StandardCharsets.UTF_8);

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(USER_ROLE_CLAIM_KEY, String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get(USER_ROLE_CLAIM_KEY, String.class);
        }
    }

}
