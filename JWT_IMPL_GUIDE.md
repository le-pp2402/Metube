# JWT + Security Implementation Guide

## Step 1 — Implement `JwtServiceImpl`

File: `src/main/java/com/phatpl/metube/auth/service/impl/JwtServiceImpl.java`

### What to inject

```java
@Service
public class JwtServiceImpl implements JwtService {
    private final KeyProvider keyProvider;
    private final TokenBlacklistService blacklistService;
    private final IdGenerator idGenerator;
    // constructor injection
}
```

### `genAccessToken`

1. Call `keyProvider.getCurrent()` to get the active `RsaKeyPair`.
2. Build a `JWSHeader` with algorithm `RS256` and `kid` set to
   `String.valueOf(keyPair.kid())`.
3. Build a `JWTClaimsSet`:
   - `subject` → `String.valueOf(user.getUser().getId())`
   - custom claim `"username"` → `user.getUsername()`
   - custom claim `"tokenVer"` → `user.getUser().getTokenVer()`
   - custom claim `"jti"` → `idGenerator.nextLongId()` (store as `Long`)
   - `issueTime` → `new Date()`
   - `expirationTime` → now + 15 minutes
   - custom claim `"type"` → `"access"`
4. Sign with `RSASSASigner(keyPair.privateKey())`.
5. Return `jwt.serialize()`.

### `genRefreshToken`

Same as `genAccessToken` but:

- expiration → now + 7 days
- `"type"` claim → `"refresh"`

### `validateToken`

1. Parse the raw token with `SignedJWT.parse(token)`.
2. Read the `kid` from the JWS header. Parse it to `Long`.
3. Call `keyProvider.getById(kid)` — if empty, throw
   `JwtException("Unknown signing key")`.
4. Verify signature: `jwt.verify(new RSASSAVerifier(keyPair.publicKey()))` — if
   false, throw.
5. Check expiry: `claims.getExpirationTime().before(new Date())` — if true,
   throw.
6. Read `jti` claim (Long). Call `blacklistService.isBlacklisted(jti)` — if
   true, throw.
7. Read `tokenVer` claim (Long) — callers will compare against the user's
   current `tokenVer`.
8. Build and return `TokenClaims`:
   ```java
   new TokenClaims(
       Long.valueOf(claims.getSubject()),
       (String) claims.getClaim("username"),
       (Long) claims.getLongClaim("tokenVer"),
       (Long) claims.getLongClaim("jti"),
       claims.getExpirationTime().toInstant(),
       (String) claims.getClaim("type")
   )
   ```

### `revokeToken`

```java
blacklistService.add(claims.jti(), claims.expiry());
```

---

## Step 2 — Spring Security Config

File: `src/main/java/com/phatpl/metube/common/config/SecurityConfig.java`

### Bean list

| Bean                         | Purpose                                |
| ---------------------------- | -------------------------------------- |
| `SecurityFilterChain`        | HTTP security rules                    |
| `JwtDecoder`                 | Decode & verify JWT using live JWK set |
| `JwtAuthenticationConverter` | Map JWT claims → `Authentication`      |
| `PasswordEncoder`            | BCrypt for password hashing            |

### `SecurityFilterChain`

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/jwks.json").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .build();
}
```

### `JwtDecoder` (backed by live Redis JWK set)

Use a `NimbusJwtDecoder` with a dynamic `JWKSource` that always delegates to
`keyProvider.jwkSet()`:

```java
@Bean
public JwtDecoder jwtDecoder() {
    JWKSource<SecurityContext> jwkSource = (selector, context) ->
        selector.select(keyProvider.jwkSet());

    return NimbusJwtDecoder.withJwkSetUri(null) // don't use URI-based
        // instead build manually:
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(
            new DefaultJWTProcessor<>() {{
                setJWSKeySelector(new JWSVerificationKeySelector<>(
                    JWSAlgorithm.RS256, jwkSource));
            }}
        );
    return decoder;
}
```

> Simpler alternative: implement `JwtDecoder` yourself by calling
> `jwtService.validateToken(token)` and returning a `Jwt` object from the
> resulting `TokenClaims`.

### `JwtAuthenticationConverter`

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt ->
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
    return converter;
}
```

### `PasswordEncoder`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### JWKS endpoint

Add a controller method to expose the public key set:

```java
@GetMapping("/jwks.json")
public Map<String, Object> jwks() {
    return keyProvider.jwkSet().toJSONObject();
}
```

---

## Step 3 — Auth DTOs

### Request DTOs

**File:**
`src/main/java/com/phatpl/metube/auth/dto/request/RegisterRequest.java`

```java
public record RegisterRequest(
    @NotBlank String username,
    @NotBlank @Size(min = 8) String password,
    @NotBlank @Email String email
) {}
```

**File:** `src/main/java/com/phatpl/metube/auth/dto/request/LoginRequest.java`

```java
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}
```

**File:**
`src/main/java/com/phatpl/metube/auth/dto/request/RefreshTokenRequest.java`

```java
public record RefreshTokenRequest(
    @NotBlank String refreshToken
) {}
```

### Response DTOs

**File:** `src/main/java/com/phatpl/metube/auth/dto/response/LoginResponse.java`

```java
public record LoginResponse(
    String accessToken,
    String refreshToken
) {}
```

**File:** `src/main/java/com/phatpl/metube/auth/dto/response/UserResponse.java`

```java
public record UserResponse(
    Long id,
    String username,
    String email,
    String avatarUrl,
    boolean verified
) {}
```

---

## Step 4 — AuthService

**File:** `src/main/java/com/phatpl/metube/auth/service/AuthService.java`  
**Impl:** `src/main/java/com/phatpl/metube/auth/service/impl/AuthServiceImpl.java`

### Interface

```java
public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshTokenRequest request);
    void logout(TokenClaims claims);
}
```

### `register`

1. Check `userRepository.findByUsername(request.username())` — if present, throw
   a `409 Conflict` exception.
2. Hash the password: `passwordEncoder.encode(request.password())`.
3. Create a new `User`, call `user.register(username, hashedPwd, email)`.
4. Save via `userRepository.save(user)`.
5. Return a mapped `UserResponse`.

> Email verification is wired here later (Step 5). For now `verified` defaults
> to `false`.

### `login`

1. Load user via `userDetailsService.loadUserByUsername(request.username())`.
2. Check `passwordEncoder.matches(request.password(), principal.getPassword())`
   — if false, throw `401`.
3. Check `principal.isEnabled()` — if false, throw `403` (account inactive or
   not verified).
4. Generate both tokens:
   ```java
   var access  = jwtService.genAccessToken(principal);
   var refresh = jwtService.genRefreshToken(principal);
   ```
5. Return `new LoginResponse(access, refresh)`.

### `refresh`

1. Call `jwtService.validateToken(request.refreshToken())`.
2. Assert `claims.type().equals("refresh")` — if not, throw `400`.
3. Load the user by `claims.userId()` from `userRepository`.
4. Assert `claims.tokenVer().equals(user.getTokenVer())` — if not, throw `401`
   (tokens revoked).
5. Revoke the old refresh token: `jwtService.revokeToken(claims)`.
6. Generate a fresh pair and return `LoginResponse`.

### `logout`

1. Assert `claims.isAccess()` — must be called with an access token.
2. Load the user, call `user.revokeTokens()`, save.
3. Also blacklist the current access token: `jwtService.revokeToken(claims)`.

---

## Step 5 — AuthController

**File:** `src/main/java/com/phatpl/metube/auth/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    // constructor injection

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        // extract TokenClaims from the already-validated JWT principal
        var claims = jwtService.validateToken(jwt.getTokenValue());
        authService.logout(claims);
    }
}
```

### JWKS endpoint

Create a separate thin controller (or add to `AuthController`):

```java
@RestController
public class JwksController {

    private final KeyProvider keyProvider;

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        return keyProvider.jwkSet().toJSONObject();
    }
}
```

---

## Step 6 — Global Exception Handling

**File:**
`src/main/java/com/phatpl/metube/common/exception/GlobalExceptionHandler.java`

Use `@RestControllerAdvice` to map domain exceptions to HTTP responses:

| Exception                                           | HTTP status |
| --------------------------------------------------- | ----------- |
| `UsernameNotFoundException`                         | 404         |
| `BadCredentialsException` / wrong password          | 401         |
| `JwtException` (invalid/expired token)              | 401         |
| Duplicate username on register                      | 409         |
| `MethodArgumentNotValidException` (bean validation) | 400         |
| `AccessDeniedException`                             | 403         |

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"
            ));
        return Map.of("errors", errors);
    }

    // add handlers for other exceptions
}
```

---

## Checklist

- [ ] `JwtServiceImpl` — `genAccessToken`
- [ ] `JwtServiceImpl` — `genRefreshToken`
- [ ] `JwtServiceImpl` — `validateToken`
- [ ] `JwtServiceImpl` — `revokeToken`
- [ ] `SecurityConfig` — `SecurityFilterChain`
- [ ] `SecurityConfig` — `JwtDecoder`
- [ ] `SecurityConfig` — `JwtAuthenticationConverter`
- [ ] `SecurityConfig` — `PasswordEncoder`
- [ ] JWKS endpoint (`/jwks.json`)
- [ ] Auth DTOs — `RegisterRequest`, `LoginRequest`, `RefreshTokenRequest`
- [ ] Auth DTOs — `LoginResponse`, `UserResponse`
- [ ] `AuthService` — `register`
- [ ] `AuthService` — `login`
- [ ] `AuthService` — `refresh`
- [ ] `AuthService` — `logout`
- [ ] `AuthController` — all endpoints
- [ ] `GlobalExceptionHandler`
