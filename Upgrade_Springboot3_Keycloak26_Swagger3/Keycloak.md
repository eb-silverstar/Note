## Upgrade Spec
```
Keycloak 13.0.0
JDK 1.8.0.202
    ↓
Keycloak 26.0.7
JDK 21.0.5
```
Keyclok 25.0.0 부터 Java 21 지원. (Java 17 지원 중단)

## Server
### 관리자 부트스트래핑
해당 정책이 반영되며 ver.26.0.0 부터 최초 부팅 전 임시 관리자 계정 생성 후 접근 가능
```
bin/kc.[sh|bat] start-dev --bootstrap-admin-client-id tmpadm --bootstrap-admin-client-secret secret
bin/kc.[sh|bat] start --bootstrap-admin-username tmpadm --bootstrap-admin-password pass
```
단, http://localhost:8080 이 아닌 http://127.0.0.1:8080 로 접속 시 이전 버전처럼 UI로 임시 관리자 생성 가능

### 도메인 등록
`/etc/hosts` 파일에 KeyCloak 에서 호출하는 서버 도메인 등록
```
11.111.111.11 p-server-KeycloakAP1 #해당 서버
22.222.222.22 poc.xxx.co.kr
...
```

## Admin Console

## Providers
### login-event-listner
#### Upgrade Spec
```
JDK 1.8.0.202
keycloak-server-spi-private 13.0.1
keycloak-services 13.0.1
        ↓
JDK 21.0.5
keycloak-server-spi-private 26.0.8
keycloak-services 26.0.8
```
#### Admin Console
1. `{KEYCLOAK_HOME}/providers` 폴더에 login-event-listner.jar 파일 넣고 기동시킨 후 Admin Console 접속
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
JDK 1.8.0.202
keycloak-server-spi-private 13.0.1
keycloak-core 13.0.1
keycloak-server-spi 13.0.1
keycloak-services 13.0.1
nimbus-jose-jwt 9.4.2
        ↓
JDK 21.0.5
keycloak-server-spi-private 26.0.8
keycloak-core 26.0.8
keycloak-server-spi 26.0.8
keycloak-services 26.0.8
nimbus-jose-jwt 9.40
```
#### Import
```
javax.ws.rs.core.*
org.jboss.resteasy.specimpl.MultivaluedMapImpl
        ↓
jakarta.ws.rs.core.*
```
#### Code
상속 받은 UsernamePasswordForm 파일을 참고하여 변경된 부분 수정
