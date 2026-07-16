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

```bash
docker compose run --rm flyway
```

# Run Playwright Tests

```bash
npx playwright test
```

# Start Flyway

```bash
cd .devcontainer
docker compose up flyway
```

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

- Java (Servlet / JSP)
- Docker
- Apache Tomcat
- MySQL
- Flyway
- GitHub Actions (CI/CD)
- Playwright (E2E Testing)
- Google Compute Engine (GCE)
- Nginx (Reverse Proxy)
- HTTPS (TLS)
