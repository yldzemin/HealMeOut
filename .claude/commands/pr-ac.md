---
description: Öz-inceleme yap, branch'i push et ve PR aç
argument-hint: "[PR başlığı]"
---

Mevcut branch için PR açma sürecini `CLAUDE.md`'deki kurallara göre yürüt:

1. Mevcut branch `main` ise dur: önce `<kullanıcı>/<tür>/<açıklama>` biçiminde branch açılmalı.
2. Commitlenmemiş değişiklik varsa ne yapılacağını kullanıcıya sor.
3. `git fetch origin` ve `git rebase origin/main`. Çakışma çıkarsa kullanıcıyla birlikte çöz.
4. Öz-inceleme: `git diff origin/main...HEAD` çıktısını baştan sona oku. Hata, güvenlik açığı,
   eksik hata yönetimi, gereksiz/ölü kod ve eksik test ara. Projede test/lint varsa çalıştır.
5. Bulduğun sorunları düzeltip commitle. Düzeltemediğin engelleyici bir sorun varsa PR açma,
   kullanıcıya bildir ve dur.
6. `git push -u origin HEAD`
7. `gh pr create --base main` ile PR aç. Başlık: $ARGUMENTS (boşsa değişiklikten türet).
   Gövdeyi `.github/pull_request_template.md` şablonuna göre doldur; "Öz-inceleme" bölümüne
   neyi kontrol ettiğini, ne bulduğunu ve neyi düzelttiğini yaz.
8. PR linkini ver. Birleştirmek için `gh pr merge <numara> --squash --delete-branch` komutunu,
   istenirse diğer bilgisayarlarda `/pr-incele <numara>` ile inceleme alınabileceğini söyle.
