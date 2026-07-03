# PlacePro Operational Monitoring

> **Scope and audience.** This monitoring stack is **optional operational tooling for the placement office's IT staff** during the live placement season. It is **not** a functional requirement for students, officers, or recruiters — PlacePro works identically with monitoring disabled. It also does **not** change the core desktop-only architecture: each PlacePro client only *exposes* metrics on a local port; no data leaves the local network, and Prometheus/Grafana themselves run locally or on the campus LAN.

## How it works

PlacePro is a fleet of desktop clients, each connected to a shared MySQL server. Unlike a web app (one server to scrape), **every lab machine running PlacePro is its own scrape target**:

- On startup, each client starts an embedded HTTP server exposing Prometheus metrics at `http://<machine>:9400/metrics`.
- The port is read from `config.properties` (`metrics.port=9400`), so two clients on the same host during testing can use different ports.
- If the port is already in use (or anything else goes wrong), the client logs a warning and **continues running normally** — monitoring never blocks or crashes the placement workflow.

### Exposed metrics

| Metric | Type | Labels | Meaning |
| --- | --- | --- | --- |
| `placepro_logins_total` | Counter | `role`, `outcome` | Login attempts (success/failure) per role |
| `placepro_applications_submitted_total` | Counter | — | Applications submitted by students |
| `placepro_application_status_changes_total` | Counter | `new_status` | Application status transitions |
| `placepro_db_query_duration_seconds` | Histogram | `dao_method` | Latency of each DAO call against MySQL |
| `placepro_active_sessions` | Gauge | — | Currently logged-in sessions on this client |
| `placepro_errors_total` | Counter | `layer` (`dao`/`service`/`ui`) | Errors by application layer |
| `jvm_*`, `process_*` | various | — | JVM memory, GC, threads (simpleclient_hotspot DefaultExports) |

Quick smoke test after launching PlacePro:

```bash
curl http://localhost:9400/metrics | grep placepro
```

## Quick start (Docker, for development/demo)

The `monitoring/` folder contains a ready-to-run Prometheus + Grafana stack whose scrape config is pre-populated to hit a PlacePro client on `localhost:9400`:

```bash
cd monitoring
docker compose up -d
```

- Prometheus: <http://localhost:9090> (check **Status → Targets** to see the client being scraped)
- Grafana: <http://localhost:3000>, login `admin` / `admin` (the Prometheus data source is auto-provisioned)

Then launch PlacePro on the same machine and metrics start flowing within one scrape interval (15s).

## Running Prometheus against a fleet of lab machines

Since every lab machine is a scrape target, list them as **static targets** in `prometheus.yml`. Edit `monitoring/prometheus.yml` (or your standalone Prometheus config):

```yaml
scrape_configs:
  - job_name: "placepro-clients"
    static_configs:
      - targets: ["lab-pc-01.campus.lan:9400"]
        labels: { machine: "lab-pc-01" }
      - targets: ["lab-pc-02.campus.lan:9400"]
        labels: { machine: "lab-pc-02" }
      - targets: ["lab-pc-03.campus.lan:9400"]
        labels: { machine: "lab-pc-03" }
      - targets: ["placement-office-desk.campus.lan:9400"]
        labels: { machine: "placement-office" }
```

The `machine` label lets you break every dashboard panel down per lab machine. Use hostnames or static LAN IPs; make sure port 9400 is allowed through each machine's local firewall **for the LAN only** — do not expose it beyond the campus network.

To run Prometheus without Docker, [download it](https://prometheus.io/download/), place `prometheus.yml` next to the binary, and run:

```bash
./prometheus --config.file=prometheus.yml
```

Notes for a fleet:

- Clients that are switched off simply show as `down` targets — that's expected for desktops and harmless.
- Counters reset when a client restarts; Prometheus's `rate()`/`increase()` functions handle these resets automatically.
- Fleet-wide totals are just sums across targets, e.g. `sum(placepro_active_sessions)`.

## Grafana setup

1. Run Grafana (the Docker stack does this for you; standalone: [download](https://grafana.com/grafana/download) and start it, default port 3000).
2. Add the data source (skip if using the Docker stack — it's auto-provisioned): **Connections → Data sources → Add data source → Prometheus**, URL `http://localhost:9090` (or your Prometheus host), **Save & test**.
3. Build a starter dashboard: **Dashboards → New → New dashboard**, then add one panel per query below.

### Starter dashboard panels

**Active sessions across all clients** (Stat or Time series)

```promql
sum(placepro_active_sessions)
```

Per machine: `placepro_active_sessions` with legend `{{machine}}`.

**Login success/failure rate** (Time series, logins per minute)

```promql
sum by (outcome) (rate(placepro_logins_total[5m])) * 60
```

Split by role too: `sum by (role, outcome) (rate(placepro_logins_total[5m])) * 60`.

**Applications submitted over time** (Time series or Bar chart)

```promql
sum(increase(placepro_applications_submitted_total[1h]))
```

**DB query latency, p50 / p95** (Time series, unit: seconds)

```promql
histogram_quantile(0.50, sum by (le) (rate(placepro_db_query_duration_seconds_bucket[5m])))
```

```promql
histogram_quantile(0.95, sum by (le) (rate(placepro_db_query_duration_seconds_bucket[5m])))
```

To find the slowest DAO methods, group by `dao_method`:

```promql
topk(5, histogram_quantile(0.95,
  sum by (dao_method, le) (rate(placepro_db_query_duration_seconds_bucket[5m]))))
```

**Error rate by layer** (Time series, errors per minute)

```promql
sum by (layer) (rate(placepro_errors_total[5m])) * 60
```

Useful extras: `sum by (new_status) (increase(placepro_application_status_changes_total[1h]))` for a placement-funnel view, and `jvm_memory_bytes_used{area="heap"}` to spot memory issues on lab machines.

## Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| App logs `WARNING: could not start metrics server on port 9400` | Port in use (another PlacePro client?). Set a different `metrics.port` in `config.properties`. The app itself keeps working. |
| Target shows `down` in Prometheus | Client not running, wrong hostname/port in `prometheus.yml`, or local firewall blocking 9400 on the LAN. |
| `curl localhost:9400/metrics` works but Docker Prometheus can't scrape | On Linux, ensure the `extra_hosts: host.docker.internal:host-gateway` entry from `monitoring/docker-compose.yml` is present. |
| Panels empty in Grafana | Verify the Prometheus data source connects, and that the target is `up` in Prometheus first. |
