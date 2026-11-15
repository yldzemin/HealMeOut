#!/usr/bin/env bash
# AI assistant PreToolUse hook: stops the AI's attempts to push to main directly, to force push,
# and to merge around the rules, locally.
# The real protection is the "main-koruma" ruleset on GitHub; this hook is only an early warning.

input="$(cat)"
if command -v jq >/dev/null 2>&1; then
  command_line="$(printf '%s' "$input" | jq -r '.tool_input.command // empty')"
elif command -v python3 >/dev/null 2>&1; then
  command_line="$(printf '%s' "$input" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("command",""))')"
else
  command_line="$input"
fi

block() {
  echo "BLOCKED: $1 Rule (CLAUDE.md): open a branch, self-review, open a PR." >&2
  exit 2
}

if printf '%s' "$command_line" | grep -Eq 'gh[[:space:]]+pr[[:space:]]+merge.*--admin'; then
  block "bypassing the rules with 'gh pr merge --admin' is forbidden."
fi

# No git push: nothing to check
if ! printf '%s' "$command_line" | grep -Eq '(^|[;&|[:space:]])git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+push([[:space:]]|$)'; then
  exit 0
fi

# Take what follows the last "git push", cut at the first ; & or |
args="$(printf '%s' "$command_line" | sed -E 's/.*git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+push//')"
args="${args%%[;&|]*}"

# --force, --force-with-lease, --force-if-includes; -f alone or combined (-uf); +refspec
if printf '%s' "$args" | grep -Eq '(^|[[:space:]])(--force[a-z-]*|-[a-zA-Z]*f[a-zA-Z]*|\+[^[:space:]]+)([[:space:]=]|$)'; then
  block "force push is forbidden (do not rebase a pushed branch; use git merge origin/main)."
fi

if printf '%s' "$args" | grep -Eq '(^|[[:space:]:+])(refs/heads/)?main([[:space:]]|$)'; then
  block "pushing to main directly is forbidden."
fi

if [ "$(git -C "${CLAUDE_PROJECT_DIR:-.}" branch --show-current 2>/dev/null)" = "main" ]; then
  block "you are on the main branch; switch to your own branch first."
fi

exit 0
