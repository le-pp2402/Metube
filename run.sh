#!/usr/bin/env bash
set -euo pipefail

# run.sh - load environment from .env or .env.example and run the app jar
# Usage: ./run.sh [--dry-run] [env-file] [jar-path]

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<EOF
Usage: $0 [--dry-run] [env-file] [jar-path]

- env-file: optional path to an env file (defaults to .env then .env.example)
- jar-path: optional path to jar (defaults to target/Metube-0.0.1-SNAPSHOT.jar)
- --dry-run: load and print variables then exit
EOF
}

DRY_RUN=0
if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then
  usage
  exit 0
fi
if [ "${1:-}" = "--dry-run" ]; then
  DRY_RUN=1
  shift
fi

# choose env file: argument -> .env -> .env.example
if [ -n "${1:-}" ] && [ -f "${1:-}" ]; then
  ENV_FILE="${1:-}"
elif [ -f "${DIR}/.env" ]; then
  ENV_FILE="${DIR}/.env"
elif [ -f "${DIR}/.env.example" ]; then
  ENV_FILE="${DIR}/.env.example"
else
  echo "No .env or .env.example found in ${DIR}" >&2
  exit 1
fi

# ensure sensible defaults so expansions in SPRING_DATASOURCE_URL work
export POSTGRES_HOST=${POSTGRES_HOST:-localhost}
export POSTGRES_PORT=${POSTGRES_PORT:-5432}

echo "Loading environment from: $ENV_FILE"

TMPENV="$(mktemp)"
trap 'rm -f "$TMPENV"' EXIT

# sanitize: remove comment lines and CR, strip optional leading 'export '
grep -E '^[[:space:]]*[A-Za-z_][A-Za-z0-9_]*=' "$ENV_FILE" | sed 's/\r$//' | sed -E 's/^[[:space:]]*export[[:space:]]+//' > "$TMPENV"

# export all variables from the sanitized file
set -a
. "$TMPENV"
set +a

if [ "$DRY_RUN" -eq 1 ]; then
  echo "\nDRY RUN - variables loaded from $ENV_FILE:\n"
  grep -E '^[[:space:]]*[A-Za-z_][A-Za-z0-9_]*=' "$ENV_FILE" | sed -E 's/^[[:space:]]*export[[:space:]]*//' | cut -d= -f1 | while read -r name; do
    printf "  %s=%s\n" "$name" "${!name-}"
  done
  exit 0
fi

# determine jar path
JAR="${2:-${DIR}/target/Metube-0.0.1-SNAPSHOT.jar}"

# rebuild: remove stale jar then repackage
echo "Removing stale jar..."
rm -f "$JAR"
echo "Building..."
JAVA_HOME="/usr/lib/jvm/java-21-amazon-corretto" \
  mvn -f "${DIR}/pom.xml" package -DskipTests -q

if [ ! -f "$JAR" ]; then
  echo "Jar not found: $JAR" >&2
  exit 1
fi

echo "Starting application: java ${JAVA_OPTS:-} -jar $JAR"
# Use Java 21 if available, otherwise use current JAVA_HOME
if [ -d "/usr/lib/jvm/java-21-amazon-corretto" ]; then
  export JAVA_HOME="/usr/lib/jvm/java-21-amazon-corretto"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
exec java ${JAVA_OPTS:-} -jar "$JAR"

