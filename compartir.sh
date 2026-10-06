#!/usr/bin/env bash
# Comparte la aplicación por Internet con un túnel de Cloudflare (macOS y Linux).
# Necesita cloudflared instalado: brew install cloudflared (macOS) o el paquete de
# https://github.com/cloudflare/cloudflared/releases (Linux).
set -e
cd "$(dirname "$0")"

if curl -s -o /dev/null http://localhost:8080/; then
  echo "Ya hay algo en el puerto 8080. Cierra la aplicación y vuelve a ejecutar ./compartir.sh"
  exit 1
fi
command -v cloudflared >/dev/null || { echo "Falta cloudflared (brew install cloudflared)"; exit 1; }

if [ -z "${CREADOR_PASSWORD:-}" ]; then
  echo "La aplicación va a ser pública: elige una contraseña para el creador de apuestas (creador@apuestas.es)."
  read -r -s -p "Contraseña del creador: " CREADOR_PASSWORD; echo
  [ -n "$CREADOR_PASSWORD" ] || { echo "Hace falta una contraseña para el creador."; exit 1; }
  export CREADOR_PASSWORD
fi

./mvnw -q spring-boot:run -Dspring-boot.run.profiles=compartir > compartir.log 2>&1 &
APP=$!
trap 'kill $APP 2>/dev/null' EXIT

echo "Esperando a que la aplicación arranque (log en compartir.log)..."
until curl -s -o /dev/null http://localhost:8080/; do
  kill -0 $APP 2>/dev/null || { echo "La aplicación no ha arrancado, mira compartir.log"; exit 1; }
  sleep 3
done

echo "Ahora sale un enlace https://....trycloudflare.com: es el que hay que compartir. Ctrl+C para cortar."
cloudflared tunnel --url http://localhost:8080
