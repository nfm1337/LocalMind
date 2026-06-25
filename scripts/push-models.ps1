$ErrorActionPreference = "Stop"

. "$PSScriptRoot/lib/models.ps1"

$appPackage = if ($env:MODEL_APP_PACKAGE) {
    $env:MODEL_APP_PACKAGE
} else {
    "il.nfm.localmind"
}

$deviceRoot = if ($env:MODEL_DEVICE_ROOT) {
    ($env:MODEL_DEVICE_ROOT).TrimEnd("/")
} else {
    ""
}

$staleTempRoot = "/data/local/tmp/localmind-models"

function Get-RemotePath {
    param([string]$DevicePath)

    if ($DevicePath.StartsWith("/")) {
        return $DevicePath
    }

    if (-not [string]::IsNullOrWhiteSpace($deviceRoot)) {
        return "$deviceRoot/$DevicePath"
    }

    return "files/$DevicePath"
}

function Get-RemoteDirectory {
    param([string]$RemotePath)

    $lastSlash = $RemotePath.LastIndexOf("/")
    if ($lastSlash -lt 0) {
        return "."
    }

    return $RemotePath.Substring(0, $lastSlash)
}

function Verify-DeviceFile {
    param(
        [string]$Label,
        [string]$RemotePath,
        [string]$ExpectedSha
    )

    $ExpectedSha = $ExpectedSha.Trim()
    $actualSha = Get-DeviceSha -RemotePath $RemotePath
    if ($actualSha -ne $ExpectedSha) {
        Write-Error "ERROR: $Label device hash mismatch at $RemotePath. Expected: $ExpectedSha Actual: $actualSha"
    }
}

function Get-DeviceSha {
    param([string]$RemotePath)

    if ((-not [string]::IsNullOrWhiteSpace($deviceRoot)) -or $RemotePath.StartsWith("/")) {
        $hashOutput = (& adb shell sha256sum $RemotePath 2>$null)
        if ([string]::IsNullOrWhiteSpace($hashOutput)) {
            $hashOutput = (& adb shell toybox sha256sum $RemotePath 2>$null)
        }
    } else {
        $hashOutput = (& adb shell run-as $appPackage sha256sum $RemotePath 2>$null)
        if ([string]::IsNullOrWhiteSpace($hashOutput)) {
            $hashOutput = (& adb shell run-as $appPackage toybox sha256sum $RemotePath 2>$null)
        }
    }

    if ([string]::IsNullOrWhiteSpace($hashOutput)) {
        return ""
    }

    return ($hashOutput -split "\s+")[0].Trim().ToLowerInvariant()
}

function Test-DeviceFileMatches {
    param(
        [string]$RemotePath,
        [string]$ExpectedSha
    )

    $actualSha = Get-DeviceSha -RemotePath $RemotePath
    return (-not [string]::IsNullOrWhiteSpace($actualSha)) -and ($actualSha -eq $ExpectedSha.Trim())
}

function Push-ModelFile {
    param(
        [string]$LocalPath,
        [string]$RemotePath,
        [string]$RemoteDirectory
    )

    if ((-not [string]::IsNullOrWhiteSpace($deviceRoot)) -or $RemotePath.StartsWith("/")) {
        & adb shell mkdir -p $RemoteDirectory
        & adb push $LocalPath $RemotePath | Out-Null
    } else {
        & adb shell run-as $appPackage mkdir -p $RemoteDirectory
        Copy-FileToAppSandbox `
            -LocalPath $LocalPath `
            -RemotePath $RemotePath
    }
}

function Copy-FileToAppSandbox {
    param(
        [string]$LocalPath,
        [string]$RemotePath
    )

    $tempPath = "$RemotePath.push"
    $command = "run-as $appPackage sh -c 'rm -f `"$RemotePath`" `"$tempPath`" && cat > `"$tempPath`" && mv `"$tempPath`" `"$RemotePath`"'"

    $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = "adb"
    $processInfo.Arguments = "shell -T `"$command`""
    $processInfo.RedirectStandardInput = $true
    $processInfo.UseShellExecute = $false

    $process = [System.Diagnostics.Process]::Start($processInfo)
    try {
        $inputStream = [System.IO.File]::OpenRead($LocalPath)
        try {
            $inputStream.CopyTo($process.StandardInput.BaseStream)
        } finally {
            $inputStream.Dispose()
            $process.StandardInput.Dispose()
        }

        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            Write-Error "ERROR: adb shell -T failed while pushing $LocalPath to $RemotePath."
        }
    } finally {
        $process.Dispose()
    }
}

if ($null -eq (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Error "ERROR: adb is not installed or not on PATH."
}

if ([string]::IsNullOrWhiteSpace($deviceRoot)) {
    & adb shell run-as $appPackage pwd | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "ERROR: run-as failed for $appPackage. Install a debuggable app build before running pushModels."
    }
    & adb shell rm -rf $staleTempRoot
    & adb shell run-as $appPackage mkdir -p files
}

foreach ($entry in (Get-ModelEntries)) {
    Assert-ModelEntryFields `
        -Entry $entry `
        -Fields @("fileName", "localPath", "devicePath", "sha256")

    Assert-LocalModelFile `
        -Label $entry.fileName `
        -Path $entry.localPath `
        -ExpectedSha $entry.sha256

    $remotePath = Get-RemotePath -DevicePath $entry.devicePath
    $remoteDirectory = Get-RemoteDirectory -RemotePath $remotePath

    if (Test-DeviceFileMatches -RemotePath $remotePath -ExpectedSha $entry.sha256) {
        Write-Host "$($entry.fileName) already pushed."
        continue
    }

    Write-Host "Pushing $($entry.fileName)..."
    Push-ModelFile `
        -LocalPath $entry.localPath `
        -RemotePath $remotePath `
        -RemoteDirectory $remoteDirectory
    Verify-DeviceFile `
        -Label $entry.fileName `
        -RemotePath $remotePath `
        -ExpectedSha $entry.sha256
    Write-Host "$($entry.fileName) pushed and verified."
}

Write-Host "Done."
