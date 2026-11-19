param(
    [string[]] $MavenArgs = @('verify')
)

$ErrorActionPreference = 'Stop'
$bundledJdk = Get-ChildItem (Join-Path $PSScriptRoot '.jdk21') -Directory -ErrorAction SilentlyContinue |
    Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
    Select-Object -First 1
if ($bundledJdk) {
    $env:JAVA_HOME = $bundledJdk.FullName
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + [IO.Path]::PathSeparator + $env:PATH
}

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCommand) {
    throw 'No se encuentra Java. Instala un JDK 21 y vuelve a ejecutar .\probar.ps1.'
}

$versionOutput = & java -version 2>&1 | Out-String
$versionMatch = [regex]::Match($versionOutput, 'version "([0-9]+)')
if (-not $versionMatch.Success -or [int]$versionMatch.Groups[1].Value -lt 21) {
    throw "El proyecto requiere Java 21 o superior. Versión detectada:`n$versionOutput"
}

$env:MAVEN_USER_HOME = Join-Path $PSScriptRoot '.maven-cache'
$mavenRepository = Join-Path $env:MAVEN_USER_HOME 'repository'
& (Join-Path $PSScriptRoot 'mvnw.cmd') -B "-Dmaven.repo.local=$mavenRepository" @MavenArgs
exit $LASTEXITCODE
