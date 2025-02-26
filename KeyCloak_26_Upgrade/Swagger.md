## Upgrade Spec
```
Swagger2 → Swagger3
Springfox → Springdoc
```

## Library


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
Possible cross-origin (CORS) issue? The URL origin (https://petstore.swagger.io) does not match the page (https://poc.xxxx.co.kr). Check the server returns the correct 'Access-Contorl-Allow-*' headers.
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
해당하는 API들 설정 추가
```yml
springdoc:
  swagger-ui:
    url: '/api/v3/api-docs'
    configUrl: '/api/v3/api-docs/swagger-config
```

### KeyCloak 인증 오류
