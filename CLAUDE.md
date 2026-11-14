# healmeout — Ekip Kuralları

Bu repoda 3 geliştirici, 3 ayrı bilgisayardan çalışır. Her bilgisayardaki ana AI asistanı
**Fable 5.2** modelidir; model `.claude/settings.json`
içinde sabitlenmiştir. Bu kurallar hem insanlar hem AI için geçerlidir.

Proje kuralları, hackathon kuralları ve GitHub çalışma düzeni (issue, PR onayı, yorumlar)
proje hafızasındadır:

@CLAUDE_HAFIZASI.md

## Değişmez kurallar

- **`main`'e direkt push YASAK.** GitHub ruleset'i (`main-koruma`) bunu herkes için,
  repo sahibi dahil, sunucu tarafında engeller. Bypass yetkisi olan kimse yok.
- **Force push ve `main` silme YASAK.**
- **Proje dili %100 İngilizce:** kod, yorumlar, dokümanlar, commit, dal adı, issue, PR ve
  review yorumları İngilizce yazılır. AI asistanı kendi kullanıcısıyla Türkçe konuşur.
- **AI aracının adı GitHub'a yazılmaz (#44):** commit, PR, issue ve yorumlarda araç adı, imza,
  co-author satırı veya "Generated with …" satırı olmaz. YZ kullanımı teslim formunda beyan edilir.
- Her değişiklik şu yoldan geçer: **branch → öz-inceleme → PR → squash merge.**
- Kuralları atlatmaya çalışmak yasak: `gh pr merge --admin`, ruleset'i kapatmak/değiştirmek,
  `--no-verify`. Kural dosyalarındaki (`CLAUDE.md`, `.claude/`, `.github/`) değişiklikler de
  normal PR sürecinden geçer.

## Branch

- Her iş için güncel `main`'den yeni branch aç:
  ```sh
  git switch main && git pull --rebase
  git switch -c <tür>/<konu>
  ```
- Dal adları: `feat/<modül>`, `fix/<konu>`, `docs/<konu>` (örn. `feat/scenario-simulator`).
- Bir branch = bir konu. PR'ları küçük tut.
- Başkasının branch'ine sahibine sormadan push etme.
- Commit mesajı İngilizce: `tür: kısa açıklama` (örn. `fix: reject empty email`).

## 1. Öz-inceleme (PR açmadan önce, zorunlu)

PR'ı açan bilgisayarın Fable'ı kendi değişikliğini incelemeden PR açmaz.
Kısayol: **`/pr-ac`**.

1. `git fetch origin && git rebase origin/main`
2. `git diff origin/main...HEAD` baştan sona okunur: hata, güvenlik açığı, eksik hata
   yönetimi, gereksiz/ölü kod, eksik test.
3. Projede test/lint varsa çalıştırılır.
4. Bulunan sorunlar düzeltilir; düzeltilemeyen varsa PR açılmaz, kullanıcıya bildirilir.
5. PR açıklamasındaki "Self-review" bölümü İngilizce doldurulur (`.github/pull_request_template.md`).

## 2. İnceleme

- Ruleset onay şartı koymaz; onay ve inceleme kuralları `CLAUDE_HAFIZASI.md` §6'dadır.
- Kısayol: **`/pr-incele <PR-numarası>`**. AI diff'i okur, mümkünse testleri çalıştırır;
  sorun bulursa **Request changes** verir.

## 3. Merge

- Sadece **squash merge**. PR sahibi birleştirir:
  ```sh
  gh pr merge <numara> --squash --delete-branch
  ```
- Merge sonrası: `git switch main && git pull --ff-only`

## Kurulum (her bilgisayarda bir kez)

1. `gh auth login` — her kişi kendi GitHub hesabıyla.
2. Repoyu klonla, AI asistanını repo kökünde başlat. Model `.claude/settings.json`'dan gelir;
   `/model` ile değiştirme.
3. `.claude/hooks/main-push-engeli.sh` AI'ın `main`'e push denemesini yerelde durdurur;
   asıl koruma GitHub ruleset'idir.

Ruleset `.github/rulesets/main-koruma.json` dosyasında tutulur. Değişiklik PR ile birleştikten
sonra repo admini `scripts/ruleset-uygula.sh` ile GitHub'a uygular.
