package fr.em;

import io.smallrye.jwt.build.Jwt;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class JwtService {
    public static String generateJwt(){
        Set<String> roles = new HashSet<>(
                Arrays.asList("admin","user")
        );
        Duration duration = Duration.ofMinutes(20);

        return Jwt.issuer("https://example.com/issuer")
                .subject("connect-api")
                .groups(roles)
                .expiresIn(duration)
                .sign();
    }
}
