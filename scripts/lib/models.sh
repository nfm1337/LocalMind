#!/usr/bin/env bash

manifest_path="${MODEL_MANIFEST_PATH:-models/models.manifest.json}"

sha256() {
    shasum -a 256 "$1" | awk '{print $1}'
}

read_manifest_entries() {
    local fields="$1"

    if command -v jq >/dev/null 2>&1; then
        jq -r --arg fields "$fields" '
            def model_entries:
                .llm,
                .embedder,
                .embedder.tokenizer;

            ($fields | split(",")) as $keys
            | model_entries
            | select(. != null)
            | [$keys[] as $key | .[$key]]
            | @tsv
        ' "$manifest_path"
        return
    fi

    if command -v python3 >/dev/null 2>&1; then
        python3 - "$manifest_path" "$fields" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as f:
    manifest = json.load(f)

keys = sys.argv[2].split(",")
entries = [
    manifest.get("llm"),
    manifest.get("embedder"),
    manifest.get("embedder", {}).get("tokenizer"),
]

for entry in entries:
    if entry:
        print("\t".join(str(entry.get(key, "")) for key in keys))
PY
        return
    fi

    echo "ERROR: install jq or python3 to read $manifest_path." >&2
    exit 1
}

require_fields() {
    local entry_name="$1"
    shift

    for value in "$@"; do
        if [[ -z "$value" ]]; then
            echo "ERROR: incomplete model entry for $entry_name in $manifest_path." >&2
            exit 1
        fi
    done
}

verify_local_file() {
    local label="$1"
    local path="$2"
    local expected_sha="$3"
    local actual_sha

    if [[ ! -f "$path" ]]; then
        echo "ERROR: $label is missing at $path. Run downloadModels first." >&2
        exit 1
    fi

    actual_sha="$(sha256 "$path")"
    if [[ "$actual_sha" != "$expected_sha" ]]; then
        echo "ERROR: $label hash mismatch at $path." >&2
        echo "Expected: $expected_sha" >&2
        echo "Actual:   $actual_sha" >&2
        exit 1
    fi
}
