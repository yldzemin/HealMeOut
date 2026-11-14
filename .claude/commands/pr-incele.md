---
description: Başka bir bilgisayarın PR'ını Fable ile incele; onayla ya da değişiklik iste
argument-hint: "<PR numarası>"
---

PR #$ARGUMENTS'ı `CLAUDE.md`'deki kurallara göre incele:

1. `gh pr view $ARGUMENTS` ve `gh pr diff $ARGUMENTS` ile açıklamayı ve diff'i oku.
   PR'ı açan kişi bu bilgisayardaki hesapsa (`gh api user --jq .login`) dur: kendi PR'ını onaylayamazsın.
2. Hedef branch'in `main`, kaynak branch adının kurallara uygun olduğunu ve PR açıklamasında
   öz-inceleme bölümünün doldurulduğunu kontrol et. Eksikse değişiklik iste.
3. Çalışma ağacı temizse `gh pr checkout $ARGUMENTS` ile branch'e geç, test/lint varsa çalıştır,
   iş bitince önceki branch'e geri dön. Ağaç temiz değilse checkout etme, sadece diff'le devam et.
4. Diff'i baştan sona incele: doğruluk, güvenlik, hata yönetimi, okunabilirlik, test.
5. Karar ver:
   - Engelleyici sorun yoksa: `gh pr review $ARGUMENTS --approve --body "<kısa inceleme özeti>"`
   - Engelleyici sorun varsa: `gh pr review $ARGUMENTS --request-changes --body "<sorunlar, dosya:satır ile>"`
   - Küçük, engelleyici olmayan önerileri onay gövdesine yaz.
   - Emin değilsen onaylama; durumu kullanıcıya anlat ve sor.
6. Verdiğin kararı ve gerekçesini kullanıcıya özetle.
