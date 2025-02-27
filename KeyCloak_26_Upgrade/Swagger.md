## Upgrade Spec
```
springfox-swagger2 2.9.2
springfox-swagger-ui 2.10.5
↓
springdoc-openapi-starter-webmvc-ui 2.8.3
```
Springfox Swagger2 는 Jakarta 를 지원하지 않아 Springboot3 환경에서 사용 불가

## Code
### appication-poc.yml
```yml
#Springdoc Swagger
springdoc:
    swagger-ui:
        oauth:
            client-id: portal
        tag-sorter: alpha
        operations-sorter: alpha
        doc-expansion: none
        config-url: '/api/v3/api-docs/swagger-config'
        url: '/api/v3/api-docs'
        oauth2-redirect-url: 'https://poc.xxx.co.kr/api/swagger-ui/oaut2redirect.html'
    api-docs:
        version: openapi_3_1

#KeyCloak
keycloak:
    realm: dev
    auth-server-url: https://poc.xxx.co.kr/auth
    token-path: /realms/dev/protocol/openid-connect/token
    auth-path: /realms/dev/protocol/openid-connect/auth
```
### application-local.yml
```
#Server
server:
    servlet:
        context-path: /api/

#Springdoc Swagger
springdoc:
    swagger-ui:
        oauth:
            client-id: portal
        tag-sorter: alpha
        operations-sorter: alpha
        doc-expansion: none
    api-docs:
        version: openapi_3_1

#KeyCloak
keycloak:
    realm: dev
    auth-server-url: https://poc.xxx.co.kr/auth
    token-path: /realms/dev/protocol/openid-connect/token
    auth-path: /realms/dev/protocol/openid-connect/auth
```
### SwaggerConfig.java
```java
@Configuration
public class SwaggerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthRootUrl;

    @Value("${keycloak.token-path}")
    private String clientTokenPath;

    @Value("${keycloak.auth-path}")
    private String clientAuthPath;

    private static final String OAUTH_SCHEME_NAME = "oAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Sever().url("/api/"))
                .compomemts(new Components()
                        .addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme()))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
    }

    private Info apiInfo() {
        return new Info()
                .title("Portal Rest Api")
                .description("The document is a description of the Portal Rest Api")
                .contact(new Contact().email("portal@xxx.com"))
                .license(new License().name("Portal 1.0"))
                .version("1.0");
    }

    private SecurityScheme createOAuthScheme() {
        OAuthFlows flows = createOAuthFlows();
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(flows);
    }

    private OAuthFlows createOAuthFlows() {
        OAuthFlow flow = createAuthorizationCodeFlow();
        return new OAuthFlow()
                .authorizationCode(flow);
    }

    private OAuthFlow createAuthorizationCodeFlow() {
        return new OAuthFlow()
                .authoricationUrl(keycloakAuthRootUrl + clientAuthPath)
                .tokenUrl(keycloakAuthRootUrl + clientTokenPath);
    }

}
```
### SpringSecurityConfig.java
Swagger 경로 PermitAll 추가
```java
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        ...
        return httpSecurity
                ...
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/swagger-ui/**","/v3/api-docs","/v3/api-docs/**").permitAll
                ...
                .build();
    }

}
```

## Bugfix
### API Docs 로딩 오류 (NGINX 통해 prefix URL 설정돼 있는 경우)
POC 서버에 올린 후 Swagger 진입 시 오류 발생
```
YAMLException: bad indentation of a mapping entry (5:303)

2 | ...
3 | ...
4 | ...
5 | ... ong>We're sorry but 포털: xxx doesn't work ...
------------------------------------
6 | ...
7 | ...

Fetch error
Failed to fetch https://petstore.swagger.io/v2/swagger.json
Fetch error
Possible cross-origin (CORS) issue? The URL origin (https://petstore.swagger.io) does not match the page (https://poc.xxx.co.kr). Check the server returns the correct 'Access-Contorl-Allow-*' headers.
```
Local 에선 /v3/api-docs 를 호출하는 구간에 https://petstore.swagger.io 를 호출함
/v3/api-docs 호출하도록 설정 추가
```yml
springdoc:
    swagger-ui:
        url: '/v3/api-docs'
```
오류 메세지 변경
```
YAMLException: bad indentation of a mapping entry (5:303)

2 | ...
3 | ...
4 | ...
5 | ... ong>We're sorry but 포털: xxx doesn't work ...
------------------------------------
6 | ...
7 | ...

Parser error on line 5
bad indentation of a mapping entry

Unable to render this definition
The provided definition does not specify a valid version field.

Please indicate a valid Swagger or OpenAPIversion field. Supported version fields are swagger: "2.0" and those that match openapi: 3.x.y (for example, openapi: 3.1.0).
```
Response가 Portal Front 에서 반환해 주는 페이지임을 확인 (5번째 줄의 ' 에서 Parsing error로 인식하는 듯함)
여기서 꺠달은 문제
1. 모든 Backend 로 오는 요청은 NGINX 를 통해 /api 를 붙여야 함
2. Swagger 호출 시 일부는 /api 가 붙지 않은 채 호출됨
```
/v3/api-docs/swagger-config
/v3/api-docs
```
해당하는 경로 설정 추가
```yml
springdoc:
    swagger-ui:
        url: '/api/v3/api-docs'
        configUrl: '/api/v3/api-docs/swagger-config'
```

### KeyCloak 인증 오류
Authorization 시 KeyCloak 로그인 페이지에서 `invalid_redirect_uri` 오류 발생
호출 URL이 아래와 같음
```
https://poc.xxx.co.kr/auth/realms/{realm-name}/protocol/openid-connect/auth?response_type=code&client_id={client_id}&redirect_uri=http%3A%2F%2Fpoc.xxx.co.kr%2Fswagger-ui%2Foauth2-redirect.html&state={state}
```
Parameter 중 redirect_uri 를 보면 2가지 문제점이 존재함
1. NGINX 설정으로 인해 모든 Backend 로 오는 요청은 /api 로 호출되어야 함
2. KeyCloak 해당 Client의 Valid redirect URIs 에 등록된 주소는 `https://poc.xxx.co.kr/*` 로 redirect_uri Value 와 Protocol(http)이 다름
따라서 Swagger 설정에 redirect_uri 를 추가함
```yml
springdoc:
    swagger-ui:
        oauth2-redirect-url: 'https://poc.xxx.co.kr/api/swagger-ui/oauth2-redirect.html'
```

### API 호출 오류
