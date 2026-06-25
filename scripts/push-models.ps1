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
        Write-Error "ERROR: could not read device hash for $Label at $RemotePath."
    }

    $actualSha = ($hashOutput -split "\s+")[0].Trim().ToLowerInvariant()
    if ($actualSha -ne $ExpectedSha) {
        Write-Error "ERROR: $Label device hash mismatch at $RemotePath. Expected: $ExpectedSha Actual: $actualSha"
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

    Write-Host "Pushing $($entry.fileName)..."
    if ((-not [string]::IsNullOrWhiteSpace($deviceRoot)) -or $remotePath.StartsWith("/")) {
        & adb shell mkdir -p $remoteDirectory
        & adb push $entry.localPath $remotePath | Out-Null
    } else {
        & adb shell run-as $appPackage mkdir -p $remoteDirectory
        Copy-FileToAppSandbox `
            -LocalPath $entry.localPath `
            -RemotePath $remotePath
    }
    Verify-DeviceFile `
        -Label $entry.fileName `
        -RemotePath $remotePath `
        -ExpectedSha $entry.sha256
    Write-Host "$($entry.fileName) pushed and verified."
}

Write-Host "Done."
