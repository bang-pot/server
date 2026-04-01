# BangPot Backend

Round 1 auth flow for the BangPot backend repository.

## Stack

- Java 21 target
- Spring Boot
- Spring Security OAuth2 Client
- Gradle
- PostgreSQL

## Local setup

1. Copy `.env.example` to `.env`.
2. Update the PostgreSQL connection values for your local machine.
3. Create the database if it does not exist yet.
4. Fill in the Kakao OAuth, JWT, frontend base URL, and required terms version values.
5. If you prefer, copy `src/main/resources/application-local-secret.yml.example` to `application-local-secret.yml` and keep only secret values there.
6. Register `http://localhost:8080/login/oauth2/code/kakao` in the Kakao developer console.
7. Run the application with the `local` profile.

### PowerShell example

```powershell
Copy-Item .env.example .env
./gradlew.bat bootRun
```

The application exposes these round-1 auth endpoints:

- `GET /api/auth/me`
- `GET /api/auth/nickname-availability`
- `POST /api/auth/complete`
- `GET /api/health`

OAuth authorize, callback, and user info exchange are handled by Spring Security through:

- `GET /oauth2/authorization/kakao`
- `GET /login/oauth2/code/kakao`

If you want a local smoke run without PostgreSQL, you can temporarily start the app against in-memory H2:

```powershell
./gradlew.bat bootRun --args="--spring.datasource.url=jdbc:h2:mem:bangpot-run;MODE=PostgreSQL;DB_CLOSE_DELAY=-1 --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=create-drop --server.port=8080"
```

## Profiles

- `local`: locally installed PostgreSQL connection, SQL logging enabled, `ddl-auto=update`
- `prod`: environment-variable driven PostgreSQL connection, SQL logging disabled, `ddl-auto=validate`

The YAML setup is intentionally minimal for round 1: datasource, JPA, auth, and profile separation only.

## Verification commands

```powershell
./gradlew.bat lint
./gradlew.bat test
./gradlew.bat build
```

## Notes

- Tests use H2 and do not require a running PostgreSQL instance.
- The default runtime target is still PostgreSQL; H2 is only for local smoke verification.
- `ProviderId` is the only account matching key. Email is not used for identity.
- Temp users stay temp until `nickname + required terms agreement` is completed.
- Frontend redirects after OAuth success or failure use `BANGPOT_AUTH_FRONTEND_BASE_URL`.
