## Upgrade Spec
KeyCloak 22 부터 Java Adapter 지원 중단되어 관련 Library 제거
```
keycloak-admin-client 16.0.0          → keycloak-admin-client 26.0.4
keycloak-spring-boot-starter 16.0.0   → 제거
keycloak-adapter-bom 16.0.0           → 제거
```
KeyCloak Adapter 제거로 Spring Security Resource Server 구축을 위한 Library 추가
```
spring-boot-starter-oauth2-resource-server
```

## Import
```java
import org.keycloak.adapters.*;    → 제거
```

## Code
KeycloakSecurityConfig.java 삭제<br>
SpringSecurityConfig.java 생성

## Final Code
### pom.xml
```xml
<dependency>
    <groupdId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupdId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>26.0.4</version>
</dependency>
```
### application-xxx.yml
```yml
#Spring Security
spring:
    security:
        oauth2:
            resourceserver:
                jwt:
                    issuer-uri: https://poc.xxx.co.kr/auth/realms/dev

#Keycloak
keycloak:
    realm: dev
    auth-server-url: https://poc.xxx.co.kr/auth
    token-path: /realms/dev/protocol/openid-connect/token
    auth-paht: /realms/dev/protocol/openid-connect/auth

#Keycloak Admin
keycloakAdmin:
    Url: https://poc.xxx.co.kr/auth
    realmMasterName: master
    adminId: ENC(ser45tfhyfgj5673654bfsrrqet)
    adminPass: ENC(d233qresgdfgj5e67irfthrtufser)
```

### SrpingSecurityConfig.java
```java
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disabled)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/configuration/ui", "/configuration/**", "/images/**", "/csrf", "/").permitAll()
                ...
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    /** 정확한 Error 확인을 위해 SecurityFilter 를 거치지 않도록 /error 등록
        등록하지 않으면 /error 도 권한 체크 되어 401 error 로 떨어짐
    */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring()
                .requestMatchers("/error");
        };
    }

}
```
