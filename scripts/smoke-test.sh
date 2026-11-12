#!/usr/bin/env bash
#
# Prueba de humo del backend UCO sin interfaz gráfica.
# Requiere: curl, jq y que la aplicación esté corriendo.
#
#   ./scripts/smoke-test.sh [URL_BASE]
#
# Ejemplo: ./scripts/smoke-test.sh http://localhost:8080

set -euo pipefail

BASE="${1:-http://localhost:8080}"
ADMIN_EMAIL="${UCO_ADMIN_EMAIL:-admin@uco.edu.co}"
ADMIN_PASSWORD="${UCO_ADMIN_PASSWORD:-Admin123!}"
SUFIJO="$(date +%s)"
EMAIL_PROFESOR="profesor.${SUFIJO}@uco.edu.co"
EMAIL_ESTUDIANTE="estudiante.${SUFIJO}@uco.edu.co"
CODIGO="EST-${SUFIJO}"
AULA="A-101"
MATERIA="Bases de Datos"

HTTP_STATUS=""
HTTP_BODY=""
TOTAL=0
FALLOS=0

for dependencia in curl jq; do
    if ! command -v "$dependencia" >/dev/null 2>&1; then
        echo "Falta la dependencia: $dependencia" >&2
        exit 1
    fi
done

api() {
    local metodo="$1" ruta="$2" token="${3:-}" cuerpo="${4:-}"
    local argumentos=(-s -X "$metodo" "${BASE}${ruta}" -H 'Accept: application/json'
                      -w $'\n%{http_code}')
    [[ -n "$token" ]] && argumentos+=(-H "Authorization: Bearer ${token}")
    [[ -n "$cuerpo" ]] && argumentos+=(-H 'Content-Type: application/json' -d "$cuerpo")

    local salida
    salida="$(curl "${argumentos[@]}")"
    HTTP_STATUS="${salida##*$'\n'}"
    HTTP_BODY="${salida%$'\n'*}"
}

verificar() {
    local descripcion="$1" esperado="$2" real="$3"
    TOTAL=$((TOTAL + 1))
    if [[ "$esperado" == "$real" ]]; then
        printf '  \033[32mOK\033[0m   %s (HTTP %s)\n' "$descripcion" "$real"
    else
        FALLOS=$((FALLOS + 1))
        printf '  \033[31mFALLA\033[0m %s -> esperaba HTTP %s, obtuve %s\n' "$descripcion" "$esperado" "$real"
        printf '        %s\n' "${HTTP_BODY:0:400}"
    fi
}

json() { jq -r "$@" <<<"$HTTP_BODY"; }

echo "== Backend UCO: prueba de humo contra ${BASE} =="

echo "-- 1. Login del administrador"
api POST /api/auth/login "" "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}"
verificar "login admin" 200 "$HTTP_STATUS"
TOKEN_ADMIN="$(json '.token')"

echo "-- 2. Registro de profesor y estudiante (quedan PENDIENTES)"
api POST /api/auth/registro/profesor "" "{
  \"nombreCompleto\":\"Profesor de Prueba\",
  \"email\":\"${EMAIL_PROFESOR}\",
  \"password\":\"Profesor123!\",
  \"aula\":\"${AULA}\",
  \"materia\":\"${MATERIA}\"
}"
verificar "registro profesor" 201 "$HTTP_STATUS"
verificar "profesor queda PENDIENTE" "PENDIENTE" "$(json '.estado')"

api POST /api/auth/registro/estudiante "" "{
  \"nombreCompleto\":\"Estudiante de Prueba\",
  \"email\":\"${EMAIL_ESTUDIANTE}\",
  \"password\":\"Estudiante123!\",
  \"aula\":\"${AULA}\",
  \"codigoEstudiantil\":\"${CODIGO}\"
}"
verificar "registro estudiante" 201 "$HTTP_STATUS"

echo "-- 3. Un usuario pendiente no puede iniciar sesión"
api POST /api/auth/login "" "{\"email\":\"${EMAIL_ESTUDIANTE}\",\"password\":\"Estudiante123!\"}"
verificar "login bloqueado por PENDIENTE" 403 "$HTTP_STATUS"

echo "-- 4. El admin aprueba a ambos"
api GET "/api/admin/usuarios?estado=PENDIENTE" "$TOKEN_ADMIN"
verificar "listar pendientes" 200 "$HTTP_STATUS"
ID_USUARIO_PROFESOR="$(json --arg email "$EMAIL_PROFESOR" '.[] | select(.email == $email) | .id')"
ID_USUARIO_ESTUDIANTE="$(json --arg email "$EMAIL_ESTUDIANTE" '.[] | select(.email == $email) | .id')"

api PATCH "/api/admin/usuarios/${ID_USUARIO_PROFESOR}/aprobar" "$TOKEN_ADMIN"
verificar "aprobar profesor" 200 "$HTTP_STATUS"
api PATCH "/api/admin/usuarios/${ID_USUARIO_ESTUDIANTE}/aprobar" "$TOKEN_ADMIN"
verificar "aprobar estudiante" 200 "$HTTP_STATUS"

echo "-- 5. Login de profesor y estudiante aprobados"
api POST /api/auth/login "" "{\"email\":\"${EMAIL_PROFESOR}\",\"password\":\"Profesor123!\"}"
verificar "login profesor" 200 "$HTTP_STATUS"
TOKEN_PROFESOR="$(json '.token')"

api POST /api/auth/login "" "{\"email\":\"${EMAIL_ESTUDIANTE}\",\"password\":\"Estudiante123!\"}"
verificar "login estudiante" 200 "$HTTP_STATUS"
TOKEN_ESTUDIANTE="$(json '.token')"

echo "-- 6. El profesor ve a los estudiantes de su aula"
api GET /api/profesor/estudiantes "$TOKEN_PROFESOR"
verificar "estudiantes del aula" 200 "$HTTP_STATUS"
verificar "el estudiante aparece en el aula" "1" "$(json --arg email "$EMAIL_ESTUDIANTE" '[.[] | select(.email == $email)] | length')"
ID_ESTUDIANTE="$(json --arg email "$EMAIL_ESTUDIANTE" '.[] | select(.email == $email) | .estudianteId')"

echo "-- 7. El profesor crea y luego modifica una nota"
api POST /api/profesor/notas "$TOKEN_PROFESOR" "{
  \"estudianteId\":${ID_ESTUDIANTE},
  \"periodo\":\"2026-1\",
  \"valor\":3.5,
  \"comentario\":\"Primera entrega\"
}"
verificar "crear nota" 201 "$HTTP_STATUS"
verificar "valor inicial" "3.5" "$(json '.valor')"
ID_NOTA="$(json '.id')"

api PUT "/api/profesor/notas/${ID_NOTA}" "$TOKEN_PROFESOR" "{
  \"periodo\":\"2026-1\",
  \"valor\":4.7,
  \"comentario\":\"Nota corregida\"
}"
verificar "modificar nota" 200 "$HTTP_STATUS"
verificar "valor actualizado" "4.7" "$(json '.valor')"

echo "-- 8. El estudiante consulta sus notas y su promedio"
api GET /api/estudiante/notas "$TOKEN_ESTUDIANTE"
verificar "notas del estudiante" 200 "$HTTP_STATUS"
verificar "una nota registrada" "1" "$(json 'length')"

api GET /api/estudiante/promedio "$TOKEN_ESTUDIANTE"
verificar "promedio" 200 "$HTTP_STATUS"
verificar "promedio calculado" "true" "$(json '(.promedio | tonumber) == 4.7')"

echo "-- 9. Reglas de acceso entre roles"
api GET /api/admin/usuarios "$TOKEN_ESTUDIANTE"
verificar "estudiante no puede entrar a /api/admin" 403 "$HTTP_STATUS"
api POST /api/profesor/notas "" "{\"estudianteId\":1,\"periodo\":\"x\",\"valor\":1}"
verificar "sin token no se puede calificar" 401 "$HTTP_STATUS"

echo "-- 10. Expulsión y reactivación por el admin"
api PATCH "/api/admin/usuarios/${ID_USUARIO_ESTUDIANTE}/expulsar" "$TOKEN_ADMIN"
verificar "expulsar estudiante" 200 "$HTTP_STATUS"
api POST /api/auth/login "" "{\"email\":\"${EMAIL_ESTUDIANTE}\",\"password\":\"Estudiante123!\"}"
verificar "login bloqueado por EXPULSADO" 403 "$HTTP_STATUS"
api PATCH "/api/admin/usuarios/${ID_USUARIO_ESTUDIANTE}/reactivar" "$TOKEN_ADMIN"
verificar "reactivar estudiante" 200 "$HTTP_STATUS"

echo "-- 11. Resumen del administrador"
api GET /api/admin/resumen "$TOKEN_ADMIN"
verificar "resumen" 200 "$HTTP_STATUS"
verificar "hay al menos 1 nota" "true" "$(json '.notas >= 1')"

echo
if [[ "$FALLOS" -eq 0 ]]; then
    printf '\033[32mTodo bien: %d verificaciones pasaron.\033[0m\n' "$TOTAL"
else
    printf '\033[31m%d de %d verificaciones fallaron.\033[0m\n' "$FALLOS" "$TOTAL"
    exit 1
fi
