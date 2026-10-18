# Initial Setup

1. Generate project artifacts

    ```bash
    ./mvnw clean install
    ```

2. Build and start the Docker containers
    ```bash
    docker compose up -d --build
    ```

# Deploy Updated Java Application

```bash
docker restart my-tomcat
```

# Apply Database Migrations (Flyway)

Run from the repository root with `.env` and the Compose file explicitly specified. See [Database migrations](docs/database-migrations.md) for status checks and recovery guidance.

```bash
docker compose --env-file .env -f .devcontainer/docker-compose.yml run --rm flyway
```

# Run Playwright Tests

```bash
npx playwright test
```

# Code Formatting (Prettier)

Install the project dependencies before running the formatter for the first time.

```bash
npm install
```

Format the JSP files supported by the current JSP formatter configuration:

```bash
npm run format:jsp
```

Format all supported JSP, JavaScript, CSS, Markdown, JSON, and YAML files:

```bash
npm run format
```

Check formatting without changing files:

```bash
npm run format:check
```

Format a specific file:

```bash
npx prettier --write "src/main/webapp/WEB-INF/jsp/security/auditLog.jsp"
```

JSP files are formatted with `prettier-plugin-jsp`. Legacy JSP files that use syntax unsupported by the plugin are listed in `.prettierignore` and should be migrated before enabling automatic formatting for them.

# Start Flyway

Use the [database migration procedure](docs/database-migrations.md); do not remove the DB volume for a routine migration.

# Apply Modified Application

1. Move to the project directory

    ```bash
    cd nwproject_docker
    ```

2. Build the project

    ```bash
    ./mvnw package
    ```

3. Perform a browser hard reload (Super Reload).

# VS Code Workspace Cache

```text
%APPDATA%\Code\User\workspaceStorage
```

# Tech Stack

-   Application: Java 21, Servlet 4.0 / JSP / JSTL, JavaScript, CSS; built as a WAR with Maven and run on Apache Tomcat 9.
-   Application libraries: MySQL Connector/J, Jackson, Lombok, and Spring Security's BCrypt password encoder.
-   Database: MySQL 8.0 with Flyway 11 SQL migrations.
-   Local runtime: Docker Compose with Nginx as the reverse proxy and TLS termination.
-   Optional AI integration: the separate My-AiCreate service, connected to Ollama through its API.
-   Optional article reader: the separate Qiita Guide (AI_Support) application, containerized with Node.js 22.
-   Quality checks: JUnit 5, Playwright for browser E2E tests, and Prettier with `prettier-plugin-jsp` for formatting.
-   Automation and deployment: GitHub Actions for CI; Google Compute Engine is configured as an optional deployment target (not required for local development).
