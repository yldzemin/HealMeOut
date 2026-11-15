#!/usr/bin/env bash
# Applies .github/rulesets/main-koruma.json to GitHub (updates the ruleset if it exists).
# Needs repo admin rights. Usage: scripts/apply-ruleset.sh [owner/repo]
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
REPO="${1:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"
FILE=".github/rulesets/main-koruma.json"
NAME="main-koruma"

ID="$(gh api "repos/$REPO/rulesets" --jq ".[] | select(.name == \"$NAME\") | .id")"
if [ -n "$ID" ]; then
  gh api -X PUT "repos/$REPO/rulesets/$ID" --input "$FILE" >/dev/null
  echo "Updated: $NAME (#$ID) -> $REPO"
else
  gh api -X POST "repos/$REPO/rulesets" --input "$FILE" >/dev/null
  echo "Created: $NAME -> $REPO"
fi

echo "Active rules on main:"
gh api "repos/$REPO/rules/branches/main" --jq '.[] | "  - \(.type)"'
