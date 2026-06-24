$ErrorActionPreference = "Stop"

. "$PSScriptRoot/lib/models.ps1"

New-Item -ItemType Directory -Force -Path "models/llm" | Out-Null
New-Item -ItemType Directory -Force -Path "models/embedding" | Out-Null

function Download-OrSkip {
    param(
        [string]$Label,
        [string]$Url,
        [string]$Path,
        [string]$ExpectedSha
    )

    if (Test-Path -LiteralPath $Path) {
        $actualSha = Get-Sha256 -Path $Path
        if ($actualSha -eq $ExpectedSha) {
            Write-Host "$Label already downloaded."
            return
        }

        Write-Host "$Label exists but hash does not match; downloading again."
    } else {
        Write-Host "Downloading $Label..."
    }

    $tmpPath = "$Path.download"
    Invoke-WebRequest -Uri $Url -OutFile $tmpPath

    $actualSha = Get-Sha256 -Path $tmpPath
    if ($actualSha -ne $ExpectedSha) {
        Remove-Item -Force -LiteralPath $tmpPath
        Write-Error "ERROR: $Label hash mismatch. Expected: $ExpectedSha Actual: $actualSha"
    }

    Move-Item -Force -LiteralPath $tmpPath -Destination $Path
    Write-Host "$Label downloaded and verified."
}

foreach ($entry in (Get-ModelEntries)) {
    Assert-ModelEntryFields `
        -Entry $entry `
        -Fields @("fileName", "url", "localPath", "sha256")

    $directory = Split-Path -Parent $entry.localPath
    if (-not [string]::IsNullOrWhiteSpace($directory)) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }

    Download-OrSkip `
        -Label $entry.fileName `
        -Url $entry.url `
        -Path $entry.localPath `
        -ExpectedSha $entry.sha256
}

Write-Host "Done."
