#!/usr/bin/env bash
# Claude Code PreToolUse hook'u: AI'ın main'e direkt push, force push ve
# kuralları atlayan merge denemelerini yerelde durdurur.
# Asıl koruma GitHub'daki "main-koruma" ruleset'idir; bu hook sadece erken uyarıdır.

girdi="$(cat)"
if command -v jq >/dev/null 2>&1; then
  komut="$(printf '%s' "$girdi" | jq -r '.tool_input.command // empty')"
elif command -v python3 >/dev/null 2>&1; then
  komut="$(printf '%s' "$girdi" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("command",""))')"
else
  komut="$girdi"
fi

engelle() {
  echo "ENGELLENDİ: $1 Kural (CLAUDE.md): branch aç, öz-inceleme yap, PR aç, 2 onay bekle." >&2
  exit 2
}

if printf '%s' "$komut" | grep -Eq 'gh[[:space:]]+pr[[:space:]]+merge.*--admin'; then
  engelle "'gh pr merge --admin' ile kuralları atlamak yasak."
fi

# git push yoksa karışma
if ! printf '%s' "$komut" | grep -Eq '(^|[;&|[:space:]])git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+push([[:space:]]|$)'; then
  exit 0
fi

# Son "git push"tan sonrasını al, ilk ; & | karakterinde kes
arguman="$(printf '%s' "$komut" | sed -E 's/.*git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+push//')"
arguman="${arguman%%[;&|]*}"

if printf '%s' "$arguman" | grep -Eq '(^|[[:space:]])(-f|--force[a-z-]*)([[:space:]=]|$)'; then
  engelle "force push yasak."
fi

if printf '%s' "$arguman" | grep -Eq '(^|[[:space:]:+])(refs/heads/)?main([[:space:]]|$)'; then
  engelle "main'e direkt push yasak."
fi

if [ "$(git -C "${CLAUDE_PROJECT_DIR:-.}" branch --show-current 2>/dev/null)" = "main" ]; then
  engelle "şu an main branch'indesin; önce kendi branch'ine geç."
fi

exit 0
