# Dataset Excel Export

A Spring Boot project that imports a large CSV dataset (~500,000 rows) into MySQL and exports it to an Excel `.xlsx` file efficiently, without loading everything into memory. It is a learning project focused on batch processing, memory management, streaming Excel generation, and measuring whether multithreading actually helps.

## Table of Contents

- [Goals](#goals)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API](#api)
- [Roadmap](#roadmap)
- [Design Decisions](#design-decisions)
- [Benchmarking](#benchmarking)
- [Verification](#verification)
- [License](#license)

## Goals

- Import a ~500k-row CSV into MySQL using bulk or batch inserts, not `saveAll()` on entities.
- Export the data to `.xlsx` without `findAll()` and without holding the whole sheet in memory.
- Build a single-worker baseline first, then add a bounded thread pool and measure the difference.
- Expose the export as an asynchronous job with progress tracking.
- Verify every export automatically: no missing rows, no duplicates, correct ordering.

## Architecture

```
          CSV file (~500k rows)
                  │
                  ▼
          ┌───────────────┐
          │  CSV Importer │
          └───────┬───────┘
                  ▼
          ┌───────────────┐
          │     MySQL     │
          └───────┬───────┘
                  │  keyset-paginated batches
                  ▼
          ┌───────────────┐
          │ Export Service│
          └───────┬───────┘
                  │  (1..N reader workers)
                  ▼
          ┌───────────────┐
          │ Excel Writer  │  single writer, SXSSFWorkbook
          └───────┬───────┘
                  ▼
              output.xlsx
```

Reads may run in parallel. Writing to the workbook is always done by one writer, because a POI workbook is not thread-safe and rows must be written in order.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 4.1.1 (Spring MVC) | Backend and REST API |
| Spring JDBC (`JdbcTemplate`) | Batch inserts and keyset queries |
| MySQL | Dataset storage |
| Apache POI (`SXSSFWorkbook`) | Streaming `.xlsx` generation |
| Maven Wrapper | Build |
| React *(planned)* | Frontend |

## Project Structure

```
dataset-excel-export/
├── pom.xml
├── README.md
├── data/
│   └── sample.csv              # small sample (committed)
├── src/main/java/com/halfmile/excelexport/
│   ├── ExcelexportApplication.java
│   ├── config/                 # ExportProperties (batch size, threads)
│   ├── controller/             # REST endpoints
│   ├── service/                # ImportService, ExportService
│   ├── repository/             # JdbcTemplate queries
│   └── model/                  # Row model
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql
└── src/test/java/...
```

Large CSVs (`data/large/`), generated Excel files (`output/`, `*.xlsx`) and local credentials are excluded via `.gitignore`.

## Getting Started

### Prerequisites

- JDK 21
- MySQL 8+ running locally
- Git

### Setup

```bash
git clone https://github.com/CDBLiyanage/dataset-excel-export.git
cd dataset-excel-export
```

1. Create the database:
```sql
   CREATE DATABASE excel_export;
```
2. Create `src/main/resources/application-local.properties` (git-ignored):
```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/excel_export?rewriteBatchedStatements=true
   spring.datasource.username=your_user
   spring.datasource.password=your_password
```
3. Run the app:
```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
On Windows PowerShell: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`

## Configuration

| Property | Default | Description |
|---|---|---|
| `export.batch-size` | `10000` | Rows fetched per batch |
| `export.max-threads` | `1` | Reader workers per export |
| `export.max-concurrent-exports` | `1` | Export jobs allowed to run at once |
| `export.window-size` | `100` | Rows kept in memory by `SXSSFWorkbook` |

## API

*Planned endpoints (not all implemented yet):*

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Sanity check |
| `POST` | `/api/imports` | Import a CSV into MySQL |
| `POST` | `/api/exports` | Start an export, returns a job ID |
| `GET` | `/api/exports/{id}/status` | Job status and progress |
| `GET` | `/api/exports/{id}/download` | Download the finished `.xlsx` |

Example status response:

```json
{
  "status": "PROCESSING",
  "processed": 230000,
  "total": 500000,
  "percentage": 46
}
```

Job statuses: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`.

## Roadmap

- [x] Phase 0: Repo and Spring Boot project setup
- [ ] Phase 1: Choose and inspect the dataset
- [ ] Phase 2: MySQL schema and CSV import (small sample first, then 500k)
- [ ] Phase 3: Spring Boot connected to MySQL, read back records
- [ ] Phase 4: Single-worker export with `SXSSFWorkbook`
- [ ] Phase 5: Benchmark the baseline
- [ ] Phase 6: Bounded multithreaded reads with ordered writing
- [ ] Phase 7: Benchmark 1 / 2 / 4 / 8 workers
- [ ] Phase 8: Background jobs with progress tracking
- [ ] Phase 9: React frontend
- [ ] Phase 10: Full 500k end-to-end verification

## Design Decisions

- **`SXSSFWorkbook` over `XSSFWorkbook`**: streams old rows to a temp file so memory stays flat regardless of row count. The workbook is always disposed in a `finally` block.
- **Keyset pagination over `OFFSET`**: `WHERE id > ? ORDER BY id LIMIT ?` stays fast on late batches, while `OFFSET` gets slower as it scans and discards rows.
- **Single writer**: POI workbooks are not thread-safe and rows must be in order, so workers only read and transform. Completed batches are held until their turn.
- **Bounded queue**: limits how many finished batches can sit in memory if readers outpace the writer.
- **Bulk import**: `JdbcTemplate.batchUpdate` with `rewriteBatchedStatements=true`, or `LOAD DATA INFILE`, instead of JPA `saveAll()`.
- **Temp file then rename**: exports are written to `*.tmp` and renamed on success, so a partial file is never downloadable.
- **Async jobs**: the export endpoint returns a job ID immediately, so HTTP requests don't time out.

## Benchmarking

Method: warm-up run first, then the average of several runs, with a fixed JVM heap (`-Xmx`) and the same machine for every run.

**Environment:** *(CPU cores, RAM, MySQL version, `-Xmx` setting)*

| Batch size | Workers | Time (s) | Peak heap (MB) | CPU % | File size (MB) |
|---|---|---|---|---|---|
| 10,000 | 1 | | | | |
| 10,000 | 2 | | | | |
| 10,000 | 4 | | | | |
| 10,000 | 8 | | | | |

| Batch size | Workers | Time (s) | Peak heap (MB) |
|---|---|---|---|
| 5,000 | 1 | | |
| 25,000 | 1 | | |
| 50,000 | 1 | | |

**Conclusion:** *(to be filled in after benchmarking)*

## Verification

Every export run is checked automatically:

- Row count in Excel equals `SELECT COUNT(*)`
- First and last IDs match the database
- IDs are strictly increasing (no duplicates, correct order)
- Optional: checksum over one column

A fast export that silently drops a batch counts as a failure.

## License

Distributed under the GNU General Public License v2.0. See [LICENSE](LICENSE).