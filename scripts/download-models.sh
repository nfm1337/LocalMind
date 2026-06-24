#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/models.sh
source "$script_dir/lib/models.sh"

mkdir -p models/llm
mkdir -p models/embedding

download_or_skip() {
    local label="$1"
    local url="$2"
    local path="$3"
    local expected_sha="$4"
    local actual_sha
    local tmp_path

    if [[ -f "$path" ]]; then
        actual_sha="$(sha256 "$path")"
        if [[ "$actual_sha" == "$expected_sha" ]]; then
            echo "$label already downloaded."
            return
        fi
        echo "$label exists but hash does not match; downloading again."
    else
        echo "Downloading $label..."
    fi

    tmp_path="${path}.download"
    curl -fL "$url" -o "$tmp_path"

    actual_sha="$(sha256 "$tmp_path")"
    if [[ "$actual_sha" != "$expected_sha" ]]; then
        rm -f "$tmp_path"
        echo "ERROR: $label hash mismatch." >&2
        echo "Expected: $expected_sha" >&2
        echo "Actual:   $actual_sha" >&2
        exit 1
    fi

    mv "$tmp_path" "$path"
    echo "$label downloaded and verified."
}

while IFS=$'\t' read -r file_name url local_path expected_sha; do
    require_fields "$file_name" "$file_name" "$url" "$local_path" "$expected_sha"

    mkdir -p "$(dirname "$local_path")"
    download_or_skip "$file_name" "$url" "$local_path" "$expected_sha"
done < <(read_manifest_entries "fileName,url,localPath,sha256")

echo "Done."
