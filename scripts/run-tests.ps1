#!/usr/bin/env pwsh
# Runs every test program under examples/tests/{pass,fail} through the compiler JAR.
# Use during the project discussion to walk through both correct execution and clear errors.

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $root

$jar = Join-Path $root 'target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path $jar)) {
    Write-Host 'Building JAR (first run)...' -ForegroundColor DarkGray
    mvn -q package -DskipTests
}

function Invoke-Case {
    param(
        [string]$Path,
        [string]$Expectation
    )
    $name = Split-Path -Leaf $Path
    Write-Host ''
    Write-Host ("===== {0,-40}  (expected: {1}) =====" -f $name, $Expectation) -ForegroundColor Cyan
    Write-Host ''
    & java -jar $jar $Path
}

$testsRoot = Join-Path $root 'examples\tests'

$passDir = Join-Path $testsRoot 'pass'
$failDir = Join-Path $testsRoot 'fail'

if (Test-Path $passDir) {
    Get-ChildItem (Join-Path $passDir '*.txt') | Sort-Object Name | ForEach-Object {
        Invoke-Case -Path $_.FullName -Expectation 'success'
    }
}

if (Test-Path $failDir) {
    Get-ChildItem (Join-Path $failDir '*.txt') | Sort-Object Name | ForEach-Object {
        Invoke-Case -Path $_.FullName -Expectation 'error reported'
    }
}

Write-Host ''
Write-Host 'All test cases dispatched.' -ForegroundColor Green
