# CLAUDE.md — ASTRA-TWIN (NASA Space Apps Challenge 2026)

Bu dosya, bu projede çalışan Claude için kuralları ve çalışma metodolojisini tanımlar. Projenin tasarımı (konsept, mimari, arayüz, tıbbi değerler, veri kaynakları) **DESIGN.md**'dedir. Kod yazmadan veya tasarımla ilgili bir karar vermeden önce DESIGN.md'yi oku.

Kullanıcıyla Türkçe konuş. Teslim içeriği (UI metinleri, README, demo altyazısı, commit mesajları) İngilizce olacak, çünkü Space Apps kuralı bunu istiyor.

> **Son güncelleme:** 2026-11-14 (hackathon 1. gün)
> **Açık doğrulama:** 2026 Project Submission ve Judging & Awards rehberleri 13 Kasım'da yayınlandı, ancak site JavaScript ile oluşturulduğu için okunamadı. §2 şu an **2025 rehberine** dayanıyor. Teslimden önce rehberleri tarayıcıyla aç, farkları buraya işle.

## 1. Proje

**ASTRA-TWIN:** Mars görevindeki astronotlar için sağlık izleme ve dijital ikiz aracı. Karar vermez, ekip doktoruna (CMO) karar desteği sunar. Ayrıntılar DESIGN.md §1–2'de.

## 2. Hackathon kuralları (kritik)

Kaynak: 2025 Project Submission Guide (3 Ekim 2025). 2026 sürümüyle karşılaştırılacak.

- **Tarih:** 14–15 Kasım 2026. Teslim 14 Kasım 09:00'da açılır, 15 Kasım 23:59'da kapanır. Saat, Team Owner'ın Local Event'inin yerel saatidir (Universal Event'te Team Owner'ın yaşadığı yerin saati).
- **Veri kuralı (ekip kararı):** Yalnızca **NASA menşeli** veri kullanılır. NOAA veya başka ajans verisi yok. NASA verisi projenin çekirdek bileşeni olmalı; Relevance kriteri bunu "superficial addition" olmamalı diye tarif ediyor.
- **Demo:** En fazla 30 sn video veya başlık dahil en fazla 7 slayt. Link şifre, izin veya kayıt istememeli. Video İngilizce altyazılı, slaytlar İngilizce olmalı.
- **YZ (AI) kullanımı:** Serbest, ama teslimdeki "Use of Artificial Intelligence" alanında açıklanmak zorunda.
  - Üretilen içerik NASA logosu veya NASA tasarım öğesi içeremez ve onları değiştiremez.
  - YZ ile üretilen görsel ve videolarda görünür filigran olmalı.
  - YZ ile üretilen ses, kod ve veri; açıklama metninde ve metadata'da belirtilmeli.
  - Bu projede kodu Claude yazıyor; teslimde bunu belirt.
- **Teslim formu alanları:** Project Name, High-Level Project Summary, Link to Project Demo, Link to Final Project, Detailed Project Description, NASA Data, Space Agency Partner & Other Data, Use of Artificial Intelligence, Project Submission Verification. Tüm alanlar İngilizce olmalı.
- **Jüri kriterleri** (2025): Impact, Creativity, Validity, Relevance, Presentation. Presentation diğerleriyle eşit ağırlıkta düşünülmeli.

## 3. Dosya yapısı

```
CLAUDE.md                                     ← bu dosya: kurallar ve çalışma metodolojisi
DESIGN.md                                     ← proje tasarımı: konsept, mimari, UI, tıbbi değerler, veri
ARASTIRMA_RAPORU.md                           ← doğrulanmış araştırmanın tamamı (kaynaklarıyla)
(çevrimiçi) Proje Kapsam Dokümanı             ← ekip için tam kapsam, 6 senaryo, API'ler: https://claude.ai/artifact/V9p1PiJczNXqhjp5WoEpUT
ASTRA-TWIN — Proje Kapsam Dokümanı.md / .pdf  ← kapsam dokümanının yerel kopyası
Seminer Raporu -space app.docx                ← seminer raporu
ESKI_CLAUDE_2026-11-09.md                     ← eski CLAUDE.md (9 Kasım). DESIGN.md gelene kadar "kodda KULLANMA" tablosunun tek kopyası (#5)
nasa space apps araştırma sonucu olasılıklar.docx  ← ilk taslak spec (hatalı değerler var, bkz. DESIGN.md §4)
nasa örnek siteler araştırma.pdf              ← ilk kaynak listesi (OSDR ID'lerinde hatalar var, bkz. DESIGN.md §5)
57110f2f9bef1ff11b47b3e897d3eee2.jpg          ← UI estetik referansı
data/osdr/                                    ← NASA OSDR'den indirilmiş gerçek Inspiration4 verisi
data/cache/                                   ← DONKI ve Horizons önbelleği (internetsiz demo için)
kaynaklar/                                    ← NASA teknik dokümanları (OCHMO brief'leri, HRP RAP, M2M HHP)
```

Kod 14 Kasım'da sıfırdan yazılır. Repo bilgisi §6'da.

## 4. Çalışma kuralları

- **Varsayım yapma.** Her sayısal eşik ve tıbbi iddia için bir kaynak göster. Kaynak yoksa "doğrulanamadı" de. Yeni bilgi ARASTIRMA_RAPORU.md'ye kaynağıyla eklenir.
- **Tasarım değişiklikleri DESIGN.md'ye işlenir.** Kodda DESIGN.md ile çelişen bir değer veya davranış olamaz. DESIGN.md §4'teki "kodda KULLANMA" tablosundaki değerler hiçbir koda girmez.
- spaceappschallenge.org sayfaları JavaScript ile oluşturulur; WebFetch boş veya eski sürüm döner. Tarayıcı ya da Wayback Machine kullan ve arşiv kopyasının tarihini belirt.
- **Demo internetsiz de çalışmalı.** Her canlı API için yerel önbellek tutulur (`data/cache/`, yedek yollar DESIGN.md §5'te).
- **Wiro MCP** bağlı (kişisel hesap, ~$16 kupon bakiyesi). Görsel, video ya da ses üretimi için kullanılabilir. Ücretli her çalıştırmadan önce kullanıcıya maliyeti söyle. YZ ile üretilen görsellere filigran gerekir.

## 5. Açık işler

- [ ] 2026 Project Submission ve Judging & Awards rehberlerini tarayıcıyla oku, §2'deki farkları işle.
- [ ] Kriz senaryolarını doğrulanmış değerler ve ISS formülerisiyle yeniden yaz (DESIGN.md §4).
- [ ] Doğrulanamayanları kontrol et (rapor §7): Vol 1 Rev C, Hassler 2014 birincil kaynağı, EMU içme torbası boyutu.
- [ ] DONKI ve Horizons önbelleğini `data/cache/` içine oluştur.

## 6. GitHub kullanım kuralları

**Repo:** `git@github.com:yldzemin/healmeout.git` (https://github.com/yldzemin/healmeout)

**Ortak çalışma yaptığının farkında ol.** Proje 3 bilgisayarda paralel geliştiriliyor. Her bilgisayarda bir Claude çalışıyor ve her biri kendi kullanıcısının GitHub hesabını kullanıyor.

### Senkronizasyon
- Çalışmaya başlamadan önce ve her push'tan önce `git pull --rebase` yap.
- `main` dalına doğrudan push yok. Her değişiklik bir PR ile gelir.
- Dal adları: `feat/<modül>`, `fix/<konu>`, `docs/<konu>` (örn. `feat/scenario-simulator`).
- Commit mesajları İngilizce.

### PR onayı
- Her bilgisayardaki Claude, **kendi kullanıcısına ait olmayan** PR'ları inceler ve onaylar ya da değişiklik ister (GitHub kişinin kendi PR'ını onaylamasına izin vermez).
- **Mimari karar** içeren bir PR'da (stack, klasör yapısı, veri modeli, yeni bağımlılık, senaryo motorunun yapısı) onaylamadan önce kullanıcısına sor.
- Gerçekten kritik bir onay veya red durumunda (veri kaybı, güvenlik, DESIGN.md §4'teki hatalı bir değerin geri gelmesi, NASA dışı veri, teslim kurallarına aykırılık) kullanıcısına bildir.
- DESIGN.md'yi değiştiren her PR mimari karar sayılır.
- PR takibi için PR bildirimlerine abone olunur. Sürekli sorgulama (polling) yapılmaz.

### Issue ve PR yorumları
Yorumlar, üç bilgisayardaki Claude'ların **ortak hafızasıdır**. Bir Claude diğerinin konuşmasını göremez; yalnızca repoyu, issue'ları ve yorumları görür.

- **Her iş bir issue ile başlar.** Issue'da: amaç, kabul ölçütü, hangi dosyalara dokunulacağı. PR açıklamasında `Closes #N` yaz.
- **Çakışmayı önle:** Bir işe başlarken issue'ya "Şu dosyalarda çalışıyorum: …" yorumu bırak ve issue'yu kendine ata. Başkasının atandığı dosyalara dokunmadan önce o issue'ya yorum yaz.
- **İnceleme yorumları satır bazında olur** (`gh pr review` veya inline yorum). Her yorumda: ne yanlış, neden, önerilen düzeltme. Genel "LGTM" tek başına yeterli değildir; neyi kontrol ettiğini yaz.
- **Kararları yaz:** Bir tartışma sonuca bağlanınca kararı ve gerekçesini issue'ya ayrı bir yorum olarak yaz ve `decision` etiketi ekle. Tasarım kararları ayrıca DESIGN.md'ye, çalışma kuralı kararları bu dosyaya işlenir.
- **Soruları yorumda sor:** Başka bir bilgisayardaki Claude'a veya kişiye soru, ilgili issue/PR yorumu olarak sorulur. `@` ile yalnızca gerçek GitHub kullanıcı adları etiketlenir.
- **YZ imzası:** Claude'un yazdığı her yorum `🤖 Claude (<kullanıcı adı>):` ile başlar. Bu hem kimin makinesinden geldiğini gösterir hem de teslimdeki YZ açıklamasına kanıt olur.
- **Etiketler:** `scenario`, `ui`, `data`, `docs`, `decision`, `blocked`, `needs-human`. İnsan kararı gereken her şey `needs-human` alır.
