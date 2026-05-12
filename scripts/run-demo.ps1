#!/usr/bin/env pwsh
# Runs every examples/demo-*.txt file through the compiler JAR after building once.

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $root

$jar = Join-Path $root 'target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path $jar)) {
    Write-Host 'Building JAR (first run)...' -ForegroundColor DarkGray
    mvn -q package -DskipTests
}

$demos = Get-ChildItem (Join-Path $root 'examples\demo-*.txt') | Sort-Object Name
if ($demos.Count -eq 0) {
    Write-Error 'No examples\demo-*.txt files found.'
}

foreach ($f in $demos) {
    Write-Host "`n===== $($f.Name) =====`n" -ForegroundColor Cyan
    java -jar $jar $f.FullName
}
