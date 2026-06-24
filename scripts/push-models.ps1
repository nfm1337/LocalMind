$ErrorActionPreference = "Stop"

. "$PSScriptRoot/lib/models.ps1"

$deviceRoot = if ($env:MODEL_DEVICE_ROOT) {
    ($env:MODEL_DEVICE_ROOT).TrimEnd("/")
} else {
    "/sdcard/Android/data/il.nfm.localmind/files"
}

function Get-RemotePath {
    param([string]$DevicePath)

    if ($DevicePath.StartsWith("/")) {
        return $DevicePath
    }

    return "$deviceRoot/$DevicePath"
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

    $hashOutput = (& adb shell sha256sum $RemotePath 2>$null)
    if ([string]::IsNullOrWhiteSpace($hashOutput)) {
        $hashOutput = (& adb shell toybox sha256sum $RemotePath 2>$null)
    }

    if ([string]::IsNullOrWhiteSpace($hashOutput)) {
        Write-Error "ERROR: could not read device hash for $Label at $RemotePath."
    }

    $actualSha = ($hashOutput -split "\s+")[0].Trim().ToLowerInvariant()
    if ($actualSha -ne $ExpectedSha) {
        Write-Error "ERROR: $Label device hash mismatch at $RemotePath. Expected: $ExpectedSha Actual: $actualSha"
    }
}

if ($null -eq (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Error "ERROR: adb is not installed or not on PATH."
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
    & adb shell mkdir -p $remoteDirectory
    & adb push $entry.localPath $remotePath | Out-Null
    Verify-DeviceFile `
        -Label $entry.fileName `
        -RemotePath $remotePath `
        -ExpectedSha $entry.sha256
    Write-Host "$($entry.fileName) pushed and verified."
}

Write-Host "Done."
