$ManifestPath = if ($env:MODEL_MANIFEST_PATH) {
    $env:MODEL_MANIFEST_PATH
} else {
    "models/models.manifest.json"
}

$Manifest = Get-Content -Raw -Path $ManifestPath | ConvertFrom-Json

function Get-ModelEntries {
    $entries = @(
        $Manifest.llm
        $Manifest.embedder
        $Manifest.embedder.tokenizer
    )

    return $entries | Where-Object { $null -ne $_ }
}

function Get-Sha256 {
    param([string]$Path)

    return (Get-FileHash -Algorithm SHA256 -Path $Path).Hash.ToLowerInvariant()
}

function Assert-ModelEntryFields {
    param(
        [object]$Entry,
        [string[]]$Fields
    )

    foreach ($field in $Fields) {
        if ($Entry.$field -is [string]) {
            $Entry.$field = $Entry.$field.Replace("`r", "")
        }
        if ([string]::IsNullOrWhiteSpace($Entry.$field)) {
            Write-Error "ERROR: incomplete model entry in $ManifestPath."
        }
    }
}

function Assert-LocalModelFile {
    param(
        [string]$Label,
        [string]$Path,
        [string]$ExpectedSha
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        Write-Error "ERROR: $Label is missing at $Path. Run downloadModels first."
    }

    $actualSha = Get-Sha256 -Path $Path
    $ExpectedSha = $ExpectedSha.Trim()
    if ($actualSha -ne $ExpectedSha) {
        Write-Error "ERROR: $Label hash mismatch at $Path. Expected: $ExpectedSha Actual: $actualSha"
    }
}
