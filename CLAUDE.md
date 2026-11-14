# healmeout — Ekip Kuralları

Bu repoda 3 geliştirici, 3 ayrı bilgisayardan çalışır. Her bilgisayardaki ana AI
Claude Code + **Fable 5.2** (`claude-fable-5-2`) modelidir; model `.claude/settings.json`
içinde sabitlenmiştir. Bu kurallar hem insanlar hem AI için geçerlidir.

## Değişmez kurallar

- **`main`'e direkt push YASAK.** GitHub ruleset'i (`main-koruma`) bunu herkes için,
  repo sahibi dahil, sunucu tarafında engeller. Bypass yetkisi olan kimse yok.
- **Force push ve `main` silme YASAK.**
- Her değişiklik şu yoldan geçer: **branch → öz-inceleme → PR → squash merge.**
- Kuralları atlatmaya çalışmak yasak: `gh pr merge --admin`, ruleset'i kapatmak/değiştirmek,
  `--no-verify`. Kural dosyalarındaki (`CLAUDE.md`, `.claude/`, `.github/`) değişiklikler de
  normal PR sürecinden geçer.

## Branch

- Her iş için güncel `main`'den yeni branch aç:
  ```sh
  git switch main && git pull --ff-only
  git switch -c <github-kullanıcı-adı>/<tür>/<kısa-açıklama>
  ```
- `tür`: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`
- Örnek: `sener00/feat/giris-ekrani`, `yldzemin/fix/token-suresi`
- Bir branch = bir konu. PR'ları küçük tut.
- Başkasının branch'ine sahibine sormadan push etme.
- Commit mesajı: `tür: kısa açıklama` (örn. `fix: boş e-posta kontrolü`).

## 1. Öz-inceleme (PR açmadan önce, zorunlu)

PR'ı açan bilgisayarın Fable'ı kendi değişikliğini incelemeden PR açmaz.
Kısayol: **`/pr-ac`**.

1. `git fetch origin && git rebase origin/main`
2. `git diff origin/main...HEAD` baştan sona okunur: hata, güvenlik açığı, eksik hata
   yönetimi, gereksiz/ölü kod, eksik test.
3. Projede test/lint varsa çalıştırılır.
4. Bulunan sorunlar düzeltilir; düzeltilemeyen varsa PR açılmaz, kullanıcıya bildirilir.
5. PR açıklamasındaki "Öz-inceleme" bölümü doldurulur (`.github/pull_request_template.md`).

## 2. İnceleme (isteğe bağlı)

- Ruleset onay şartı koymaz; PR'ı açan kişi öz-incelemeden sonra birleştirebilir.
- İstenirse başka bir bilgisayarın Fable'ı inceleyebilir: **`/pr-incele <PR-numarası>`**.
  AI diff'i okur, mümkünse testleri çalıştırır; sorun bulursa **Request changes** verir.

## 3. Merge

- Sadece **squash merge**. PR sahibi birleştirir:
  ```sh
  gh pr merge <numara> --squash --delete-branch
  ```
- Merge sonrası: `git switch main && git pull --ff-only`

## Kurulum (her bilgisayarda bir kez)

1. `gh auth login` — her kişi kendi GitHub hesabıyla.
2. Repoyu klonla, Claude Code'u repo kökünde başlat. Model `claude-fable-5-2` olarak gelir;
   `/model` ile değiştirme.
3. `.claude/hooks/main-push-engeli.sh` AI'ın `main`'e push denemesini yerelde durdurur;
   asıl koruma GitHub ruleset'idir.

Ruleset `.github/rulesets/main-koruma.json` dosyasında tutulur. Değişiklik PR ile birleştikten
sonra repo admini `scripts/ruleset-uygula.sh` ile GitHub'a uygular.
