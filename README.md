# MFHE — Multi-Family Housing & Employment Insights

Full-stack web application for exploring how multi-family housing activity relates to regional employment in selected Canadian census metropolitan areas (CMAs). It combines CMHC housing construction data with Statistics Canada labour-market records so urban planners, policy analysts, and researchers can compare trends visually and export data for further analysis.

## Why This Matters for Urban Planning

Housing supply decisions should be grounded in labour-market reality. This tool helps planners answer questions like:

- **Where is demand outpacing supply?** If employment in a CMA is growing steadily but housing starts are flat or declining, the region likely needs more residential construction — particularly multi-family units that densify existing urban areas.
- **Which dwelling types are under-built?** The starts-vs-completions breakdown by category (singles, semis, rows, apartments) reveals whether the housing mix matches workforce needs. A CMA dominated by single-detached starts in a market that needs affordable rental apartments signals a mismatch.
- **How did external shocks affect the pipeline?** The COVID-19 employment drop in 2020 is visible in the data. Planners can see how quickly housing starts recovered relative to jobs, and whether completions lagged, creating pent-up demand.
- **Seasonal and cyclical patterns** — The heat map shows month-by-month starts intensity, helping planners anticipate construction-labour bottlenecks and time permit approvals.

By connecting employment growth to housing construction in the same dashboard, MFHE gives planners an evidence-based starting point for zoning decisions, infrastructure investment, and housing-policy recommendations.

## Tech Stack

| Layer | Tech |
|-------|------|
| Frontend | React 18, TypeScript, Vite, Chart.js / react-chartjs-2 |
| Backend | Java 17+, Spring Boot 3.2, OpenCSV |
| Database | MySQL 8.4 |
| Ops | Docker Compose |

## Prerequisites

- **Docker** & **Docker Compose**
- **Java 17** or newer (JDK on PATH; Spring Boot 3.2 requires 17+)
- **Maven 3.9+** (or use the Maven wrapper if added)
- **Node.js 20+** & **npm**

## Quick Start

### 1. Start MySQL

```bash
docker compose up -d
```

MySQL is exposed on **localhost:3307** (not 3306) so it does not clash with another MySQL already bound to 3306. To use a different host port, edit `docker-compose.yml` and `application.yml` to match.

### 2. Start the Spring Boot API

```bash
cd backend
./mvnw spring-boot:run        # Unix/macOS
# or: mvn spring-boot:run     # if Maven is on PATH
```

The API starts on **http://localhost:8080**.

On first launch, the application attempts to **pull live data from Statistics Canada** (see Data Pipeline below). If the download fails (e.g. no internet), it falls back to bundled CSV files so the app still works offline.

### 3. Start the React dev server

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open **http://localhost:5173** — Vite proxies `/api` requests to the Spring Boot backend.

## Data Pipeline

The application pulls data directly from **Statistics Canada's open-data service**. No API key is required.

| Dataset | StatsCan Table | Download URL |
|---------|---------------|--------------|
| Employment by CMA and NAICS industry (monthly) | 14-10-0382-01 | `https://www150.statcan.gc.ca/n1/tbl/csv/14100382-eng.zip` |
| Housing starts and completions by CMA and dwelling type (monthly) | 34-10-0143-01 | `https://www150.statcan.gc.ca/n1/tbl/csv/34100143-eng.zip` |

**How it works:**

1. `StatCanFetcherService` downloads each ZIP, extracts the data CSV, and streams it through OpenCSV.
2. Only rows for Toronto CMA and Hamilton CMA are kept; all other geographies are skipped.
3. Employment rows are filtered to 10 broad NAICS industry groups; housing rows are mapped to four dwelling categories (Single, Semi-detached, Row, Apartment).
4. Parsed records are upserted into MySQL.

**Refreshing data:** Click the **Refresh Data** button in the UI, or POST to `/api/admin/refresh`. This re-downloads the latest tables from Statistics Canada and replaces the stored data. Statistics Canada typically updates these tables monthly.

**Offline fallback:** Bundled CSV files (`backend/src/main/resources/data/`) are imported if the live download fails on first startup. These contain data calibrated to published StatsCan and CMHC figures (Jan 2018 – Sep 2024).

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
| POST | `/api/admin/refresh` | Re-fetch latest data from Statistics Canada |

## Project Structure

```
MFHE/
├── docker-compose.yml
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/mfhe/
│       ├── MfheApplication.java
│       ├── config/          # CORS, DataSeeder (live fetch + CSV fallback)
│       ├── controller/      # REST + Admin endpoints
│       ├── domain/          # JPA entities
│       ├── dto/             # Response records
│       ├── repository/      # Spring Data JPA
│       └── service/         # InsightsService, StatCanFetcherService
└── frontend/
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── App.tsx
        ├── api.ts
        ├── types.ts
        ├── chartSetup.ts
        ├── palette.ts
        ├── index.css
        └── components/
            ├── CombinedChart.tsx
            ├── NaicsPieChart.tsx
            ├── HousingCategoryChart.tsx
            └── HeatmapTable.tsx
```

## Data Sources

- **Statistics Canada** — Labour Force Survey, Table 14-10-0382-01 ([open data](https://www150.statcan.gc.ca/t1/tbl1/en/tv.action?pid=1410038201))
- **Canada Mortgage and Housing Corporation (CMHC)** — Starts & Completions Survey, Table 34-10-0143-01 ([open data](https://www150.statcan.gc.ca/t1/tbl1/en/tv.action?pid=3410014301))

Both datasets are released under the [Statistics Canada Open Licence](https://www.statcan.gc.ca/en/reference/licence).
