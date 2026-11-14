# CLAUDE.md — ASTRA-TWIN (NASA Space Apps Challenge 2026)

Bu dosya, bu projede çalışan Claude için bağlamı ve kuralları tanımlar. Kullanıcıyla Türkçe konuş. Teslim içeriği (UI metinleri, README, demo altyazısı) İngilizce olacak, çünkü Space Apps kuralı bunu istiyor.

> **Son güncelleme:** 2026-11-09
> **Bir sonraki zorunlu yenileme:** 13 Kasım 2026. O gün challenge kaynakları ile 2026 Judging ve Submission rehberleri yayınlanacak. Yayınlanınca §2 ve §8'i güncelle.

## 1. Proje özeti

**ASTRA-TWIN**, astronotlar için bir sağlık izleme ve dijital ikiz aracıdır.

- **Challenge:** "Create Health Monitoring Software for Astronauts on Space Missions". Seviye: Intermediate.
  https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/
- **Resmi hedef:** "a visual tool that helps astronauts assess their own health … and provides guidance on treatments and health improvements."
- **Fikir:** Mars–Dünya arasındaki ışık gecikmesi (tek yön yaklaşık 3–22 dk) yüzünden kararlar gemide verilmeli. Sistemin iki parçası var:
  - **Edge node (gemide):** Kural tabanlı durum motoru, uyarılar, öneri protokolleri.
  - **Ghost Twin (Dünya'da):** Gecikmeli telemetriyle ileriye dönük simülasyon yapar (T+6 ve T+12 saat).
- **Konumlandırma:** Sistem kararı kendisi vermez, karar destek aracıdır. Önerileri ekip doktoru (Crew Medical Officer, CMO) onaylar. Kritik durumlar yerdeki uçuş doktoruna iletilir. Bu yaklaşım NASA'nın EIMO (Earth-Independent Medical Operations) yaklaşımına ve HRP'nin "semi-autonomous monitoring" hedefine uyar. "Otomatik ilaç enjekte eden giysi" gibi bir anlatım **kullanılmaz**, çünkü böyle bir NASA donanımı yok ve bu fikir Validity puanını düşürür.

## 2. Hackathon kuralları (kritik)

- **Tarih:** 14–15 Kasım 2026. Teslim 15 Kasım saat 23:59'da (yerel saat) kapanır.
- ⚠️ **SSS:** "Teams are not allowed to begin working on the challenges prior to the hackathon." 14 Kasım'dan önce yalnızca araştırma ve planlama yap; proje kodunu yazma. Kullanıcı kod isterse bu kuralı hatırlat.
- **NASA verisi** projenin çekirdek bileşeni olmalı. Global ödül için zorunlu. Relevance kriteri bunu "superficial addition" olmamalı diye tarif ediyor.
- **Demo:** En fazla 30 sn video veya en fazla 7 slayt. Herkese açık link olmalı, her şey İngilizce olmalı (2025 kuralı, 2026 için yeniden doğrulanacak).
- **SI (AI) kullanımı:** Teslimde nasıl kullanıldığı açıklanmak zorunda. AI ile üretilen görsellerde filigran olmalı ve NASA logosu kullanılmamalı. Bu projede kodu Claude yazıyor; teslimde bunu belirt.
- **Jüri kriterleri** (2025, her biri 1–5 puan): Impact, Creativity, Validity, Relevance, Presentation.

## 3. Dosya yapısı

```
CLAUDE.md                                     ← bu dosya
ARASTIRMA_RAPORU.md                           ← doğrulanmış araştırmanın tamamı (kaynaklarıyla)
(çevrimiçi) Proje Kapsam Dokümanı             ← ekip için tam kapsam, 6 senaryo, API'ler: https://claude.ai/artifact/V9p1PiJczNXqhjp5WoEpUT
nasa space apps araştırma sonucu olasılıklar.docx  ← ilk taslak spec + Streamlit kodu (DÜZELTİLMESİ GEREKEN değerler var, bkz. §5)
nasa örnek siteler araştırma.pdf              ← ilk kaynak listesi (OSDR ID'lerinde hatalar var, bkz. §6)
57110f2f9bef1ff11b47b3e897d3eee2.jpg          ← astronot wireframe görseli (mavi/cyan neon, koyu lacivert zemin), UI estetik referansı
data/osdr/                                    ← NASA OSDR'den indirilmiş gerçek Inspiration4 verisi (CSV, UTF-8 BOM)
kaynaklar/                                    ← NASA teknik dokümanları (OCHMO brief'leri, HRP RAP, M2M HHP)
```

Henüz bir kod tabanı yok. docx'teki Streamlit kodu yalnızca bir taslak.

## 4. Teknik yön (taslaktan)

- **Stack taslağı:** Python, Streamlit, numpy, pandas.
- **UI:** Üç sütun.
  - Sol: Astronot avatarı ve alt sistem durumları.
  - Orta: Zaman ufku kaydırıcısı (T+0/+6/+12) ve grafikler.
  - Sağ: Jüri kriz paneli ve log terminali.
- **Tema:** Arka plan `#0a0a0c`, cyan `#00d2ff`, yeşil `#00ff66`, kırmızı `#ff0055`.
- **Ses:** 60 Hz arka plan uğultusu, 800/1000 Hz alarm, 440→880 Hz onay tonu.
- **Challenge'ın önerdiği eksik özellikler** (eklenmeli):
  - Tıklanabilir insan figürü: her bölge kendi durumunu ve açıklamasını gösterir.
  - Ölçüm hatırlatmaları.
  - Göstergelerin ne anlama geldiğini anlatan sade açıklamalar ("do not assume astronauts know everything").
  - Renk kodları.
  - Oyunlaştırma.
- Docx'te enerji krizi "Amber" butonla tanımlı, kodda ise 🔵 mavi. Bu tutarsızlığı düzelt.

## 5. Taslaktaki hatalı değerler: kodda KULLANMA

Ayrıntı ve kaynaklar ARASTIRMA_RAPORU.md §4'te.

| Konu | Taslak | Doğrusu |
|---|---|---|
| Işık gecikmesi | Sabit "22 MINUTES", "4–24 dk" | JPL Horizons'tan canlı hesapla (9 Kasım 2026'da 11.37 dk). Aralık ~3–22 dk. |
| Glukoz eşiği | <60 | ADA: <70 Seviye 1, <54 Seviye 2 |
| Uçuş içi BP baseline | 120/80 | ~112/71. Asıl risk ortostatik intolerans. |
| HRV kritik <20 ms | — | Metriği tanımla (RMSSD), kişisel baseline ve z-skoru kullan |
| Hidrasyon azalması | saatte %1.5 / EVA'da %8 | Günde 2.5 L (saatte ~%0.13 kütle). EVA'da saatte %0.3–1.2. Kütlenin %2.5'i kaybedilince performans düşer. |
| Radyasyon arka planı | 0.05 mSv/sa | ISS 0.015, Mars transit 0.075, Mars yüzeyi 0.027 mSv/sa |
| Vücut ısısı | sabit 37.0 | Uzun görevde ~+1 °C kayma |
| Propranolol | radyasyon krizi | ❌ Kaldır. SPE hipertansiyon yapmaz. ISS'te metoprolol var. |
| Potasyum iyodür | SPE | ❌ Kaldır. SPE protondur, KI yalnızca radyoiyota karşı korur. |
| Antioksidan kokteyli | radyoproteksiyon | ❌ İnsanda kanıt yok. 400 IU E vitamini kanser riskini artırdı (SELECT). Yerine barınağa geçiş, dozimetri ve ondansetron (bulantı). |
| Glukagon | <50 | Önce 15 g ağızdan glukoz, 15 dk sonra tekrar ölç (ADA). ISS'te glukagon yok. |
| Melatonin | 5 mg | ISS'te 3 mg. Asıl önlem ışık ve uyku planlaması. |
| Basınç düşüşünde epinefrin | — | ❌ Doğrusu: O2 ve basıncı geri sağlamak. Epinefrin yalnızca anafilaksi veya kalp durması için. Model zinciri: basınç → PIO2 → SpO2. |
| Transdermal otomatik dozlama | — | ❌ Böyle bir donanım yok. "Öneri + CMO onayı" olarak yeniden tasarla. |

**Kural:** İlaç önerisi yalnızca 2023 ISS formülerisindeki ilaçlarla yapılır (Diaz 2024, doi:10.1038/s41526-024-00414-3). Her öneriyle birlikte kanıt düzeyi ve kaynak gösterilir. UI'da "Simulation, not medical advice" uyarısı bulunur.

## 6. Gerçek NASA verisi

- **OSD-575 CMP ve sitokinler:** En değerli veri. Gerçek astronot glukoz, Na, K, BUN, kreatinin, IL-6 ve CRP değerleri. Hipoglisemi, dehidrasyon ve inflamasyon katmanı için kullanılır.
- **OSD-569 CBC:** Hematoloji baseline'ı. 4 mürettebat (C001–C004) × 7 zaman noktası (L-92, L-44, L-3, R+1, R+45, R+82, R+194).
- ⚠️ **OSD-569/575 yalnızca uçuş öncesi ve sonrası veridir.** Uçuş içi veya radyasyon ölçümü içermez. OSD-570/571/687 omik veridir, vital bulgu değildir. OSD-815–818 astronot verisi değil, yerde donör gözlerle yapılmış SANS laboratuvar modelidir.
- **Açık uçuş içi nabız, SpO2 veya ısı verisi yok.** Bunları fizyolojik modelle simüle et ve sunumda bunu açıkça söyle.

**Canlı API'ler** (anahtar gerekmez, URL'ler raporun §6'sında):

1. **JPL Horizons:** Mars ışık gecikmesi.
2. **NOAA SWPC GOES:** Proton ve X-ray akısı, S-ölçeği. Güneş patlaması senaryosunu tetiklemek için.
3. **OSDR RadLab:** Gerçek doz serileri. Örneğin Mars'taki Eylül 2017 SPE'si (MSL-RAD), 11 Eylül zirvesi 17.87 μGy/sa. Kriz senaryosunda oynatılır.
4. **NASA DONKI:** Anahtar ister ve testte 503 döndü. Mutlaka yedek yol ve önbellek kullan.

**OSDR API:**
- `/osdr/data/osd/meta/<id>`, `/osdr/data/osd/files/<id>?page=1&size=100` (sayfalar 1'den başlar).
- İndirme: `https://osdr.nasa.gov` + `remote_url`.
- CSV okurken `utf-8-sig` kullan.

## 7. Çalışma kuralları

- **Varsayım yapma.** Her sayısal eşik ve tıbbi iddia için bir kaynak göster. Kaynak yoksa "doğrulanamadı" de. Yeni bilgi ARASTIRMA_RAPORU.md'ye kaynağıyla eklenir.
- spaceappschallenge.org sayfaları JavaScript ile oluşturulur; WebFetch boş döner. Tarayıcı ya da Wayback Machine kullan ve arşiv kopyasının tarihini belirt.
- API'lerde ağ kesintisine karşı yerel önbellek tut (`data/` klasörü). Demo internetsiz de çalışmalı.
- **Wiro MCP** bağlı (kişisel hesap, ~$16 kupon bakiyesi). Görsel, video ya da ses üretimi için kullanılabilir. Ücretli her çalıştırmadan önce kullanıcıya maliyeti söyle. AI ile üretilen görsellere filigran gerekir.
- Bu klasör henüz git deposu değil.

## 8. Açık işler

- [ ] 13 Kasım: Challenge kaynaklar sekmesini ve 2026 Judging/Submission rehberlerini oku, §2'yi güncelle.
- [ ] Yerel organizatöre hackathon öncesi hangi hazırlıkların serbest olduğunu sor.
- [ ] Kriz senaryolarını doğrulanmış değerler ve ISS formülerisiyle yeniden yaz (§5).
- [ ] Doğrulanamayanları kontrol et (rapor §7): Vol 1 Rev C, Hassler 2014 birincil kaynağı, EMU içme torbası boyutu.
