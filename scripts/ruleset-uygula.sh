#!/usr/bin/env bash
# .github/rulesets/main-koruma.json dosyasını GitHub'a uygular (varsa günceller).
# Repo admin yetkisi gerekir. Kullanım: scripts/ruleset-uygula.sh [sahip/repo]
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
REPO="${1:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"
DOSYA=".github/rulesets/main-koruma.json"
AD="main-koruma"

ID="$(gh api "repos/$REPO/rulesets" --jq ".[] | select(.name == \"$AD\") | .id")"
if [ -n "$ID" ]; then
  gh api -X PUT "repos/$REPO/rulesets/$ID" --input "$DOSYA" >/dev/null
  echo "Güncellendi: $AD (#$ID) -> $REPO"
else
  gh api -X POST "repos/$REPO/rulesets" --input "$DOSYA" >/dev/null
  echo "Oluşturuldu: $AD -> $REPO"
fi

echo "main üzerinde aktif kurallar:"
gh api "repos/$REPO/rules/branches/main" --jq '.[] | "  - \(.type)"'
