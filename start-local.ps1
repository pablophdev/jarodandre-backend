param([string]$FrontendPath = "$PSScriptRoot\..\ja-joyeria\jarodandre")
$ErrorActionPreference = 'Stop'
# No modifica ni elimina bases de datos existentes.
foreach ($service in @('ms-auth', 'ms-catalogo', 'ms-pedidos', 'ms-comunicaciones')) {
    $directory = Join-Path $PSScriptRoot $service
    Start-Process powershell.exe -WorkingDirectory $directory -ArgumentList '-NoExit', '-Command', '.\mvnw.cmd spring-boot:run'
}
if (Test-Path (Join-Path $FrontendPath 'package.json')) {
    Start-Process powershell.exe -WorkingDirectory $FrontendPath -ArgumentList '-NoExit', '-Command', 'npm.cmd run dev -- --port 5173 --strictPort'
}
