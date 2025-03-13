## Upgrade Spec
Keyclok 25.0.0 부터 Java 21 지원 (Java 17 지원 중단)
```
Keycloak 13.0.0    → Keycloak 26.0.7
JDK 1.8.0.202      → JDK 21.0.5
```

## Server
### 관리자 부트스트래핑
해당 정책이 반영되며 ver.26.0.0 부터 최초 부팅 전 임시 관리자 계정 생성 후 접근 가능
```
bin/kc.[sh|bat] bootstrap-admin user
```
단, http://localhost:8080 이 아닌 http://127.0.0.1:8080 로 접속 시 이전 버전처럼 UI로 임시 관리자 생성 가능

### 도메인 등록
`/etc/hosts` 파일에 KeyCloak 에서 호출하는 서버 도메인 등록
```
11.111.111.11 p-server-KeycloakAP1 #해당 서버
22.222.222.22 poc.xxx.co.kr
...
```

### Keycloak.conf
`{KEYCLOAK_HOME}/conf/keycloak.conf`
```conf
# Database
db-usernae=keycloak
db-password={DB_PW}
db-url=jdbc:postgresql://localhost:5432/keycloak
# Observability
health-enabled=ture
metrics-enabled=true
# HTTP
http-enabled=true
http-relative-path=/auth
hostname-strict=false
# Theme Cache
spi-theme-static-max-age=-1
spi-theme-cache-theme=false
spi-theme-cache-templates=false
# Logout
spi-login-protocol-openid-connect-legacy-logout-redirect-uri=true
# LDAP
https-trust-store-type=default
https-trust-store-file=/opt/openlogic-openjdk-21.0.5+11-linux-x64/lib/security/cacerts
https-trust-store-password=changit
```

### LDAP 인증서
제공 받은 ca.cer 파일을 USER_HOME 에 위치
```
keytool -import -alias ad -file "{USER_HOME}/ca.cer" -keystore "{JDK_HOME}/lib/security/cacerts" -storepass changeit
```

## Admin Console
### LDAP 연동
`dev Realm > User federation > Ldap > Settings`
```
Vendor : Active Directory
Connection URL : ldaps://ldap.com
Use Truststore SPI : Always
Connection pooling : On
Bind DN : {LDAP_ID}
Bind credentials : {LDAP_PW}
Edit moe : READ_ONLY
User DN : {LDAP 제공}
Search scope : One Level
Pagination : On
Import users : On
Sync Registrations : Off
Batch size : 1000
Periodicchanged user sync : On
그 외 Default
```
`dev Realm > User federation > Ldap > Mappers`<br>
Add Mapper
```
User Model Attribute : mobile_number
LDAP Attribute : epmobile
Reda Only : On
Force a Default Value : On
```
Edit Mapper
```
1. create date
Force a Default Value : Off

2. Email
Force a Default Value : Off

3. firstName
LDAP Attribute : sn

4. lastName
LDAP Attribute : givenName

5. modify date
Force a Default Value : Off
```

## Providers
### login-event-listner
#### Upgrade Spec
```
JDK 1.8.0.202                            → JDK 21.0.5
keycloak-server-spi-private 13.0.1       → keycloak-server-spi-private 26.0.8
keycloak-services 13.0.1                 → keycloak-services 26.0.8
```
#### Admin Console
1. `{KEYCLOAK_HOME}/providers` 폴더에 `login-event-listner.jar` 파일을 놓고 기동시킨 후 Admin Console 접속
2. `Realm settings > Events > Event listners` 에서 login_event_listner 등록
#### Bugfix
Portal 호출 시 오류 발생
```
ERROR [com.kt.keycloak.event.listner.LoginEventListnerProvider] (executor-thread-1) java.net.UnknownHostException: poc.xxx.co.kr
```
`/etc/hosts` 파일에 도메인 등록
```
22.222.222.22 poc.xxx.co.kr
```

### custom-username-password-form (LDAP용 비밀번호 암복호화)
#### Upgrade Spec
```
JDK 1.8.0.202                            → JDK 21.0.5
keycloak-server-spi-private 13.0.1       → keycloak-server-spi-private 26.0.8
keycloak-core 13.0.1                     → keycloak-core 26.0.8
keycloak-server-spi 13.0.1               → keycloak-server-spi 26.0.8
keycloak-services 13.0.1                 → keycloak-services 26.0.8
nimbus-jose-jwt 9.4.2                    → nimbus-jose-jwt 9.40
```
#### Import
```
javax.ws.rs.core.*                                → jakarta.ws.rs.core.*
org.jboss.resteasy.specimpl.MultivaluedMapImpl    → 제거
```
#### Code
상속 받은 UsernamePasswordForm 파일을 참고하여 변경된 부분 수정
#### Admin Console
1. `{KEYCLOAK_HOME}/providers` 폴더에 `custom-username-password-form.jar` `nimbus-jose-jwt-9.40.jar` 파일을 놓고 기동시킨 후 Admin Console 접속
2. `dev realm > Authentication > browser duplicate`
```
# browser with SMS
kerberos                                      → 삭제
browser with SMS Organization                 → 삭제
Username Password Form                        → Custom Username Password Form
browser with SMS Browser - Conditional OTP    → 삭제제
```
3. 생성한 browser with SMS 를 Browser flow 로 Bind

### 2fa-sms (2차인증 SMS)
#### Upgrade Spec
```
JDK 1.8.0.202                            → JDK 21.0.5
keycloak-server-spi-private 13.0.1       → keycloak-server-spi-private 26.0.8
keycloak-server-spi 13.0.1               → keycloak-server-spi 26.0.8
keycloak-services 13.0.1                 → keycloak-services 26.0.8
keycloak-parent 13.0.1                   → keycloak-parent 26.0.8
```
#### Import
```
javax.ws.rs.core.Response     → jakarta.ws.rs.core.Response
```
#### Code
##### pom.xml
```xml
<properties>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version></maven.compiler.target>
    <keycloak.version>26.0.8</keycloak.version>
    <maven-shade.version>3.2.4</maven-shade.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-server-spi-private</artifactId>
        <scope>provided</scope>
    </dependency>
    ...
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.keycloak</groupId>
            <artifactId>keycloak-parent</artifactId>
            <version>${keycloak.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
#### Admin Console
1. `{KEYCLOAK_HOME}/providers` 폴더에 `2fa-sms-authenticator.jar` 파일을 놓고 기동시킨 후 Admin Console 접속
2. `dev realm > Authentication > browser with SMS`
3. Custom Username Password Form 다음 step 으로 SMS Authentication 추가

## Bugfix
### LDAP 인증서 오류
Admin Console 에서 LDAP 연동 테스트 시 오류 발생
```
LC-SERVICES0055: Error when authenticating to LDAP: simple bind failed: ldap.xx.com:663: javax.naming.CommunicationException: simple bind failed: ldap.xxx.com:636 [Root exception is javax.net.ssl.SSLHandshakeException: PLIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target]
```
`{KEYCLOAK_HOME}/conf/keycloak.conf` 에 인증서 경로 및 changit 옵션 설정
```
# LDAP
https-trust-store-type=default
https-trust-store-file=/opt/openlogic-openjdk-21.0.5+11-linux-x64/lib/security/cacerts
https-trust-store-password=changit
```
