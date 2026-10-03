param([string]$ApiBaseUri = 'http://127.0.0.1:8080')
$ErrorActionPreference = 'Stop'
$health = Invoke-RestMethod -Uri "$ApiBaseUri/actuator/health"
$tutorials = Invoke-RestMethod -Uri "$ApiBaseUri/api/v1/content/TUTORIAL?size=50"
$security = Invoke-RestMethod -Uri "$ApiBaseUri/api/v1/auth/csrf" -SessionVariable smokeSession
$smokeHeaders = @{ 'X-CSRF-TOKEN' = $security.token }
$smokeUsername = 'smoke_' + [guid]::NewGuid().ToString('N').Substring(0,16)
$smokePassword = [guid]::NewGuid().ToString('N') + '-Check!'
function Send-SmokeRequest([string]$Method,[string]$Path,$Body) {
    $parameters = @{ Uri = "$ApiBaseUri/api/v1$Path"; Method = $Method; WebSession = $smokeSession; Headers = $smokeHeaders }
    if ($null -ne $Body) { $parameters.ContentType = 'application/json'; $parameters.Body = ConvertTo-Json -InputObject $Body -Depth 12 -Compress }
    Invoke-RestMethod @parameters
}
$credentials = @{ username = $smokeUsername; password = $smokePassword }
$null = Send-SmokeRequest POST '/auth/register' $credentials
$account = Send-SmokeRequest POST '/auth/login' $credentials
$security = Send-SmokeRequest GET '/auth/csrf' $null
$smokeHeaders['X-CSRF-TOKEN'] = $security.token
$runId = [guid]::NewGuid().ToString()
$null = Send-SmokeRequest POST '/runs' @{ id = $runId; storySlug = 'listening' }
$choice = @{ sequence = 1; nodeId = 'tired'; optionIndex = 0 }
$null = Send-SmokeRequest POST "/runs/$runId/choices" $choice
$retried = Send-SmokeRequest POST "/runs/$runId/choices" $choice
if ($retried.revision -ne 1 -or $retried.choices.Count -ne 1) { throw '重复选择未按预期去重' }
$complete = Send-SmokeRequest POST "/runs/$runId/choices" @{ sequence = 2; nodeId = 'time'; optionIndex = 0 }
$null = Send-SmokeRequest PUT '/me/favorites/TUTORIAL/emotional-validation' $null
$null = Send-SmokeRequest PUT '/me/read/emotional-validation' $null
$daily = Send-SmokeRequest GET '/daily' $null
$null = Send-SmokeRequest POST '/daily/attempts' @{ id = [guid]::NewGuid().ToString(); date = $daily.date; questionSlug = $daily.question.slug; version = $daily.question.version; optionIndex = 0 }
$me = Send-SmokeRequest GET '/me' $null
if ($complete.status -ne 'COMPLETE' -or $me.completedRuns -ne 1 -or $me.readTutorials -ne 1 -or $me.favorites -ne 1 -or $me.practiceDays -ne 1) { throw '学习记录与实际请求不一致' }
$null = Send-SmokeRequest POST '/auth/logout' $null
[pscustomobject]@{ Health = $health.status; Tutorials = $tutorials.total; CompletedRuns = $me.completedRuns; ReadTutorials = $me.readTutorials; Favorites = $me.favorites; PracticeDays = $me.practiceDays; TestAccountId = $account.id }
# Creates one isolated USER account and its sample records in the development database; no secrets are printed or saved.
