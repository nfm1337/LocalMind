#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/models.sh
source "$script_dir/lib/models.sh"

app_package="${MODEL_APP_PACKAGE:-il.nfm.localmind}"
device_root="${MODEL_DEVICE_ROOT:-}"
device_root="${device_root%/}"
stale_temp_root="/data/local/tmp/localmind-models"

remote_path_for() {
    local device_path="$1"

    if [[ "$device_path" == /* ]]; then
        echo "$device_path"
    elif [[ -n "$device_root" ]]; then
        echo "$device_root/$device_path"
    else
        echo "files/$device_path"
    fi
}

verify_device_file() {
    local label="$1"
    local remote_path="$2"
    local expected_sha="$3"
    local actual_sha

    actual_sha="$(device_sha "$remote_path")"
    expected_sha="${expected_sha//$'\r'/}"

    if [[ "$actual_sha" != "$expected_sha" ]]; then
        echo "ERROR: $label device hash mismatch at $remote_path." >&2
        echo "Expected: $expected_sha" >&2
        echo "Actual:   ${actual_sha:-<missing>}" >&2
        exit 1
    fi
}

device_sha() {
    local remote_path="$1"
    local actual_sha

    if [[ -n "$device_root" || "$remote_path" == /* ]]; then
        actual_sha="$(adb shell sha256sum "$remote_path" </dev/null 2>/dev/null | awk '{print $1}' | tr -d '\r' || true)"
        if [[ -z "$actual_sha" ]]; then
            actual_sha="$(adb shell toybox sha256sum "$remote_path" </dev/null 2>/dev/null | awk '{print $1}' | tr -d '\r' || true)"
        fi
    else
        actual_sha="$(adb shell run-as "$app_package" sha256sum "$remote_path" </dev/null 2>/dev/null | awk '{print $1}' | tr -d '\r' || true)"
        if [[ -z "$actual_sha" ]]; then
            actual_sha="$(adb shell run-as "$app_package" toybox sha256sum "$remote_path" </dev/null 2>/dev/null | awk '{print $1}' | tr -d '\r' || true)"
        fi
    fi

    echo "$actual_sha"
}

device_file_matches() {
    local remote_path="$1"
    local expected_sha="$2"
    local actual_sha

    expected_sha="${expected_sha//$'\r'/}"
    actual_sha="$(device_sha "$remote_path")"
    [[ -n "$actual_sha" && "$actual_sha" == "$expected_sha" ]]
}

push_file() {
    local local_path="$1"
    local remote_path="$2"
    local remote_dir="$3"

    if [[ -n "$device_root" || "$remote_path" == /* ]]; then
        adb shell mkdir -p "$remote_dir" </dev/null
        adb push "$local_path" "$remote_path" </dev/null >/dev/null
    else
        adb shell run-as "$app_package" mkdir -p "$remote_dir" </dev/null
        push_to_app_sandbox "$local_path" "$remote_path"
    fi
}

push_to_app_sandbox() {
    local local_path="$1"
    local remote_path="$2"
    local temp_path="$remote_path.push"

    adb shell -T "run-as $app_package sh -c 'rm -f \"$remote_path\" \"$temp_path\" && cat > \"$temp_path\" && mv \"$temp_path\" \"$remote_path\"'" < "$local_path"
}

if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: adb is not installed or not on PATH." >&2
    exit 1
fi

if [[ -z "$device_root" ]]; then
    if ! adb shell run-as "$app_package" pwd </dev/null >/dev/null 2>&1; then
        echo "ERROR: run-as failed for $app_package. Install a debuggable app build before running pushModels." >&2
        exit 1
    fi
    adb shell rm -rf "$stale_temp_root" </dev/null
    adb shell run-as "$app_package" mkdir -p files </dev/null
fi

while IFS=$'\t' read -r file_name local_path device_path expected_sha; do
    file_name="${file_name//$'\r'/}"
    local_path="${local_path//$'\r'/}"
    device_path="${device_path//$'\r'/}"
    expected_sha="${expected_sha//$'\r'/}"

    require_fields "$file_name" "$file_name" "$local_path" "$device_path" "$expected_sha"

    verify_local_file "$file_name" "$local_path" "$expected_sha"

    remote_path="$(remote_path_for "$device_path")"
    remote_dir="$(dirname "$remote_path")"

    if device_file_matches "$remote_path" "$expected_sha"; then
        echo "$file_name already pushed."
        continue
    fi

    echo "Pushing $file_name..."
    push_file "$local_path" "$remote_path" "$remote_dir"
    verify_device_file "$file_name" "$remote_path" "$expected_sha"
    echo "$file_name pushed and verified."
done < <(read_manifest_entries "fileName,localPath,devicePath,sha256")

echo "Done."
