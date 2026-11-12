#!/usr/bin/env bash
#
# Levanta Oracle Free en Docker y espera a que esté listo.
# Uso: ./scripts/oracle-up.sh
#
# Requisitos: docker instalado y el usuario en el grupo docker
#   sudo systemctl enable --now docker
#   sudo usermod -aG docker "$USER"   # luego cerrar y abrir sesión

set -euo pipefail

cd "$(dirname "$0")/.."

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker no está instalado." >&2
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "No se puede hablar con Docker. ¿Está corriendo el servicio y tienes permisos?" >&2
    echo "  sudo systemctl enable --now docker" >&2
    echo "  sudo usermod -aG docker \"$USER\"" >&2
    exit 1
fi

echo "Levantando Oracle Free (la primera vez descarga ~1.6 GB)..."
docker compose up -d

echo "Esperando a que Oracle acepte conexiones (puede tardar 1-3 minutos)..."
for _ in $(seq 1 60); do
    if docker compose exec -T oracle healthcheck.sh >/dev/null 2>&1; then
        echo "Oracle listo en localhost:${ORACLE_PORT:-1521} (servicio FREEPDB1, usuario ${ORACLE_USER:-UCO})"
        exit 0
    fi
    sleep 5
done

echo "Oracle no respondió a tiempo. Revisa los logs con:" >&2
echo "  docker compose logs -f oracle" >&2
exit 1
