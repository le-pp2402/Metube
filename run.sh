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

resolve_java_home() {
  if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ] && [ -x "${JAVA_HOME}/bin/javac" ]; then
    printf '%s\n' "$JAVA_HOME"
    return 0
  fi

  if [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
    printf '%s\n' "/usr/lib/jvm/java-21-openjdk-amd64"
    return 0
  fi

  if command -v javac >/dev/null 2>&1; then
    javac_path="$(readlink -f "$(command -v javac)")"
    printf '%s\n' "$(dirname "$(dirname "$javac_path")")"
    return 0
  fi

  if command -v java >/dev/null 2>&1; then
    java_path="$(readlink -f "$(command -v java)")"
    printf '%s\n' "$(dirname "$(dirname "$java_path")")"
    return 0
  fi

  return 1
}

JAVA_HOME="$(resolve_java_home)" || {
  echo "No JDK found. Install Java 21 and set JAVA_HOME to its JDK directory." >&2
  exit 1
}

JAVA_VERSION="$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F'[".]' '/version/ {print $2; exit}')"
if [ -z "$JAVA_VERSION" ] || [ "$JAVA_VERSION" -lt 21 ]; then
  echo "Metube requires JDK 21. Found Java ${JAVA_VERSION:-unknown} at $JAVA_HOME." >&2
  echo "Set JAVA_HOME to a JDK 21 installation and rerun ./run.sh." >&2
  exit 1
fi

# rebuild: remove stale jar then repackage
echo "Removing stale jar..."
rm -f "$JAR"
echo "Building..."
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f "${DIR}/pom.xml" package -DskipTests -q

if [ ! -f "$JAR" ]; then
  echo "Jar not found: $JAR" >&2
  exit 1
fi

echo "Starting application: java ${JAVA_OPTS:-} -jar $JAR"
exec "$JAVA_HOME/bin/java" ${JAVA_OPTS:-} -jar "$JAR"

