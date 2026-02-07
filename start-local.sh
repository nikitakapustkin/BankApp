#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${SCRIPT_DIR}/logs"

RESET_DB=0
SKIP_DOCKER=0

usage() {
  cat <<'EOF'
Usage: ./start-local.sh [options]

Options:
  --reset-db      Recreate docker volumes before startup (docker compose down -v)
  --skip-docker   Do not run docker compose commands
  -h, --help      Show help
EOF
}

for arg in "$@"; do
  case "$arg" in
    --reset-db)
      RESET_DB=1
      ;;
    --skip-docker)
      SKIP_DOCKER=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[start-local] Unknown option: $arg" >&2
      usage
      exit 1
      ;;
  esac
done

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "[start-local] Missing required command: $cmd" >&2
    exit 1
  fi
}

ensure_secret() {
  local name="$1"
  if [ -n "${!name:-}" ]; then
    return
  fi

  require_cmd openssl
  export "${name}=$(openssl rand -base64 32)"
  echo "[start-local] ${name} is not set, generated ephemeral value for this run"
}

stop_existing_processes() {
  local -a patterns=(
    "${SCRIPT_DIR}.*-pl bank-bootstrap -am spring-boot:run"
    "${SCRIPT_DIR}.*-pl security -am spring-boot:run"
    "${SCRIPT_DIR}.*-pl storage -am spring-boot:run"
    "${SCRIPT_DIR}/bank-bootstrap/target/classes.*org.nikitakapustkin.BankServiceApplication"
    "${SCRIPT_DIR}/security/target/classes.*org.nikitakapustkin.security.SecurityApplication"
    "${SCRIPT_DIR}/storage/target/classes.*org.nikitakapustkin.storage.StorageApplication"
  )

  local -a found=()
  local pattern
  for pattern in "${patterns[@]}"; do
    while IFS= read -r pid; do
      if [ -n "$pid" ] && [ "$pid" -ne "$$" ]; then
        found+=("$pid")
      fi
    done < <(pgrep -f "$pattern" || true)
  done

  if [ "${#found[@]}" -eq 0 ]; then
    return
  fi

  local -a unique=()
  while IFS= read -r pid; do
    unique+=("$pid")
  done < <(printf '%s\n' "${found[@]}" | awk '!seen[$0]++')

  echo "[start-local] Stopping previously running BankApp processes"
  local pid
  for pid in "${unique[@]}"; do
    kill "$pid" >/dev/null 2>&1 || true
  done

  local started_at
  started_at="$(date +%s)"
  while true; do
    local alive=0
    for pid in "${unique[@]}"; do
      if kill -0 "$pid" >/dev/null 2>&1; then
        alive=1
        break
      fi
    done

    if [ "$alive" -eq 0 ]; then
      break
    fi

    if [ $(( $(date +%s) - started_at )) -ge 10 ]; then
      for pid in "${unique[@]}"; do
        kill -9 "$pid" >/dev/null 2>&1 || true
      done
      break
    fi
    sleep 1
  done
}

wait_for_service_state() {
  local service="$1"
  local expected="$2"
  local timeout_sec="${3:-60}"
  local started_at
  started_at="$(date +%s)"

  local cid
  cid="$(docker compose ps -q "$service")"
  if [ -z "$cid" ]; then
    echo "[start-local] Container for service '$service' is not found" >&2
    exit 1
  fi

  while true; do
    local state
    state="$(docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" "$cid" 2>/dev/null || true)"

    if [ "$expected" = "running" ] && [ "$state" = "running" ]; then
      return
    fi
    if [ "$expected" = "healthy" ] && [ "$state" = "healthy" ]; then
      return
    fi

    local now
    now="$(date +%s)"
    if [ $((now - started_at)) -ge "$timeout_sec" ]; then
      echo "[start-local] Timeout waiting for '$service' to become ${expected}. Current state: ${state:-unknown}" >&2
      exit 1
    fi
    sleep 1
  done
}

wait_for_started_log() {
  local pid="$1"
  local log_file="$2"
  local marker="$3"
  local timeout_sec="${4:-90}"
  local started_at
  started_at="$(date +%s)"

  while true; do
    if ! kill -0 "$pid" >/dev/null 2>&1; then
      echo "[start-local] Process exited before startup marker '${marker}' appeared. Last log lines:"
      tail -n 120 "$log_file" || true
      return 1
    fi

    if [ -f "$log_file" ] && grep -Fq "$marker" "$log_file"; then
      return 0
    fi

    if [ $(( $(date +%s) - started_at )) -ge "$timeout_sec" ]; then
      echo "[start-local] Timeout waiting for startup marker '${marker}' in ${log_file}. Last log lines:"
      tail -n 120 "$log_file" || true
      return 1
    fi
    sleep 1
  done
}

cd "$SCRIPT_DIR"

if [ ! -x "${SCRIPT_DIR}/mvnw" ]; then
  echo "[start-local] mvnw is missing or not executable: ${SCRIPT_DIR}/mvnw" >&2
  exit 1
fi

if [ "$SKIP_DOCKER" -eq 1 ] && [ "$RESET_DB" -eq 1 ]; then
  echo "[start-local] --reset-db cannot be used together with --skip-docker" >&2
  exit 1
fi

if [ -f "${SCRIPT_DIR}/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/.env"
  set +a
fi

ensure_secret JWT_SECRET
ensure_secret JWT_SERVICE_SECRET

stop_existing_processes

if [ "$SKIP_DOCKER" -eq 0 ]; then
  require_cmd docker
  if ! docker compose version >/dev/null 2>&1; then
    echo "[start-local] docker compose is not available" >&2
    exit 1
  fi

  if [ "$RESET_DB" -eq 1 ]; then
    echo "[start-local] Recreating docker volumes"
    docker compose down -v
  fi

  echo "[start-local] Starting infrastructure with docker compose"
  docker compose up -d

  echo "[start-local] Waiting for Kafka and Postgres containers"
  wait_for_service_state kafka running 60
  wait_for_service_state pg_bank healthy 90
  wait_for_service_state pg_security healthy 90
  wait_for_service_state pg_storage healthy 90
fi

mkdir -p "$LOG_DIR"

declare -a PIDS=()
declare -a NAMES=()
declare -a LOGS=()
CLEANED_UP=0

start_service() {
  local name="$1"
  shift
  local log_file="${LOG_DIR}/${name}.log"
  : > "$log_file"

  echo "[start-local] Starting ${name}"
  "$@" >"$log_file" 2>&1 &

  PIDS+=("$!")
  NAMES+=("$name")
  LOGS+=("$log_file")
}

cleanup() {
  if [ "$CLEANED_UP" -eq 1 ]; then
    return
  fi
  CLEANED_UP=1

  local count="${#PIDS[@]}"
  if [ "$count" -eq 0 ]; then
    return
  fi

  echo
  echo "[start-local] Stopping local services"
  for pid in "${PIDS[@]}"; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
  done
  wait || true
}

trap cleanup EXIT INT TERM

start_service security ./mvnw -pl security -am spring-boot:run
start_service bank ./mvnw -pl bank-bootstrap -am spring-boot:run
start_service storage ./mvnw -pl storage -am spring-boot:run

wait_for_started_log "${PIDS[0]}" "${LOGS[0]}" "Started SecurityApplication" 120
wait_for_started_log "${PIDS[1]}" "${LOGS[1]}" "Started BankServiceApplication" 120
wait_for_started_log "${PIDS[2]}" "${LOGS[2]}" "Started StorageApplication" 120

echo "[start-local] Services started. Logs:"
echo "  security: ${LOG_DIR}/security.log"
echo "  bank:     ${LOG_DIR}/bank.log"
echo "  storage:  ${LOG_DIR}/storage.log"
echo
echo "Endpoints:"
echo "  security UI: http://localhost:8082/ui/index.html"
echo "  bank API:    http://localhost:8080/swagger-ui/index.html"
echo
echo "Press Ctrl+C to stop all three services."
tail -n 50 -f "${LOG_DIR}/security.log" "${LOG_DIR}/bank.log" "${LOG_DIR}/storage.log"
