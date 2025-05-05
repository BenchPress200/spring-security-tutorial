# 🚀 Summary
> Spring Security의 동작 원리를 이해하고, 인증-인가 구현을 목표로 합니다.

> [!NOTE]
> 사용된 이미지의 출처는 모두 https://docs.spring.io/spring-security/reference/servlet/architecture.html 입니다.

<br><br>

## 📌 Spring Security 란?
> 인증, 권한 부여 및 보호 기능을 제공하는 프레임워크 입니다.

Spring Security를 사용함으로써 인증, 권한 확인에 필요한 기능과 옵션들을 제공받을 수 있습니다.

<br><br>

## 📌 동작방식
### Servlet Filters

<img width="300" alt="a" src="https://github.com/user-attachments/assets/c6bf4f08-772c-4ad5-a2d7-e7754165a39c"><br>
위 그림은 단일 HTTP 요청에 대한 일반적인 핸들러 계층을 보여주고 있습니다. HTTP 요청이 들어오면 서블릿 컨테이너(톰캣)가 FIlterChain을 만들어서 여러 개의 필터들과 최종 목적지인 Servlet을 실행시킵니다. FilterChain은 필터들을 순차적으로 실행해주고, 마지막에 Servlet을 호출합니다.

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    // do something before the rest of the application
    chain.doFilter(request, response); // invoke the rest of the application
    // do something after the rest of the application
}
```
각 필터들은 자신보다 후순위의 필터들과 서블릿에만 영향을 줄 수 있기 때문에 필터들의 호출 순서는 매우 중요합니다.

```text
HTTP Request -> FilterA -> FilterB -> DispacherServlet -> FilterB -> FilterA -> HTTP Response
```
예를 들어, 필터 A-B-서블릿 순서로 실행된다고 했을 때 필터 A는 필터 B까지 도달하지 못하도록 할 수도 있고, 필터 B에게 전달되는 요청을 수정하거나 필터 B에서 나온 응답을 수정할 수도 있습니다.

<br>

<img width="300" alt="ㅠ" src="https://github.com/user-attachments/assets/f9291481-f439-43d8-93ac-0cf74cc8a9be" /><br>
위 사진은 Spring Security의 핵심 중 하나인 DelegatingFilterProxy의 위치를 나타냅니다. 서블릿 컨테이너(톰캣)는 필터를 등록할 수 있지만 스프링 컨테이너가 관리하는 스프링 빈의 존재를 알 수 없습니다. 반면에, Spring Security의 필터는 전부 스프링 빈 입니다. 그러므로, 서블릿 컨테이너와 Spring Bean간 연결이 필요하고, DelegatingFilterProxy 그 역할을 하고 있습니다.
<br><br>정리하면 다음과 같습니다.

- 서블릿 컨테이너가 관리하는 필터체인이 있음
- 스프링 시큐리티는 스프링 컨테이너에 의해 자체적인 필터를 스프링 빈으로 생성해서 관리
    - 서블릿 컨테이너는 스프링 컨테이너가 관리하는 빈의 존재를 모름 ⇒ 커스텀 필터 어떻게 추가?
- 스프링이 `DelegatingFilterProxy`의 필터 구현체를 제공
    - 내부적으로 스프링의 필터 빈을 호출하도록 함
    - `DelegatingFilterProxy` = 서블릿 컨테이너와 스프링 컨테이너 사이의 중계자

<br>

<img width="300" alt="c" src="https://github.com/user-attachments/assets/a90409e0-d808-4271-82e1-15340b848004" /><br>
`SecurityFilterChain`은 현재 요청에 대해 어떤 스프링 시큐리티 필터를 사용할지 결정할 수 있습니다. `SecurityFilterChain`의 시큐리티 필터들은 `DelegatingFilterProxy` 을 대신해서 등록됩니다. 스프링의 입장에서, 서블릿에 도달하기전에 본인(스프링)이 자체적으로 필터를 추가해 방화벽 역할을 할 수 있습니다. 또한, 서블릿 컨테이너가 관리하는 필터에서는 오로지 URL 기반으로 필터링을 진행했다면, `SecurityFilterChain` 의 필터는 `RequestMatcher` 인터페이스를 사용하여 `HttpServletRequest` 에 담긴 모든 것을 활용하여 필터 호출을 결정할 수 있습니다.

<br>

### Security Filters
시큐리티 필터들은 SecurityFilterChain API 로 `FilterChainProxy` 에 삽입됩니다. 이 필터들은 다양한 목적을 가지고 사용되는데, 적절한 시점에 호출되기 위해서 특정 순서를 지키면서 실행됩니다. 예를 들어, 인증을 위한 필터는 인가를 위한 필터보다 반드시 먼저 실행되어야 합니다. 아래는 해당 필터들의 순서를 확인할 수 있는 소스코드 입니다.<br>
[🔐 Spring Security 필터 체인 소스 코드](https://github.com/spring-projects/spring-security/blob/6.4.5/config/src/main/java/org/springframework/security/config/annotation/web/builders/FilterOrderRegistration.java)

<br>

### Adding Filters to the Filter Chain
기본으로 존재하는 시큐리티 필터들은 충분히 보안을 제공하지만, 커스텀 필터를 추가할 수 있습니다. 필터 추가 메서드는 아래와 같습니다.

```java
// 두번째 인자로 전달한 필터 클래스 이전에 추가
addFilterBefore(Filter, Class<?>);

// 두번째 인자로 전달한 필터 클래스 이후에 추가
addFilterAfter(Filter, Class<?>);

// 두번째 인자로 전달한 필터 클래스 대체
addFilterAt(Filter, Class<?>);
```

<br>


### Servlet Authentication Architecture
스프링 시큐리티가 제공하는 인증 아키텍처에 대해서 알아보려고 합니다. 구성 컴포넌트는 다음과 같습니다.

| 컴포넌트 | 역할 |
|----------|------|
| **SecurityContextHolder** | 인증 정보를 보관하는 공간입니다. `ThreadLocal`을 사용하여 현재 요청 처리 중인 **스레드**에 인증 객체를 저장합니다. |
| **SecurityContext** | `SecurityContextHolder`가 가지고 있는 실제 컨텍스트 객체이며, 현재 인증된 사용자의 `Authentication` 객체를 보관합니다. |
| **Authentication** | 인증 정보를 담는 객체입니다. 인증 정보는 다음과 같습니다:<br> - principal: 사용자 정보 (보통 `UserDetails` 객체)<br> - credentials: 비밀번호 등 인증 수단<br> - authorities: 권한 목록 (예: `ROLE_USER`)<br> - authenticated: 인증 완료 여부 |
| **GrantedAuthority** | 인증된 사용자(`Authentication`에 있는 `principal`)가 가진 권한 또는 역할입니다. |
| **AuthenticationManager** | 인증을 처리하는 핵심 인터페이스입니다. |
| **ProviderManager** | `AuthenticationManager`의 기본 구현체로, 여러 개의 `AuthenticationProvider`를 순차적으로 시도합니다. |
| **AuthenticationProvider** | 실제 인증을 수행하는 객체입니다. |
| **Request Credentials with AuthenticationEntryPoint** | 인증이 필요하지만 인증되지 않은 사용자가 요청하면 동작하는 예외 처리 **진입점**입니다. |
| **AbstractAuthenticationProcessingFilter** | `UsernamePasswordAuthenticationFilter` 등 인증 필터의 공통 로직을 구현한 추상 클래스입니다. 인증 흐름의 전반적인 구조를 구현합니다:<br> - `attemptAuthentication()`: 인증 시도<br> - `successfulAuthentication()`: 성공 시 호출<br> - `unsuccessfulAuthentication()`: 실패 시 호출 |

<br>

<img width="300" alt="g" src="https://github.com/user-attachments/assets/e3340384-7032-4bfc-89a6-387f2669bf75" /><br>
위 사진은 스프링 시큐리티의 인증 진행 흐름입니다.

1. `AbstractAuthenticationProcessingFilter` 가 요청 유저의 `HttpServletRequest`로부터 `Authentication` 을 생성
  - `UsernamePasswordAuthenticationFilter` 를 사용하면, `HttpServletRequest` 에서 username과 password로 `UsernamePasswordAuthenticationToken`을 생성
2. `Authentication` 객체를 인증을 위해서 `AuthenticationManager` 로 넘김
  - `AuthenticationManager`의 구현체에 의해 인증진행
3. 인증에 실패한다면, `SecurityContextHolder` 를 클리어하고, 결과적으로 `AuthenticationFailureHanlder` 호출
4. 인증에 성공한다면, 인증 객체는 SecurityContextHolder에 할당되고, 결과적으로 `AuthenticationSuccessHandler` 호출

<br>

### Authorization Architecture
인가의 동작 흐름은 다음과 같습니다.
1. 사용자가 요청 보냄
2. 스프링 시큐리티 필터를 통해 인증 진행
3. 인증에 성공하면 `Authentication` 객체에 `GrantedAuthority` 포함
4. `AuthorizationManager` 가 권한 평가
5. 권한 없으면 403 Forbidden 반환

<br>

### Handling Security Exceptions
`ExceptionTranslationFilter`은`AccessDeniedException`이나`AuthenticationException` 예외를 HTTP 응답으로 바꿔주는 필터입니다.

- 로그인 안 된 (인증 X 상태) 사용자가 요청할 때 ⇒ 401 Unauthorized
- 권한이 부족한 사용자가 요청할 때 ⇒ 403 Forbidden

<br>


인증-인가 예외 처리의 흐름은 다음과 같습니다.<br>
<img width="300" alt="q" src="https://github.com/user-attachments/assets/f7e6e810-158b-42b8-a774-52fbaba4d7d1" /><br>
1. `ExceptionTranslationFilter` 의 이전 필터에서 인증을 수행해서 `SecurityContextHodler`에 인증 결과가 담겨있음
2. `ExceptionTranslationFilter` 는 doFilter호출해서 어플리케이션 흐름 진행
3. 마지막 필터인 `AuthorizationFilter` 에서 인가 여부를 확인하고 예외 던짐
4. `ExceptionTranslationFilter` 가 받아서 익명 유저인 경우 인증에 대해서 한 번더 검증 및 인가 확인
5. 인증 관련 예외일 경우 최종적으로 `AuthenticationEntryPoint` 가 처리 진행
  - `AuthenticationEntryPoint` 를 커스텀하지 않을 경우, `Http403ForbiddenEntryPoint` 가 사용될 수 있고 인증 실패이지만 403이 반환될 수도 있음
6. 인가 관련 예외일 경우 최종적으로 `AccessDeniedHandler` 가 처리 진행
   - 해당 핸들러도 적절한 응답 포맷을 생성하기 위해 커스텀 추천

  



