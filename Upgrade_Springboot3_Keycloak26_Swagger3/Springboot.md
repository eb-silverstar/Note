## Upgrade Spec
KeyCloak 26 지원 가능한 스펙으로 업그레이드 (Srpingboot 3, JDK 21)
```
JDK 1.8.0.202                        → JDK 21.0.5
spring-boot-starter-parent 2.4.13    → spring-boot-starter-parent 3.4.1
```
Springboot 3 부터 Spring Security 6 으로 업그레이드 필요<br>
WebSecurityConfigurer 제거되어 사용 불가 [참고](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)
```
spring-security-core 5.6.0        → spring-security-core 6.4.2
spring-security-web 5.6.0         → spring-security-web 6.4.2
spring-security-cofnig 5.6.0      → spring-security-cofnig 6.4.2
spring-security-crypto 5.6.0      → spring-security-crypto 6.4.2
```
Tomcat 10.1.34 부터 Springboot 3 지원
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
```

## Import
```java
import javax.servlet.*    → import jakarta.servlet.*
```

## Code
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
