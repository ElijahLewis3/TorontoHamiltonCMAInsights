# MFHE — Multi-Family Housing & Employment Insights

Full-stack web application for exploring how multi-family housing activity relates to regional employment in selected Canadian census metropolitan areas (CMAs). It combines housing construction data with Statistics Canada labour-market records so urban planners, policy analysts, and researchers can compare trends visually and export data for further analysis.

## Why This Matters for Urban Planning

Housing supply decisions should be grounded in labour-market reality. This tool helps planners answer questions like:

- **Where is demand outpacing supply?** If employment in a CMA is growing steadily but housing starts are flat or declining, the region likely needs more residential construction, particularly multi-family units that densify existing urban areas.
- **Which dwelling types are under-built?** The starts-vs-completions breakdown by category (singles, semis, rows, apartments) reveals whether the housing mix matches workforce needs. A CMA dominated by single-detached starts in a market that needs affordable rental apartments signals a mismatch.
- **How did external shocks affect the pipeline?** The COVID-19 employment drop in 2020 is visible in the data. Planners can see how quickly housing starts recovered relative to jobs, and whether completions lagged, creating pent-up demand.
- **Seasonal and cyclical patterns** The heat map shows month-by-month starts intensity, helping planners anticipate construction-labour bottlenecks and time permit approvals.

By connecting employment growth to housing construction in the same dashboard, MFHE gives planners an evidence-based starting point for zoning decisions, infrastructure investment, and housing-policy recommendations.

## Tech Stack

| Layer | Tech |
|-------|------|
| Frontend | React 18, TypeScript, Vite |
| Backend | Java 17+, Spring Boot 3.4, OpenCSV |
| Database | MySQL |
| Operations | Docker Compose |

## Prerequisites

- **Docker** & **Docker Compose**
- **Java 17** or newer 
- **Maven 3.9+** 
- **Node.js 20+** & **npm**

## Starting the Application

### 1. Create your environment file

Copy the example and fill in your own values:

```bash
cp .env.example .env
```

Edit `.env` and replace the `change_me_*` placeholders with secure passwords. This file is **gitignored** and must be created on every machine that runs the project.

### 2. Start MySQL

```bash
docker compose up -d
```

MySQL is exposed on **localhost:3307** by default. Adjust `MYSQL_HOST_PORT` in `.env` to change the port.

### 3. Start the Spring Boot API

```bash
cd backend
./mvnw spring-boot:run 
```

The API starts on **http://localhost:8080**.

On first launch, the application attempts to **pull live data from Statistics Canada**. If the download fails (e.g. no internet), it falls back to bundled CSV files so the app still works offline.

### 4. Start the React dev server

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open **http://localhost:5173**

## Environment Variables

All configuration is managed through environment variables. See `.env.example` for the full list.

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password (required) | — |
| `MYSQL_USER` | MySQL application user (required) | — |
| `MYSQL_PASSWORD` | MySQL application password (required) | — |
| `MYSQL_DATABASE` | Database name | `mfhe_db` |
| `MYSQL_HOST_PORT` | Host port for MySQL | `3307` |
| `DB_URL` | JDBC connection string | `jdbc:mysql://localhost:3307/mfhe_db` |
| `DB_USER` | Spring Boot DB user | `mfhe_user` |
| `DB_PASSWORD` | Spring Boot DB password | `mfhe_pass` |
| `CORS_ORIGIN` | Allowed frontend origin | `http://localhost:5173` |
| `ADMIN_API_KEY` | API key for `/api/admin/*` endpoints (empty = no protection) | — |
| `JPA_DDL_AUTO` | Hibernate DDL mode (`update`, `validate`, `none`) | `update` |

## Data Pipeline

The application pulls data directly from **Statistics Canada's open-data service**. No API key is required.

| Dataset | StatsCan Table | Download URL |
|---------|---------------|--------------|
| Employment by CMA and NAICS industry (monthly) | 14-10-0382-01 | `https://www150.statcan.gc.ca/n1/tbl/csv/14100382-eng.zip` |
| Housing starts and completions by CMA and dwelling type (monthly) | 34-10-0143-01 | `https://www150.statcan.gc.ca/n1/tbl/csv/34100143-eng.zip` |


## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/regions` | List available CMAs |
| GET | `/api/insights/combined/{code}` | Housing starts vs employment time series |
| GET | `/api/insights/employment/naics/{code}` | NAICS industry breakdown |
| GET | `/api/insights/housing/categories/{code}` | Starts vs completions by category |
| GET | `/api/insights/housing/heatmap/{code}` | Heat-map data (starts by period × category) |
| GET | `/api/export/employment/{code}` | Download employment CSV |
| GET | `/api/export/housing/{code}` | Download housing CSV |
| POST | `/api/admin/refresh` | Re-fetch latest data from Statistics Canada (requires `X-API-Key` header if `ADMIN_API_KEY` is set) |


## Data Sources

- **Statistics Canada** — Labour Force Survey, Table 14-10-0382-01 ([open data](https://www150.statcan.gc.ca/t1/tbl1/en/tv.action?pid=1410038201))
- **Canada Mortgage and Housing Corporation (CMHC)** — Starts & Completions Survey, Table 34-10-0143-01 ([open data](https://www150.statcan.gc.ca/t1/tbl1/en/tv.action?pid=3410014301))

Both datasets are released under the [Statistics Canada Open Licence](https://www.statcan.gc.ca/en/reference/licence).
