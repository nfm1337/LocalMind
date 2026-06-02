#!/bin/sh

commit_msg=$(cat "$1")
pattern='^(feat|fix|refactor|chore|docs|test|style|ci)(\(.+\))?: .{1,72}$'

if ! echo "$commit_msg" | grep -qE "$pattern"; then
  echo "Commit message must follow Conventional Commits:"
  echo "  feat(scope): description"
  echo "  fix: description"
  echo "Got: $commit_msg"
  exit 1
fi
