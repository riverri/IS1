@echo off
chcp 65001 >nul
cd /d "%~dp0"

curl -s -o nul http://localhost:8080/
if not errorlevel 1 (
  echo Ya hay algo en el puerto 8080. Cierra la aplicacion ^(IntelliJ o la terminal^) y vuelve a ejecutar compartir.bat.
  pause
  exit /b 1
)

if not defined CREADOR_PASSWORD (
  echo La aplicacion va a ser publica: elige una contrasena para el creador de apuestas ^(creador@apuestas.es^).
  set /p CREADOR_PASSWORD=Contrasena del creador: 
)
if not defined CREADOR_PASSWORD (
  echo Hace falta una contrasena para el creador.
  pause
  exit /b 1
)

if not exist cloudflared.exe (
  echo Descargando cloudflared...
  curl -L -o cloudflared.exe https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe
  if errorlevel 1 (
    echo No se ha podido descargar cloudflared.
    pause
    exit /b 1
  )
)

echo Arrancando la aplicacion en otra ventana...
start "Apuestas" cmd /k mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=compartir"

echo Esperando a que la aplicacion arranque...
:espera
timeout /t 3 /nobreak >nul
curl -s -o nul http://localhost:8080/
if errorlevel 1 goto espera

echo.
echo Ahora sale un enlace https://....trycloudflare.com: es el que hay que compartir.
echo Para cortar, cierra esta ventana y la de la aplicacion.
echo.
cloudflared.exe tunnel --url http://localhost:8080
pause
