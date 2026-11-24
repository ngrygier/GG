# GG

System CRM dla klienta zewnętrznego umożliwiający zdalne zarządzanie firmą. System ma wspierać obsługę klientów, ofert, umów i projektów.

## Local PostgreSQL

The project uses PostgreSQL **17** in Docker. Every developer runs this stack on their own machine; the named Docker volume keeps their database data when the container is stopped or restarted. The application reads the same local `.env` file as Docker Compose, while production can provide the variables through its environment without changing application code.

### Required variables

| Variable | Purpose | Local example |
| --- | --- | --- |
| `DB_NAME` | Database name | `crm` |
| `DB_USERNAME` | Database role/user | `crm` |
| `DB_PASSWORD` | Database password | a local secret |
| `DB_HOST` | Host used by Spring Boot | `localhost` |
| `DB_PORT` | Host port used by Spring Boot | `5433` |
| `DDL_AUTO` | Hibernate schema mode | `update` locally; `validate` for deployments |

`DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` create and configure the local database container. `DB_HOST` and `DB_PORT` define where Spring Boot connects; use production host and port values when deploying. The database name and credentials remain environment-driven in every environment.

### First-time setup

1. Copy the template: `Copy-Item .env.example .env`
2. Open `.env` and replace `DB_PASSWORD=change-me` with a local secret.
3. Start PostgreSQL:

   ```powershell
   docker compose --env-file .env -f docker/docker-compose.yml up -d
   ```

4. Wait until the container is healthy:

   ```powershell
   docker compose --env-file .env -f docker/docker-compose.yml ps
   ```

5. Run the application from the repository root:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Spring Boot loads `.env` when it is present and connects to `jdbc:postgresql://localhost:5433/<DB_NAME>`. By default Hibernate uses `ddl-auto=update` to bootstrap the local development schema. Set `DDL_AUTO=validate` in deployments once the schema is managed by migrations.

### Daily commands

Start the database:

```powershell
docker compose --env-file .env -f docker/docker-compose.yml up -d
```

Stop the database while preserving data:

```powershell
docker compose --env-file .env -f docker/docker-compose.yml down
```

Follow PostgreSQL logs:

```powershell
docker compose --env-file .env -f docker/docker-compose.yml logs -f postgres
```

### Reset local database

This permanently deletes only the local Docker volume created by this Compose project:

```powershell
docker compose --env-file .env -f docker/docker-compose.yml down -v
docker compose --env-file .env -f docker/docker-compose.yml up -d
```

`.env` is intentionally ignored by Git; do not commit local credentials. Commit changes to `.env.example` when the required variable contract changes.
