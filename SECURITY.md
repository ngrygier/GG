# Bezpieczeństwo – Spring Security w CRM

Dokument opisuje fundament bezpieczeństwa backendu: co jest gotowe, co jest tymczasowe i czego jeszcze potrzebujemy od pozostałych członków zespołu.

> Zasada nadrzędna: **fail-closed**. Każdy brak danych, brak konfiguracji albo niezaimplementowana integracja kończy się **odmową** dostępu, nigdy jego przyznaniem.

---

## 1. Stan: gotowe vs tymczasowe

| Element | Status | Gdzie |
|---|---|---|
| `SecurityFilterChain` (stateless, publiczne `/api/public/**` i `/auth/login`, reszta wymaga uwierzytelnienia, 401/403 w stylu Bearer) | ✅ gotowe | `config/SecurityConfig` |
| `PasswordEncoder` (BCrypt) | ✅ gotowe | `config/SecurityConfig` |
| `@EnableMethodSecurity` (`@PreAuthorize`) | ✅ gotowe | `config/SecurityConfig` |
| CORS z `application.properties` (`/api/**` bez credentials, `/auth/**` z credentials) | ✅ gotowe | `config/CorsConfig`, `config/CorsProperties` |
| Mapowanie ról/uprawnień (String) → `GrantedAuthority` | ✅ gotowe | `security/authority/AuthorityMapper` |
| Weryfikacja JWT RS256: wiele kluczy publicznych po `kid`, `iss`, `aud`, `exp`, `sub` | ✅ gotowe | `security/jwt/JwtSecurityConfig` |
| Mapowanie claims → `GrantedAuthority`, `JwtAuthenticationConverter` | ✅ gotowe | `security/jwt/JwtAuthoritiesConverter`, `security/jwt/JwtSecurityConfig` |
| Reguły dostępu do rekordów (uprawnienie „widok wszystkiego” LUB przypisanie), id typu `Long` | ✅ gotowe | `security/access/ResourceAccessService` |
| Kontrakt JWT: `iss=crm-auth`, `aud=crm-api`, `sub`=id pracownika, `kid` | ✅ uzgodnione | `security/jwt/JwtProperties` |
| Włączenie JWT | ⏳ czeka na plik klucza publicznego | `application.properties` (`crm.security.jwt.public-keys[...]`) |
| Nazwy claimów `roles` / `permissions` | ⚠️ **robocze** – do potwierdzenia | `security/jwt/JwtProperties` |
| Nazwy uprawnień „widok wszystkiego” (`PROJECT_VIEW_ALL`, `CLIENT_VIEW_ALL`, `DESIGNER_VIEW_ALL`) | ⚠️ **propozycja** (tylko `TASK_VIEW_ALL` jest na liście) | `security/access/ResourceAccessProperties` |
| Sprawdzanie przypisań w bazie | ⚠️ **tymczasowe, zawsze odmawia** | `security/access/NotImplementedResourceAssignmentLookup` |
| `CrmApplicationTests` bez DataSource/JPA | ⚠️ tymczasowe – docelowo Testcontainers | `src/test/.../CrmApplicationTests` |

Pakiety (wszystkie w `pl.gg.crm`):

```
config/
  SecurityConfig          – łańcuch filtrów, PasswordEncoder, punkt włączenia JWT
  CorsConfig, CorsProperties
security/
  authority/AuthorityMapper           – String → GrantedAuthority (role z ROLE_, uprawnienia bez prefiksu)
  jwt/JwtProperties                   – kontrakt JWT (iss, aud, klucze publiczne per kid, nazwy claimów)
  jwt/JwtSecurityConfig               – JwtDecoder (RS256, kid, walidatory) + JwtAuthenticationConverter
  jwt/JwtAuthoritiesConverter         – claims → authorities
  user/CurrentUserIdResolver          – id pracownika (Long) z claimu sub
  access/ResourceAccessService        – bean "resourceAccess", kontrola dostępu do rekordów
  access/ResourceAccessProperties     – nazwy uprawnień „widok wszystkiego”
  access/ResourceAssignmentLookup     – PORT do bazy danych (interfejs)
  access/NotImplementedResourceAssignmentLookup – tymczasowa implementacja fail-closed
```

---

## 2. Architektura

### Łańcuch filtrów (`SecurityConfig`)

- `SessionCreationPolicy.STATELESS`, wyłączony request cache – serwer nie trzyma sesji.
- Wyłączone `httpBasic`, `formLogin`, `logout` – jedyną metodą uwierzytelnienia żądań API jest access token Bearer.
- Dostęp do endpointów:
  - `/api/public/**`, `/auth/login`, `/error` – publiczne (kolejne endpointy logowania dopisywać do `PUBLIC_ENDPOINTS`),
  - wszystko inne – wymaga uwierzytelnienia.
- Odpowiedzi: brak/niepoprawny token → `401` z nagłówkiem `WWW-Authenticate: Bearer`, brak uprawnień → `403`.
- **JWT** włącza się automatycznie, gdy skonfigurowano co najmniej jeden klucz publiczny (powstaje wtedy bean `JwtDecoder`). Bez klucza wszystkie niepubliczne endpointy zwracają `401` (w logach jest ostrzeżenie przy starcie).
- **CSRF wyłączony**: access token jest w nagłówku `Authorization`, którego przeglądarka nie dołącza sama. Jedyne cookie (refresh token) dotyczy `/auth/**` i jest chronione przez `SameSite=Strict` oraz zasady z sekcji 6.

### CORS (`CorsConfig`)

Konfiguracja w `application.properties` (`crm.security.cors.*`):

| Klucz | Domyślnie |
|---|---|
| `allowed-origins` | `http://localhost:5173` (z `application.properties`); brak wartości = żaden origin; `*` niedozwolone |
| `allowed-methods` | `GET, POST, PUT, PATCH, DELETE, OPTIONS` |
| `allowed-headers` | `Authorization, Content-Type, Accept` |
| `exposed-headers` | `Location` |
| `max-age` | `1h` |

- `/api/**` – **bez** credentials (token w nagłówku).
- `/auth/**` – **z** credentials, bo przeglądarka musi wysłać/odebrać cookie z refresh tokenem. Frontend wywołuje te endpointy z `credentials: 'include'`, a pozostałe API bez tego.

Adres produkcyjny frontendu ustawiamy np. zmienną środowiskową `CRM_SECURITY_CORS_ALLOWED_ORIGINS`.

---

## 3. Model ról i uprawnień (RBAC)

- Role (`OWNER`, `OFFICE_WORKER`, `PROJECT_WORKER`), uprawnienia (`CLIENT_VIEW`, `TASK_VIEW_ALL`, …) i ich przypisania **są przechowywane w PostgreSQL** – to jedyne źródło prawdy. W kodzie nie ma enumów ani listy dozwolonych nazw; nowe uprawnienie dodane w bazie działa od razu.
- Moduł logowania przy wystawianiu access tokenu odczytuje role i uprawnienia użytkownika z bazy i umieszcza je w tokenie. Backend nie odpytuje bazy o uprawnienia przy każdym żądaniu – zmiana uprawnień zadziała najpóźniej po wygaśnięciu access tokenu (ok. 45 min) lub po jego odświeżeniu.
- `AuthorityMapper` to jedyne miejsce zamiany nazw na `GrantedAuthority`:
  - rola `OWNER` → `ROLE_OWNER` (do `hasRole('OWNER')`),
  - uprawnienie `CLIENT_CREATE` → `CLIENT_CREATE` (do `hasAuthority('CLIENT_CREATE')`),
  - uprawnienie z prefiksem `ROLE_` jest odrzucane – uprawnienie nie może „udawać” roli,
  - puste nazwy są pomijane.
- **Zalecenie: autoryzujemy po uprawnieniach, nie po rolach.** `hasRole(...)` w serwisach oznacza hardkodowanie reguł biznesowych.
- Spring Security 7 dodaje do uwierzytelnionego tokenu authority `FACTOR_BEARER` (informacja o metodzie uwierzytelnienia) – to normalne i nie koliduje z naszymi nazwami.

### Jak wymagania biznesowe przekładają się na konfigurację (w bazie, nie w kodzie)

| Wymaganie | Realizacja |
|---|---|
| OWNER ma pełny dostęp | OWNER ma w bazie wszystkie uprawnienia, w tym wszystkie `*_VIEW_ALL` |
| OWNER widzi wszystkie zadania / pracowników | `TASK_VIEW_ALL`, `EMPLOYEE_VIEW` |
| OFFICE_WORKER – klienci, projektanci, projekty, oferty, dokumenty | `CLIENT_*`, `DESIGNER_*`, `PROJECT_*`, `OFFER_*`, `DOCUMENT_*` + `CLIENT_VIEW_ALL`, `DESIGNER_VIEW_ALL`, `PROJECT_VIEW_ALL` |
| OFFICE_WORKER – brak modułu pracowników | brak `EMPLOYEE_*` |
| OFFICE_WORKER / PROJECT_WORKER widzą tylko swoje zadania | `TASK_VIEW` bez `TASK_VIEW_ALL` → sprawdzane przypisanie |
| PROJECT_WORKER – klienci/projektanci tylko przez swoje projekty | `CLIENT_VIEW`, `DESIGNER_VIEW` bez `*_VIEW_ALL` → sprawdzane powiązanie przez projekt |
| PROJECT_WORKER – kalendarz | `CALENDAR_VIEW` (`CALENDAR_EDIT`) |

---

## 4. Używanie `@PreAuthorize`

Samo uprawnienie (moduł / operacja):

```java
@PreAuthorize("hasAuthority('CLIENT_CREATE')")
public ClientDto create(CreateClientRequest request) { ... }
```

Uprawnienie + dostęp do konkretnego rekordu (bean `resourceAccess` = `ResourceAccessService`):

```java
@PreAuthorize("hasAuthority('TASK_VIEW') and @resourceAccess.canAccessTask(authentication, #taskId)")
public TaskDto getTask(Long taskId) { ... }

@PreAuthorize("hasAuthority('CLIENT_VIEW') and @resourceAccess.canAccessClient(authentication, #clientId)")
public ClientDto getClient(Long clientId) { ... }
```

Dostępne metody: `canAccessProject`, `canAccessTask`, `canAccessClient`, `canAccessDesigner` – każda przyjmuje `Authentication` i id rekordu (`Long`).

Reguła w `ResourceAccessService`: **uwierzytelniony** ORAZ (**ma uprawnienie „widok wszystkiego”** dla zasobu LUB **`ResourceAssignmentLookup` potwierdza przypisanie**). `ResourceAccessService` nie sprawdza bazowego uprawnienia (`TASK_VIEW`) – zawsze łącz oba warunki.

Listy (np. „moje zadania”) filtrujemy w zapytaniu do bazy (np. po `assigneeId` = id zalogowanego pracownika, gdy brak `TASK_VIEW_ALL`). Id pracownika daje `CurrentUserIdResolver.resolveUserId(authentication)`. `@PostFilter` na dużych kolekcjach nie jest zalecany.

---

## 5. JWT RS256 – kontrakt i weryfikacja

### Uzgodniony kontrakt

| Element | Wartość |
|---|---|
| Algorytm | RS256 (inne, np. HS256, są odrzucane) |
| Nagłówek `kid` | **obowiązkowy** – token bez `kid` lub z nieznanym `kid` jest odrzucany |
| `iss` | `crm-auth` |
| `aud` | `crm-api` |
| `sub` | id pracownika (`Long` zapisany jako String, np. `"7"`) – obowiązkowy |
| `exp` | obowiązkowy; access token ważny ok. **45 min** |
| `roles`, `permissions` | listy Stringów – **nazwy robocze, do potwierdzenia** |

Przykładowy payload:

```json
{
  "iss": "crm-auth",
  "aud": "crm-api",
  "sub": "7",
  "roles": ["OFFICE_WORKER"],
  "permissions": ["CLIENT_VIEW", "CLIENT_CREATE", "TASK_VIEW"],
  "iat": 1700000000,
  "exp": 1700002700
}
```

- **Payload JWT jest jawny** (Base64URL, nie szyfrowany) – nie umieszczać w nim danych wrażliwych.
- `iss` nie jest URL-em, więc w kodzie nie używać `Jwt#getIssuer()` (rzuca wyjątek) – czytać `jwt.getClaimAsString("iss")`.
- Wszystkie wartości można zmienić w `crm.security.jwt.*` bez zmian w kodzie (`JwtProperties`).

### Klucze i rotacja

- Ten moduł zna **wyłącznie klucze publiczne** i tylko weryfikuje tokeny. Podpisywanie (klucz prywatny) należy do modułu logowania i powinno być w osobnym pakiecie/klasie. Klucz prywatny wczytywany ze zmiennej środowiskowej / secretu, **nigdy z repozytorium**.
- Konfiguracja kluczy publicznych (PEM `-----BEGIN PUBLIC KEY-----`), mapowanych po `kid`:
  ```properties
  crm.security.jwt.public-keys[2026-11]=file:${CRM_JWT_PUBLIC_KEY_PATH}
  ```
- **Rotacja**: dodajemy nowy klucz obok starego, moduł logowania zaczyna podpisywać nowym `kid`, a po wygaśnięciu starych tokenów (≥ 45 min) stary wpis można usunąć:
  ```properties
  crm.security.jwt.public-keys[2026-11]=file:/run/secrets/jwt-2026-11.pub.pem
  crm.security.jwt.public-keys[2027-02]=file:/run/secrets/jwt-2027-02.pub.pem
  ```
  Nawiasy `[...]` w nazwie są potrzebne, aby `kid` z myślnikami/kropkami został zachowany dosłownie.
- Brak lub nieczytelny plik klucza → aplikacja nie wystartuje (błąd konfiguracji jest widoczny od razu).

---

## 6. Refresh token (dla modułu logowania)

Uzgodnione: refresh token ważny **30 dni**, trzymany w cookie `HttpOnly; Secure; SameSite=Strict`, w bazie tylko jego **hash**.

Wymagania i zalecenia po stronie modułu logowania:

- Cookie: `HttpOnly; Secure; SameSite=Strict; Path=/auth` (wtedy cookie leci tylko do endpointów `/auth/**`, nie do całego API). Przeglądarki traktują `http://localhost` jako bezpieczny kontekst, więc `Secure` działa też lokalnie.
- Hash: refresh token to losowa wartość o wysokiej entropii (np. 32 bajty z `SecureRandom`), więc wystarczy SHA-256 – BCrypt nie jest potrzebny.
- Rotacja przy każdym odświeżeniu (nowy refresh token, stary unieważniony); użycie już unieważnionego tokenu = podejrzenie kradzieży → unieważnić całą rodzinę tokenów użytkownika.
- `SameSite=Strict` działa tylko, gdy frontend i API są w **tej samej witrynie** (ta sama domena rejestrowalna, np. `app.firma.pl` i `api.firma.pl`; porty się nie liczą, więc `localhost:5173` + `localhost:8080` jest OK). Przy różnych domenach cookie nie zostanie wysłane – do uwzględnienia przy wyborze adresu produkcyjnego.
- Ochrona CSRF endpointów `/auth/refresh` / `/auth/logout` (Spring CSRF jest wyłączony): `SameSite=Strict` + CORS z listą originów + wymaganie `Content-Type: application/json` (wymusza preflight). Opcjonalnie dodatkowo sprawdzać nagłówek `Origin` w tych endpointach.
- Endpoint odświeżania musi być publiczny z punktu widzenia filtra Bearer (access token może już wygasnąć) – dopisać go do `PUBLIC_ENDPOINTS` w `SecurityConfig`. Frontend nie powinien wysyłać nagłówka `Authorization` do endpointów publicznych (niepoprawny/wygasły token = `401`).
- Do haszowania haseł użytkowników użyć beana `PasswordEncoder` (BCrypt); do budowania authorities – `AuthorityMapper.toAuthorities(roles, permissions)`.

---

## 7. Miejsca wymagające integracji

### Z bazą danych (TODO(DB))

1. **`ResourceAssignmentLookup`** – zaimplementować (np. jako `@Component` z zapytaniami `existsBy...`). `employeeId` to `Long` z claimu `sub`:
   - `isTaskAssignedTo(employeeId, taskId)` – zadanie ma jednego pracownika (`existsByIdAndAssigneeId`),
   - `isClientInAssignedProject(employeeId, clientId)` – projekt ma jednego klienta: istnieje projekt tego klienta, do którego przypisany jest pracownik,
   - `isAssignedToProject(employeeId, projectId)` – relacja pracownik ↔ projekt do ustalenia,
   - `isDesignerInAssignedProject(employeeId, designerId)` – relacja projektant ↔ projekt do ustalenia.
2. **Usunąć `NotImplementedResourceAssignmentLookup`** – pozostawienie obu implementacji celowo kończy się błędem startu.
3. **`ResourceAccessProperties`** – potwierdzić lub zmienić nazwy `PROJECT_VIEW_ALL`, `CLIENT_VIEW_ALL`, `DESIGNER_VIEW_ALL` i dodać je do bazy.
4. **`CrmApplicationTests`** – po dodaniu Testcontainers (PostgreSQL) usunąć wykluczenia DataSource/JPA.

### Z logowaniem (TODO(JWT) / TODO(LOGIN))

1. **`JwtProperties`** – potwierdzić nazwy claimów `roles` / `permissions`.
2. **`application.properties`** – ustawić `crm.security.jwt.public-keys[<kid>]` dla każdego środowiska.
3. **`SecurityConfig`** – dopisać kolejne publiczne endpointy `/auth/...` do `PUBLIC_ENDPOINTS`; po skonfigurowaniu kluczy we wszystkich środowiskach można usunąć gałąź „brak JwtDecoder”.
4. **`CurrentUserIdResolver`** – obsłużyć inne typy `Authentication`, jeżeli powstaną.

---

## 8. Otwarte kwestie

- Nazwy claimów z rolami i uprawnieniami (`roles` / `permissions` – robocze).
- Finalne nazwy uprawnień, w tym uprawnień „widok wszystkiego”.
- Relacje pracownik ↔ projekt i projektant ↔ projekt.
- Docelowy adres frontendu i API (wpływa na CORS i działanie `SameSite=Strict`).

---

## 9. Znane ograniczenia

- Bez skonfigurowanego klucza publicznego wszystkie niepubliczne endpointy zwracają `401` (stan zamierzony).
- Kontrola dostępu do rekordów odmawia wszystkim bez uprawnienia `*_VIEW_ALL`, dopóki nie ma implementacji `ResourceAssignmentLookup`.
- Uprawnienia w JWT są aktualne na moment wystawienia tokenu (do ~45 min opóźnienia po zmianie w bazie). Natychmiastowe odebranie dostępu wymagałoby sprawdzania uprawnień w bazie per żądanie.
- Klucze publiczne są wczytywane przy starcie – dodanie nowego klucza wymaga restartu (docelowo można przejść na endpoint JWKS).
- Testy weryfikacji JWT używają efemerycznych kluczy RSA generowanych w pamięci przy każdym uruchomieniu (żaden klucz nie jest w repozytorium).
- Brak rate limitingu, audytu i blokady kont – poza zakresem tego etapu.
