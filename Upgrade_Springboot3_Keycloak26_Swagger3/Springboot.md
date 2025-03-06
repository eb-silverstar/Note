## Upgrade Spec
KeyCloak 26 지원 가능한 스펙으로 업그레이드 (Srpingboot 3, JDK 21)
```
JDK 1.8.0.202                        → JDK 21.0.5
spring-boot-starter-parent 2.4.13    → spring-boot-starter-parent 3.4.1
```
Springboot 3 부터 Spring Security 6 으로 업그레이드 필요<br>
WebSecurityConfigurer 제거되어 사용 불가 [(참고)](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)
```
spring-security-core 5.6.0        → spring-security-core 6.4.2
spring-security-web 5.6.0         → spring-security-web 6.4.2
spring-security-cofnig 5.6.0      → spring-security-cofnig 6.4.2
spring-security-crypto 5.6.0      → spring-security-crypto 6.4.2
```
Tomcat 10.1.34 부터 Springboot 3 지원<br>
spring-boot-starter-web 에 포함된 버전을 사용한다면 재정의 하지 않아도 됨
```
tomcat-embed-core 9.0.56         → tomcat-embed-core 10.1.34
tomcat-annotation-api 9.0.56     → tomcat-annotation-api 10.1.34
tomcat-embed-websocket 9.0.56    → tomcat-embed-websocket 10.1.34
```
Restesy 6 부터 jakarta.ws 지원
```
resteasy-client 3.15.2.Final                   → resteasy-client 6.2.11.Final
resteasy-multipart-provider 3.15.2.Final       → resteasy-multipart-provider 6.2.11.Final
resteasy-jaxb-provider 3.15.2.Final            → resteasy-jaxb-provider 6.2.11.Final
resteasy-jackson2-provider 3.15.2.Final        → resteasy-jackson2-provider 6.2.11.Final
resteasy-jaxrs 3.15.2.Final                    → 제거
```
Springboot 3 부터 HttpClient 4 지원 중단으로 HttpClient 5 Library 추가
```
httpclient5 5.4.1
```
기존 버전 지원 불가로 업그레이드한 Library
```
mybatis-spring-boot-starter 2.1.4    → mybatis-spring-boot-starter 3.0.4
logback-core 1.2.7                   → logback-core 1.5.16
logback-classic 1.2.7                → logback-classic 1.5.16
```

## Import
```java
import javax.servlet.*    → import jakarta.servlet.*
```

## Code
### getStatusCode()
getStatusCode() return 값이 HttpStatus 에서 HttpStatusCode 로 변경됨
```java
// AS-IS
ResponseEntity<String> responseResult = callRestApi("POST", uri, str);
HttpStatus statusCode = responseResult.getStatusCode();

// TO-BE
ResponseEntity<String> responseResult = callRestApi("POST", uri, str);
HttpStatuCode statuCode = responseResult.getStatusCode();
```
```java
// AS-IS
try {
    ...
} catch(HttpClientErrorException ce) {
    HttpStatus statusCode = ce.getStatusCode();
}

// TO-BE
try {
    ...
} catch(HttpClientErrorException ce) {
    HttpStatusCode statusCode = ce.getStatusCode();
}
```
```java
// AS-IS
if(resonseResult.getStatusCode().series() == HttpStatus.Series.SUCCESSFUL) {
    ...
}

// TO-BE
if(responseResult.getStatusCode().is2xxSuccessful()) {
    ...
}
```
### HttpClient 5
HttpClient 4 에서 HttpClient 5 로 전환
```java
// AS-IS
@Configuration
public class RestTemplateConfig {

    @Value("${spring.profiles.active}")
    String springProfilesActive;

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate restTemplate() throws KeyStroeException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;

        if(springProfilesActive.equals("local") {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        } else {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpCleint = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }

}
```
```java
// TO-BE
@Configuration
public class RestTemplateConfig {

    @Value("${spring.profiles.active}")
    String springProfilesActive;

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate restTemplate() throws KeyStroeException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;

        if(springProfilesActive.equals("local") {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        } else {
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        }

        CloseableHttpClient httpCleint = HttpClients.custom()
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                    .setSsslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build())
                .build())
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }

}
```
### Spring Profile
```yml
# AS-IS
spring:
    profiles: poc

# TO-BE
spring:
    config:
        activate:
            on-profile: poc
```

## Final Code
### pom.xml
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parenmt</artifactId>
    <version>3.4.1</version>
    <relativePath/>
</parent>

<groupId>com.xxx</groupId>
<artifactId>xxx-portal</artifactId>
<version>0.0.1-SNAPSHOT</version>
<packaging<>war</packaging>
<name>xxx-portal</name>
<description>xxx API Service</description>

<properties>
    <java.version>21<java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>srping-security-config</artifactId>
        <version>6.4.2</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>srping-security-web</artifactId>
        <version>6.4.2</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>srping-security-core</artifactId>
        <version>6.4.2</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>srping-security-crypto</artifactId>
        <version>6.4.2</version>
    </dependency>

    <!-- Resteasy -->
    <dependency>
        <groupId>org.jboss.resteasy</groupId>
        <artifactId>resteasy-client</artifactId>
        <version>6.2.11.Final</version>
    </dependency>

    <dependency>
        <groupId>org.jboss.resteasy</groupId>
        <artifactId>resteasy-multipart-provider</artifactId>
        <version>6.2.11.Final</version>
    </dependency>

    <dependency>
        <groupId>org.jboss.resteasy</groupId>
        <artifactId>resteasy-jaxb-provider</artifactId>
        <version>6.2.11.Final</version>
    </dependency>

    <dependency>
        <groupId>org.jboss.resteasy</groupId>
        <artifactId>resteasy-jackson2-provider</artifactId>
        <version>6.2.11.Final</version>
    </dependency>

    <dependency>
        <groupId>org.jboss.resteasy</groupId>
        <artifactId>resteasy-</artifactId>
        <version>6.2.11.Final</version>
    </dependency>

    <!-- Mybatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>3.0.4</version>
    </dependency>

    <!-- Logback -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-core</artifactId>
        <version>1.5.16</version>
    </dependency>

    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.5.16</version>
    </dependency>

    <!-- HttpClient -->
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <version>5.4.1</version>
    </dependency>
</dependencies>
```
### application-xxx.yml
```yml
spring:
    config:
        activate:
            on-profile: poc
```

## Bugfix
### 테스트 위한 Library Log 추가
#### logback-spring.xml
```xml
<logger name ="org.springframework.security" level="DEBUG" additivity="false">
    <appender-ref ref="STDOUT" />
</logger>
<logger name ="org.keycloak" level="DEBUG" additivity="false">
    <appender-ref ref="STDOUT" />
</logger>
```
## Logback Library 버전 불일치
```
Logging system failed to initialize using configuration from 'null'
java.lang.NoClassDefFoundError: ch/qos/logbnack/core/boolex/JaninoEventEvaluatorBase
...
```
pom.xml 에서 정의하고 있는 logback library 는 `logback-core 1.5.16` 하나였는데 다운된 library 는 `logback-core 1.5.16` `logback-classic 1.5.12` 2개로 확인됨<br>
logback-classic 1.5.13 부터 JaninoEventEvaluator 가 제거[(참고)](https://stackoverflow.com/questions/79326623/error-after-updating-logback-to-version-1-5-13)되어 `logback-classic 1.5.16` 으로 재정의
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.16</version>
</dependency>
```
## 순환참조
```
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'userService': Unsatisfied dependency expressed through field 'portalService': Error creating bean with name 'portalService': Unsatisfied dependency expressed through field 'userService': Error creating bean with name 'userService': Requested bean is currently increation: Is there an unresolvable circular reference or an asynchronous initialization dependency?
```
userService 에서 portalService 를 의존성 주입하고 portalService 에서 userService 를 의존성 주입하고 있음
Springboot 2.7 부터는 이런 순환참조를 원천 차단하므로 컴파일 시 오류가 발생
근본적으로는 순환참조가 발생하지 않도록 아키텍처를 변경해야 하나, 기간이 촉박하여 아래와 같이 임시 조치함
```java
// PortalService.java

@Service
public class PortalService {

    @Lazy
    @Autowired
    private UserService userService;

    ...

}
```
