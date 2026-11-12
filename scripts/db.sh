#!/usr/bin/env bash
#
# Consola interna para operar la base de datos UCO por comandos.
#
#   ./scripts/db.sh                        # modo interactivo
#   ./scripts/db.sh resumen                # un comando y sale
#   ./scripts/db.sh "usuarios PENDIENTE"
#   ./scripts/db.sh "select * from uco_usuario"   # SQL directo, sin prefijo
#   ./scripts/db.sh "sql select * from uco_usuario"  # alias equivalente
#
# Perfiles de base de datos:
#   PERFIL=oracle   (por defecto) Oracle de Docker
#   PERFIL=dev      H2 en modo Oracle (archivo ./data/uco)
#
# La consola nunca abre el puerto web: se puede usar con la aplicación corriendo.

set -euo pipefail

cd "$(dirname "$0")/.."

JAR="target/uco-backend-0.0.1-SNAPSHOT.jar"
SELLO="target/.db-build"
PERFIL="${PERFIL:-oracle}"
[[ ",$PERFIL," == *",cli,"* ]] || PERFIL="$PERFIL,cli"

if ! command -v java >/dev/null 2>&1; then
    echo "Java no está instalado." >&2
    exit 1
fi

# El jar lleva fecha fija (build reproducible), por eso se usa un sello propio.
if [[ ! -f "$JAR" || ! -f "$SELLO" ]] || [[ -n "$(find src pom.xml -type f -newer "$SELLO" -print -quit 2>/dev/null)" ]]; then
    echo "Compilando el proyecto..." >&2
    ./mvnw -q -DskipTests package >&2
    touch "$SELLO"
fi

ARGUMENTOS=(--cli
    "--spring.profiles.active=$PERFIL"
    "--logging.level.root=WARN"
    "--logging.level.co.edu.uco=WARN"
    "--spring.main.banner-mode=off")

if [[ $# -gt 0 ]]; then
    exec java -jar "$JAR" "${ARGUMENTOS[@]}" "$*"
fi

exec java -jar "$JAR" "${ARGUMENTOS[@]}"
