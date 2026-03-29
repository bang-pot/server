# BangPot Backend

Round 1 bootstrap for the BangPot backend repository.

## Stack

- Java 21 target
- Spring Boot
- Gradle
- PostgreSQL
- Flyway

## Local setup

1. Copy `.env.example` to `.env`.
2. Update the PostgreSQL connection values for your local machine.
3. Create the database if it does not exist yet.
4. If you prefer, copy `src/main/resources/application-local-secret.yml.example` to `application-local-secret.yml` and keep only secret values there.
5. Run the application with the `local` profile.

### PowerShell example

```powershell
Copy-Item .env.example .env
./gradlew.bat bootRun
```

The application exposes `GET /api/health` as the bootstrap smoke endpoint.

## Profiles

- `local`: locally installed PostgreSQL connection, SQL logging enabled, `ddl-auto=validate`
- `prod`: environment-variable driven PostgreSQL connection, SQL logging disabled, `ddl-auto=validate`

The YAML setup is intentionally minimal for round 1: datasource, JPA, Flyway, and profile separation only.
Schema changes should be tracked with Flyway migrations instead of JPA auto-update.

## Verification commands

```powershell
./gradlew.bat lint
./gradlew.bat test
./gradlew.bat build
```

## Notes

- Tests use the `test` profile and do not require a running PostgreSQL instance.
- Running the application locally or on a server expects a locally installed PostgreSQL instance.
- Reference config shape was aligned with the existing `C:\bangpot-server\src\main\resources` layout, but no real secrets were copied into this repository.
- Domain packages for `auth`, `crew`, `explore`, and `meeting` are intentionally empty in round 1 so later builders can start without re-bootstrap work.
