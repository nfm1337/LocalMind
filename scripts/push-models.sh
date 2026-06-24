#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/models.sh
source "$script_dir/lib/models.sh"

device_root="${MODEL_DEVICE_ROOT:-/sdcard/Android/data/il.nfm.localmind/files}"
device_root="${device_root%/}"

remote_path_for() {
    local device_path="$1"

    if [[ "$device_path" == /* ]]; then
        echo "$device_path"
    else
        echo "$device_root/$device_path"
    fi
}

verify_device_file() {
    local label="$1"
    local remote_path="$2"
    local expected_sha="$3"
    local actual_sha

    actual_sha="$(adb shell sha256sum "$remote_path" 2>/dev/null | awk '{print $1}' | tr -d '\r')"
    if [[ -z "$actual_sha" ]]; then
        actual_sha="$(adb shell toybox sha256sum "$remote_path" 2>/dev/null | awk '{print $1}' | tr -d '\r')"
    fi

    if [[ "$actual_sha" != "$expected_sha" ]]; then
        echo "ERROR: $label device hash mismatch at $remote_path." >&2
        echo "Expected: $expected_sha" >&2
        echo "Actual:   ${actual_sha:-<missing>}" >&2
        exit 1
    fi
}

if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: adb is not installed or not on PATH." >&2
    exit 1
fi

while IFS=$'\t' read -r file_name local_path device_path expected_sha; do
    require_fields "$file_name" "$file_name" "$local_path" "$device_path" "$expected_sha"

    verify_local_file "$file_name" "$local_path" "$expected_sha"

    remote_path="$(remote_path_for "$device_path")"
    remote_dir="$(dirname "$remote_path")"

    echo "Pushing $file_name..."
    adb shell mkdir -p "$remote_dir"
    adb push "$local_path" "$remote_path" >/dev/null
    verify_device_file "$file_name" "$remote_path" "$expected_sha"
    echo "$file_name pushed and verified."
done < <(read_manifest_entries "fileName,localPath,devicePath,sha256")

echo "Done."
