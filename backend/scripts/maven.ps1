param([Parameter(ValueFromRemainingArguments=$true)][string[]]$MavenArguments)
$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path -Parent $PSScriptRoot
$javaCandidate = 'D:\develop2\JDK 17'
if (-not $env:JAVA_HOME -and (Test-Path -LiteralPath "$javaCandidate\bin\java.exe")) { $env:JAVA_HOME = $javaCandidate }
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$mavenPath = if ($mavenCommand) { $mavenCommand.Source } else { 'D:\develop2\apache-maven-3.8.8\bin\mvn.cmd' }
if (-not (Test-Path -LiteralPath $mavenPath)) { throw '请安装 Maven 3.6.3+ 并加入 PATH。' }
Push-Location $backendRoot
try {
  & $mavenPath -gs "$PSScriptRoot\maven-settings.xml" -s "$PSScriptRoot\maven-settings.xml" "-Dmaven.repo.local=$backendRoot\.m2" @MavenArguments
  exit $LASTEXITCODE
} finally { Pop-Location }
