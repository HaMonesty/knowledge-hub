package com.knowledgehub.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {

                if(expirationSeconds<=0){
                    throw new IllegalArgumentException("JWT有效期必须大于0");
                }

                this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
                this.issuer = issuer;
                this.expirationSeconds = expirationSeconds;

            }
            public String generateToken(Long userId){
                Instant now = Instant.now();

                return Jwts.builder()
                        .subject(userId.toString())
                        .issuer(issuer)
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                        .signWith(key,Jwts.SIG.HS256)
                        .compact();
            }

            public long getExpirationSeconds(){
                return expirationSeconds;

            }
    public Long parseUserId(String token) {
        // 验证签名、签发者；存在过期时间时，库会自动检查是否过期
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // 我们签发的令牌必须包含过期时间和用户身份
        if (claims.getExpiration() == null) {
            throw new JwtException("令牌缺少过期时间");
        }

        String subject = claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new JwtException("令牌缺少用户身份");
        }

        try {
            long userId = Long.parseLong(subject);

            if (userId <= 0) {
                throw new JwtException("令牌中的用户ID不合法");
            }

            return userId;
        } catch (NumberFormatException ex) {
            throw new JwtException("令牌中的用户ID格式错误", ex);
        }
    }


}
