# Fleet Pulse — Auth Service (IAM Provider)

> **Linguaggio**: italiano (semplice, pensato per essere compreso anche da un junior).

---

## Sommario

1. [Cos'è l'IAM Provider](#1-cosè-liam-provider)
2. [Architettura funzionale](#2-architettura-funzionale)
3. [Teoria: Spring Security (per colloquio tecnico)](#3-teoria-spring-security-per-colloquio-tecnico)
   - [3.1 Cos'è Spring Security](#31-cosè-spring-security)
   - [3.2 Filter Chain](#32-filter-chain)
   - [3.3 AuthenticationManager e AuthenticationProvider](#33-authenticationmanager-e-authenticationprovider)
   - [3.4 UserDetailsService](#34-userdetailsservice)
   - [3.5 SecurityContext e SecurityContextHolder](#35-securitycontext-e-securitycontextholder)
   - [3.6 JWT vs Sessioni](#36-jwt-vs-sessioni)
   - [3.7 Refresh Token Rotation](#37-refresh-token-rotation)
   - [3.8 Password Encoding (BCrypt)](#38-password-encoding-bcrypt)
   - [3.9 CORS](#39-cors)
   - [3.10 Principio del minimo privilegio](#310-principio-del-minimo-privilegio)
4. [Logging](#4-logging)
5. [Pratica: implementare un IAM Provider (step-by-step)](#5-pratica-implementare-un-iam-provider-step-by-step)
   - [Step 1 — Creare il progetto Spring Boot](#51-step-1--creare-il-progetto-spring-boot)
   - [Step 2 — Configurare le dipendenze](#52-step-2--configurare-le-dipendenze)
   - [Step 3 — Modello dati: Account, Role, RefreshToken](#53-step-3--modello-dati-account-role-refreshtoken)
   - [Step 4 — Servizio JWT (JwtService)](#54-step-4--servizio-jwt-jwtservice)
   - [Step 5 — UserDetailsService](#55-step-5--userdetailsservice)
   - [Step 6 — Filtro JWT (JwtAuthenticationFilter)](#56-step-6--filtro-jwt-jwtauthenticationfilter)
   - [Step 7 — SecurityConfig](#57-step-7--securityconfig)
   - [Step 8 — AppConfig (CORS, PasswordEncoder)](#58-step-8--appconfig-cors-passwordencoder)
   - [Step 9 — AuthService (business logic)](#59-step-9--authservice-business-logic)
   - [Step 10 — AuthController (API REST)](#510-step-10--authcontroller-api-rest)
   - [Step 11 — Refresh Token con Rotation](#511-step-11--refresh-token-con-rotation)
   - [Step 12 — Configurare il Gateway davanti](#512-step-12--configurare-il-gateway-davanti)
6. [Riferimenti al codice](#6-riferimenti-al-codice)

---

## 1. Cos'è l'IAM Provider

**IAM** sta per *Identity and Access Management*. Questo microservizio:

- **Identifica** gli utenti (registrazione, login)
- **Autorizza** le operazioni (decide se un utente puo' fare una data azione)
- **Emette e valida token JWT** per garantire che le richieste successive siano autenticate

E' il "portiere" dei microservizi interni: senza un token JWT valido, non si puo' accedere a nessuna funzionalita' protetta del sistema.

---

## 2. Architettura funzionale

```
                         ┌──────────────────────────────────────────────┐
                         │              API Gateway (:8080)             │
                         │  Valida JWT, inoltra, filtra, CORS          │
                         └──────┬───────────────────────┬──────────────┘
                                │                       │
                         ┌──────▼──────────┐    ┌───────▼──────────┐
                         │   Auth Service   │    │   Fleet Service   │
                         │   (:8081)        │    │   (:8082)         │
                         │   IAM Provider   │    │                   │
                         └────────┬─────────┘    └───────────────────┘
                                  │
                         ┌────────▼─────────┐
                         │   PostgreSQL DB   │
                         │   Schema: iam     │
                         └──────────────────┘
```

**Flusso di una richiesta autenticata**:

```
1. Client → POST /iam/api/v1/auth/login
2. Gateway → (nessun JWT, permitAll) → inoltra a :8081
3. AuthService → verifica username/password → genera JWT + RefreshToken
4. AuthService → Response: { accessToken, refreshToken, tokenType, expiresIn }
5. Client → salva il token (localStorage/sessionStorage)
6. Client → GET /fleet/api/fleet/vehicles + Header: Authorization: Bearer <token>
7. Gateway → valida il JWT (firma, scadenza) → se OK, inoltra a :8082
8. FleetService → (se necessario) estrae l'utente dal token per logica di business
```

---

## 3. Teoria: Spring Security (per colloquio tecnico)

### 3.1 Cos'è Spring Security

Spring Security e' il framework di sicurezza standard per applicazioni Spring. Gestisce:

- **Autenticazione** — "chi sei?" (verifica delle credenziali)
- **Autorizzazione** — "cosa puoi fare?" (controllo dei permessi)
- **Protezione** — CSRF, CORS, session fixation, clickjacking, ecc.

Non e' un prodotto esterno: e' parte dell'ecosistema Spring e si integra nativamente.

**Domanda da colloquio**: "Spring Security e' un layer applicativo? Come si integra con il container Servlet?"
- Spring Security e' implementato come una catena di filtri Servlet (Filter Chain). Ogni richiesta HTTP passa attraverso questa catena prima di arrivare al controller. Questo lo rende indipendente dalla logica di business.

### 3.2 Filter Chain

Spring Security e' implementato come una catena di filtri che intercettano ogni richiesta HTTP. Ordine approssimativo:

```
Request → ... → CorsFilter → CsrfFilter → ... → ExceptionTranslationFilter → ... → AuthorizationFilter → Controller
```

Se un filtro blocca la richiesta (es. JWT scaduto), la risposta viene restituita immediatamente senza arrivare al controller.

**Nel nostro progetto**:
- `SecurityConfig` configura la catena con regole precise
- `JwtAuthenticationFilter` e' un filtro custom aggiunto PRIMA di `UsernamePasswordAuthenticationFilter` (l'ultimo della catena standard)
- I filtri di default (CSRF, session) sono disabilitati perche' siamo stateless

**Domanda da colloquio**: "Cosa succede se aggiungo due filtri con lo stesso order?"
- Vengono eseguiti in ordine di registrazione (primo aggiunto, primo eseguito). Non e' deterministico — meglio usare order espliciti.

### 3.3 AuthenticationManager e AuthenticationProvider

**AuthenticationManager** e' l'interfaccia principale per l'autenticazione. Ha un solo metodo: `authenticate(Authentication)`.

**AuthenticationProvider** e' l'implementazione concreta che sa come verificare un particolare tipo di autenticazione.

Nel nostro caso:
- Usiamo `DaoAuthenticationProvider` — cerca l'utente nel database via `UserDetailsService` e confronta la password con `PasswordEncoder` (BCrypt)

**Flusso**:
```
Controller → AuthenticationManager.authenticate(token)
  → DaoAuthenticationProvider.supports() → sì, supporta
  → UserDetailsService.loadUserByUsername() → carica utente
  → PasswordEncoder.matches(password, encoded) → verifica password
  → Se OK → restituisce Authentication
  → Se KO → AuthenticationException
```

### 3.4 UserDetailsService

E' l'interfaccia che Spring Security usa per caricare i dati di un utente dal database (o da qualsiasi altra fonte).

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username);
}
```

**UserDetails** e' l'interfaccia che rappresenta un utente autenticato. Deve restituire:
- `getUsername()` — identificativo univoco
- `getPassword()` — password cifrata (solo per verifiche interne)
- `getAuthorities()` — lista dei ruoli/permessi (Collection<GrantedAuthority>)

Nel nostro progetto:
- `Account` (l'entita' JPA) implementa `UserDetails` — cosi' la usiamo direttamente
- `UserDetailsServiceImpl` carica `Account` dal database tramite `AccountRepository`

**Domanda da colloquio**: "Perché Account implementa UserDetails invece di usare un adapter separato?"
- Semplificazione: meno classi, meno mapping. Lo svantaggio e' che l'entita' JPA e' accoppiata a Spring Security. Per progetti grandi si preferisce un adapter (pattern Adapter) per separare il dominio dalla sicurezza.

### 3.5 SecurityContext e SecurityContextHolder

Dopo che il filtro JWT ha verificato il token, imposta l'autenticazione:

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

Da quel momento in poi, in qualsiasi punto del codice puoi ottenere l'utente corrente:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
```

**Stateless**: alla fine della richiesta, il SecurityContext viene cancellato (grazie a `SessionCreationPolicy.STATELESS`). La prossima richiesta dovra' autenticarsi di nuovo.

**Domanda da colloquio**: "Come fai a passare l'identita' dell'utente dal gateway al microservizio?"
- Il gateway aggiunge l'header `X-User-Id` (o `X-User-Name`) prima di inoltrare la richiesta. Il microservizio downstream legge questo header e imposta il SecurityContext manualmente, oppure lo usa direttamente per logica di business.

### 3.6 JWT vs Sessioni

| Caratteristica | Sessioni | JWT |
|---|---|---|
| Dove e' salvato lo stato | Server (memoria/Redis) | Client (nel token stesso) |
| Scalabilita' | Serve session store condiviso (Redis) | Nessuno stato server → orizzontale facile |
| Revoca | Immediata (cancelli la sessione) | Complessa (serve blacklist) |
| Payload | Solo un ID di sessione | Contiene dati utente (username, ruoli) |
| Firma | N/A | HMAC o RSA |

**Perche' abbiamo scelto JWT**:
- Architettura stateless → facile scalare
- Il gateway puo' validare il JWT senza chiamare l'auth-service
- Il token contiene gia' l'identita' e i ruoli

### 3.7 Refresh Token Rotation

**Il problema**: se un JWT viene rubato, l'attaccante puo' usarlo fino alla scadenza (es. 15 minuti). Un refresh token rubato e' ancora peggio (durata maggiore, es. 7 giorni).

**La soluzione**: Refresh Token Rotation.
- Ogni volta che usi un refresh token, ne ottieni uno **nuovo** e il vecchio viene **revocato**
- Se un refresh token gia' ruotato viene riutilizzato → **possibile furto** → tutti i token dell'utente vengono revocati

**Implementazione** (`RefreshTokenService.rotate`):
1. Cerca il token nel DB
2. Se e' gia' revocato → REVOCA TUTTO (furto rilevato!)
3. Se e' scaduto → ERRORE
4. Revoca il vecchio token
5. Crea e salva un nuovo token
6. Restituisce il nuovo token

### 3.8 Password Encoding (BCrypt)

Le password NON vengono mai salvate in chiaro. Usiamo BCrypt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**BCrypt**:
- E' un algoritmo di hashing **lento** apposta (complessita' configurabile, default 10 round)
- Include automaticamente un **salt** casuale (due hash della stessa password sono diversi)
- Resistente ad attacchi con GPU/ASIC (a differenza di MD5/SHA)

**Domanda da colloquio**: "Perche' non usiamo SHA-256 per le password?"
- SHA-256 e' veloce — troppo veloce. Un attaccante puo' provare miliardi di combinazioni al secondo. BCrypt e' volutamente lento (decine di millisecondi per hash), rendendo gli attacchi di forza bruta impraticabili.

### 3.9 CORS

CORS (*Cross-Origin Resource Sharing*) e' il meccanismo che permette a un frontend su un'origine diversa (es. `http://localhost:4200`) di chiamare il backend (es. `http://localhost:8081`).

Senza CORS configurato, il browser blocca le richieste con:
```
Access to fetch at 'http://localhost:8081/...' from origin 'http://localhost:4200' has been blocked by CORS policy
```

La configurazione nell'auth-service:
```yaml
allowedOrigins: ["http://localhost:4200", "http://localhost:8080"]
```

Include `http://localhost:8080` perche' quando Swagger UI e' servita dal gateway e chiama l'auth-service direttamente (caso di errore), anche quell'origine deve essere permessa.

### 3.10 Principio del minimo privilegio

Ogni utente/ruolo dovrebbe avere solo i permessi necessari per fare il suo lavoro.

**Nel nostro progetto**:
- Ruoli definiti (es. `CUSTOMER`, `ADMIN`) — ogni ruolo ha permessi specifici
- Il gateway nega di default (`.anyRequest().authenticated()`)
- L'auth-service nega di default (stessa regola)
- Solo gli endpoint pubblici sono `permitAll`: register, login, swagger

---

## 4. Logging

L'auth-service usa **Log4j 2** (invece del Logback di default Spring Boot). Configurazione in `log4j2-spring.xml`:

```
%timestamp %livello [%thread] %logger : %messaggio
```

**Request logging**: tutte le richieste HTTP vengono loggate grazie al filtro `CommonsRequestLoggingFilter` configurato in `RequestLogConfig.java`.

Esempio di output nel terminale:
```
2026-07-04 14:32:15.123 INFO  [nio-8081-exec-1] c.f.a.c.RequestLogConfig : >>> POST /iam/api/v1/auth/login, headers={...}, payload={"username":"admin","password":"***"}
```

Il livello DEBUG per il logger `org.springframework.web.filter.CommonsRequestLoggingFilter` e' abilitato in `log4j2-spring.xml`. Per disabilitarlo, basta cambiare il livello in INFO.

---

## 5. Pratica: implementare un IAM Provider (step-by-step)

Questa guida e' generica per implementare un sistema IAM in qualsiasi progetto Spring Boot con microservizi.

### 5.1 Step 1 — Creare il progetto Spring Boot

Crea un progetto con Spring Initializr:
- **Java 17+**
- **Spring Boot 3.4.x** (o 4.x se disponibile)
- Dipendenze: Web, Security, Data JPA, PostgreSQL, Validation, Lombok

### 5.2 Step 2 — Configurare le dipendenze

Nel `pom.xml` aggiungi:

```xml
<!-- Spring Boot starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- JWT (JJWT) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Log4j 2 (se vuoi sostituire Logback) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- Springdoc (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

**Attenzione**: se usi Log4j 2, escludi `spring-boot-starter-logging` da TUTTI gli starter sopra.

### 5.3 Step 3 — Modello dati: Account, Role, RefreshToken

**Account** — entita' utente che implementa `UserDetails`:
- `id`, `username`, `email`, `password`
- `role` (ManyToOne con Role)
- `enabled`, `locked` (stato account)

**Role** — ruoli dell'utente (es. `CUSTOMER`, `ADMIN`):
- `id`, `roleName` (enum TypeRole)

**RefreshToken** — refresh token persistente:
- `id`, `token` (UUID string), `account` (ManyToOne)
- `expiresAt`, `revoked`, `createdAt`

### 5.4 Step 4 — Servizio JWT (JwtService)

Crea `JwtService` per:
- **Generare** access token con claims: `sub` (username), `roles`, `iat`, `exp`
- **Validare** firma e scadenza
- **Estrarre** username e claims dal token

Usa la libreria JJWT (`io.jsonwebtoken`):
```java
// Generazione
Jwts.builder()
    .claims(claims)
    .subject(username)
    .issuedAt(new Date())
    .expiration(...)
    .signWith(secretKey)
    .compact();

// Parsing
Jwts.parser()
    .verifyWith(secretKey)
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

### 5.5 Step 5 — UserDetailsService

Crea `UserDetailsServiceImpl` che implementa `UserDetailsService`:
```java
@Override
public UserDetails loadUserByUsername(String username) {
    return accountRepository.findByEmailOrUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("..."));
}
```

**Consiglio**: fall tornare direttamente l'entita' `Account` (che implementa `UserDetails`) per minimizzare le classi.

### 5.6 Step 6 — Filtro JWT (JwtAuthenticationFilter)

Crea un filtro che estende `OncePerRequestFilter`:
1. Legge l'header `Authorization: Bearer <token>`
2. Se assente → lascia passare (sara' bloccato dopo dalla regola `authenticated()`)
3. Estrae username dal token
4. Carica `UserDetails` dal database
5. Verifica validita' token
6. Se valido → imposta `SecurityContextHolder.getContext().setAuthentication(...)`
7. Se non valido → lascia passare (sara' bloccato)

### 5.7 Step 7 — SecurityConfig

Configura:
```java
http
    .csrf(AbstractHttpConfigurer::disable)
    .cors(cors -> cors.configurationSource(corsConfigurationSource))
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .anyRequest().authenticated()
    )
    .authenticationProvider(authenticationProvider())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

**Punti chiave**:
- `permitAll` dopo il filtro JWT: significa che il filtro NON viene eseguito per questi percorsi? No — il filtro JWT e' sempre eseguito, ma se non trova il token, passa comunque. Poi `authorizeHttpRequests` dice "non serve autenticazione" e la richiesta passa.
- `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` — il filtro JWT viene eseguito prima del filtro che processa l'autenticazione standard (login/password).
- `STATELESS` — essenziale per API REST.

### 5.8 Step 8 — AppConfig (CORS, PasswordEncoder)

Bean condivisi:
```java
@Bean
public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

@Bean
public AuthenticationManager authenticationManager(...) { ... }

@Bean
public CorsConfigurationSource corsConfigurationSource() { ... }
```

**CORS in sviluppo**: permetti `localhost:4200` (Angular) e `localhost:8080` (gateway).

### 5.9 Step 9 — AuthService (business logic)

**Registrazione**:
1. Controlla se email/gia' esiste
2. Trova il ruolo di default (es. `CUSTOMER`)
3. Crea l'utente con password cifrata (BCrypt)
4. Genera access token + refresh token
5. Restituisce `AuthResponse`

**Login**:
1. `authenticationManager.authenticate()` — Spring Security verifica username/password
2. Se OK → genera access token + refresh token
3. Restituisce `AuthResponse`

**Refresh**:
1. Chiama `refreshTokenService.rotate(token)` — ruota il refresh token
2. Genera nuovo access token
3. Restituisce `AuthResponse`

**Logout**:
1. Ruota il refresh token (lo revoca)
2. Revoca tutti i refresh token dell'utente

### 5.10 Step 10 — AuthController (API REST)

Endpoints:
```java
POST /api/v1/auth/register    → 201 { accessToken, refreshToken, ... }
POST /api/v1/auth/login       → 200 { accessToken, refreshToken, ... }
POST /api/v1/auth/refresh     → 200 { accessToken, refreshToken, ... }
POST /api/v1/auth/logout      → 204
```

**Consiglio**: definisci l'interfaccia dei controller in una classe separata (es. `AuthApi`) per la documentazione Swagger.

### 5.11 Step 11 — Refresh Token con Rotation

Implementazione in `RefreshTokenService`:
```
rotate(oldToken):
    1. Cerca oldToken nel DB
    2. Se revocato → revoca TUTTI i token dell'utente (furto!) → errore
    3. Se scaduto → errore
    4. Revoca oldToken
    5. Crea e salva nuovo token
    6. Restituisci nuovo token
```

Questa e' una best practice di sicurezza OAuth 2.0. Previene il furto di refresh token.

### 5.12 Step 12 — Configurare il Gateway davanti

L'auth-service NON dovrebbe mai essere esposto direttamente in produzione. Metti un API Gateway davanti che:
1. Valida il JWT per le richieste NON pubbliche
2. Inoltra le richieste pubbliche (register, login) senza validazione
3. Aggiunge header `X-User-Id` per i servizi downstream

**Configurazione minima gateway**:
```yaml
# Gateway route per auth-service
Path: /iam/api/v1/auth/**
Forward: http://localhost:8081

# Gateway route per Swagger
Path: /iam/swagger-ui/**
Forward: http://localhost:8081

# Gateway route per OpenAPI docs
Path: /iam/v3/api-docs/**
Forward: http://localhost:8081
```

**Problema comune**: Swagger UI carica lo spec OpenAPI dal gateway, ma il server URL nello spec punta all'auth-service diretto. Soluzione: overridare l'URL con un bean `OpenAPI`:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addServersItem(new Server().url("http://localhost:8080/iam"));
}
```

---

## 6. Riferimenti al codice

| Classe / File | Scopo | Percorso |
|---|---|---|
| `AuthServiceApplication.java` | Entry point Spring Boot | `src/main/java/com/fleetpulse/authservice/AuthServiceApplication.java` |
| `SecurityConfig.java` | Configurazione sicurezza Spring | `src/main/java/com/fleetpulse/authservice/security/SecurityConfig.java` |
| `JwtAuthenticationFilter.java` | Filtro di validazione JWT | `src/main/java/com/fleetpulse/authservice/security/JwtAuthenticationFilter.java` |
| `AppConfig.java` | Bean globali (CORS, PasswordEncoder) | `src/main/java/com/fleetpulse/authservice/config/AppConfig.java` |
| `JwtProperties.java` | Proprieta' JWT da application.yaml | `src/main/java/com/fleetpulse/authservice/config/JwtProperties.java` |
| `OpenApiConfig.java` | Configurazione Swagger/OpenAPI | `src/main/java/com/fleetpulse/authservice/config/OpenApiConfig.java` |
| `RequestLogConfig.java` | Filtro logging richieste HTTP | `src/main/java/com/fleetpulse/authservice/config/RequestLogConfig.java` |
| `JwtService.java` | Creazione e validazione JWT | `src/main/java/com/fleetpulse/authservice/service/JwtService.java` |
| `AuthService.java` | Business logic (register, login, refresh) | `src/main/java/com/fleetpulse/authservice/service/AuthService.java` |
| `RefreshTokenService.java` | Gestione refresh token (rotation) | `src/main/java/com/fleetpulse/authservice/service/RefreshTokenService.java` |
| `UserDetailsServiceImpl.java` | Caricamento utenti dal DB | `src/main/java/com/fleetpulse/authservice/service/UserDetailsServiceImpl.java` |
| `AuthController.java` | API REST per autenticazione | `src/main/java/com/fleetpulse/authservice/controller/AuthController.java` |
| `AuthResponse.java` | DTO di risposta | `src/main/java/com/fleetpulse/authservice/dto/response/AuthResponse.java` |
| `application.yaml` | Configurazione (DB, JWT, server) | `src/main/resources/application.yaml` |
| `log4j2-spring.xml` | Configurazione Log4j 2 | `src/main/resources/log4j2-spring.xml` |
| `pom.xml` | Dipendenze Maven | `pom.xml` |

Leggi i **JavaDoc** nelle singole classi per i dettagli implementativi di ogni componente.
