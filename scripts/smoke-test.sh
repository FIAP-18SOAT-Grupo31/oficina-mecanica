#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
SENHA="${DEMO_PASSWORD:-dev123}"
STATUS=""
falhas=0

req() {
    local metodo="$1" caminho="$2" token="${3:-}" corpo="${4:-}"
    local args=(-s -o /dev/null -w '%{http_code}' -X "$metodo" "$BASE_URL$caminho")
    [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
    [ -n "$corpo" ] && args+=(-H 'Content-Type: application/json' -d "$corpo")
    STATUS="$(curl "${args[@]}")" || STATUS=000
}

espera() {
    local descricao="$1" esperado="$2" obtido="$STATUS"
    if [ "$obtido" = "$esperado" ]; then
        printf '  ok    %-45s %s\n' "$descricao" "$obtido"
    else
        printf '  FALHA %-45s esperado %s, veio %s\n' "$descricao" "$esperado" "$obtido"
        falhas=$((falhas + 1))
    fi
}

login() {
    curl -s -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' \
        -d "{\"login\":\"$1\",\"senha\":\"$SENHA\"}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p'
}

echo "Smoke test em $BASE_URL"

for tentativa in $(seq 1 40); do
    curl -fs "$BASE_URL/v3/api-docs" > /dev/null && break
    sleep 3
done

req GET /v3/api-docs;            espera "OpenAPI no ar" 200
req GET /swagger-ui/index.html;  espera "Swagger UI no ar" 200

if ! curl -s "$BASE_URL/v3/api-docs" | grep -q "/api/auth/login"; then
    echo "  autenticação ainda não existe nesta versão, verificações de login puladas"
else
    req GET /actuator/health;                                          espera "health público" 200
    req GET /actuator/prometheus;                                      espera "métricas do Prometheus" 200
    req GET /actuator/metrics;                                         espera "actuator sem token" 401
    req POST /api/auth/login "" '{"login":"admin","senha":"errada"}';  espera "login com senha errada" 401
    req POST /api/auth/login "" '{"login":""}';                        espera "login sem senha" 400

    for usuario in admin atendente mecanico; do
        if [ -n "$(login "$usuario")" ]; then
            printf '  ok    %-45s\n' "login de $usuario"
        else
            printf '  FALHA %-45s\n' "login de $usuario"
            falhas=$((falhas + 1))
        fi
    done

    req GET /actuator/metrics "$(login mecanico)";  espera "actuator com papel MECANICO" 403
    req GET /actuator/metrics "$(login admin)";     espera "actuator com papel ADMIN" 200
    req GET /actuator/metrics "token-invalido";     espera "token inválido" 401
fi

if [ "$falhas" -gt 0 ]; then
    echo "$falhas verificação(ões) falharam"
    exit 1
fi
echo "Tudo certo"
