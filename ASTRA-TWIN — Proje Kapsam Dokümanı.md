# ASTRA-TWIN — Proje Kapsam Dokümanı

NASA Space Apps Challenge 2026 · Create Health Monitoring Software for Astronauts on Space Missions

Nov 9, 2026 · @Mustafa Polat Güldağ

## 1. Yönetici özeti

ASTRA-TWIN, Mars görevindeki bir astronotun kendi sağlığını gemide, Dünya'yı beklemeden değerlendirmesini sağlayan görsel bir karar destek aracıdır. Gerçek NASA verisiyle beslenen bir dijital ikiz üzerine kuruludur.

**Problem.** Mars–Dünya arasında sinyal tek yönde 3 ila 22 dakika sürer; gidiş-dönüş 44 dakikayı bulabilir. 9 Kasım 2026'da tek yön gecikme 11.37 dakikaydı (JPL Horizons). Acil bir durumda yerdeki uçuş doktorunun cevabını beklemek mümkün değildir. Challenge metni de sorumluluğun büyük kısmının astronotta olduğunu söyler: "carry much of the responsibility for spotting these changes in themselves".

**Çözüm.** İki parçadan oluşur:

- **Edge node (gemide):** Vital bulguları kural tabanlı bir durum motoruyla anında değerlendirir. Renk kodlu uyarı verir. ISS ilaç listesine dayalı bir öneri sunar; bu öneri ekip doktorunun (CMO) onayıyla uygulanır.
- **Ghost Twin (Dünya'da):** Gecikmeli telemetriyle astronotun 6 ve 12 saat sonraki durumunu tahmin eder. Yeni paket geldiğinde tahmini gerçekle karşılaştırır.

**Neden güçlü bir aday.**

- Challenge'ın resmi hedefini birebir karşılar: görsel araç, öz-değerlendirme, tedavi ve iyileştirme rehberliği.
- NASA verisini çekirdekte kullanır. Inspiration4 mürettebatının gerçek kan değerleri (OSD-569, OSD-575), Mars'ta ölçülmüş gerçek güneş patlaması dozu (RadLab, Eylül 2017) ve canlı NOAA uzay havası verisi.
- Her eşik ve öneri bir NASA standardına ya da hakemli bir makaleye dayanır. Bu, Validity kriterinde fark yaratır.
- Geçmiş Space Apps kazananları arasında astronot dijital ikizi üzerine kurulu bir proje bulunamadı.

**Tek cümlelik pitch (İngilizce, sunum için):** *"When Earth is 22 light-minutes away, ASTRA-TWIN lets astronauts read their own body — a NASA-data-driven digital twin that flags risk, explains it, and recommends ISS-formulary actions for the crew medical officer to confirm."*

## 2. Challenge ve kurallar

Kod hackathon süresince (14–15 Kasım 2026) yazılmalı. Resmi SSS açıkça şöyle diyor: "Teams are not allowed to begin working on the challenges prior to the hackathon." Bu doküman yalnızca araştırma ve planlamadır.

**Challenge:** [Create Health Monitoring Software for Astronauts on Space Missions](https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/). Seviye Intermediate; konular Human Exploration, Software, Space Exploration.

**Resmi hedef:** "create a visual tool that helps astronauts assess their own health during space missions and provides guidance on treatments and health improvements."

**Metnin bizden bekledikleri ve bizim karşılığımız:**

| Challenge metni | ASTRA-TWIN karşılığı |
| --- | --- |
| Her türlü girdi: klavye, fare, kamera | Manuel ölçüm girişi, kriz butonları, simüle sensör akışı |
| Örnek göstergeler: BP, HR, kan şekeri, ısı, uyku, ruh hali | Parametre matrisinin tamamı (§5) |
| Uzayda hangi ölçümler yapılabilir? | Her göstergenin yanında "nasıl ölçülür" kartı |
| Tehlikeler göstergeleri nasıl etkiler? | 5 tehlike (RIDGE) ile gösterge eşlemesi ve senaryolar |
| Ne zaman yerdeki uçuş doktoru devreye girmeli? | Eskalasyon seviyeleri ve Ghost Twin |
| Kritik durum nasıl önlenir? | Önleyici öneriler ve T+6/T+12 tahmini |
| Tıklanabilir insan figürü | Wireframe astronot, bölge bölge durum gösterimi |
| Opsiyonel: oyunlaştırma, dinamik grafik, renk kodu, hatırlatma | Hepsi kapsamda (§3) |
| "Do not assume astronauts know everything" | Her gösterge ve risk için sade açıklama katmanı |

**Tarihler (2026):**

- **13 Kasım:** Challenge kaynakları, Project Submission Guide ve Judging & Awards Guide yayınlanır.
- **14 Kasım 09:00 – 15 Kasım 23:59 (yerel saat):** Hackathon ve teslim penceresi.
- **Aralık 2026:** Global Nominees ve Finalists açıklanır. **Ocak 2027:** Global Winners.

**Kurallar:**

- Takım en fazla 6 kişi. Her takım tek challenge seçebilir.
- Yapay zekânın 2026'daki adı "Super Intelligence (SI)". Kullanmak serbest, ama teslimde nasıl kullanıldığını açıklamak zorunlu.
- Global ödül için NASA verisi ya da kaynağı kullanmak zorunlu (2025 kuralı).

**Jüri kriterleri (2025 rehberi; 2026 rehberi 13 Kasım'da çıkacak).** Her kriter 1–5 puanlanıyor:

| Kriter | Ne puanlanır | Bizim karşılığımız |
| --- | --- | --- |
| Impact | Problemin büyüklüğü, kime yardım ettiği | Mars görevinde Dünya'dan bağımsız sağlık yönetimi (NASA EIMO hedefi) |
| Creativity | Özgünlük | Ghost Twin ile gecikme farkını görünür kılmak; gerçek SPE dozunu oynatmak |
| Validity | Bilimsel temel, fizibilite, kullanılabilirlik | Her eşik kaynaklı; ilaçlar yalnızca ISS formülerisinden |
| Relevance | Challenge'ı tam karşılamak, NASA verisi çekirdekte | OSDR, RadLab, NOAA ve JPL verisi motorun girdisi |
| Presentation | Hikaye ve demo | 30 saniyelik kriz hikayesi (§11) |

**Teslim (2025 rehberi):**

- En fazla 30 saniyelik video **veya** en fazla 7 slayt (başlık dahil). Sınır aşılırsa global jürilemeye alınmaz.
- Giriş gerektirmeyen herkese açık link.
- Her şey İngilizce, videoda İngilizce altyazı.
- AI ile üretilen görselde filigran; NASA logosu kullanılmaz.

Kaynaklar: [Space Apps 2026](https://www.spaceappschallenge.org/2026/), [Participant FAQs](https://www.spaceappschallenge.org/resources/participant-faqs/), [Awards](https://www.spaceappschallenge.org/awards/), [2025 Judging Guide (arşiv)](https://web.archive.org/web/20251128144656id_/https://www.spaceappschallenge.org/resources/judging-awards-guide/), [2025 Submission Guide (arşiv)](https://web.archive.org/web/20251229205712id_/https://www.spaceappschallenge.org/resources/project-submission-guide/).

## 3. Kapsam

Önce MVP katmanı bitirilir. Hedef katmanı MVP çalışır durumdayken eklenir; ekstralar yalnızca zaman kalırsa yapılır. Demo her aşamada internetsiz de çalışmalı.

| # | Özellik | Katman | Neden | Veri |
| --- | --- | --- | --- | --- |
| 1 | Vital paneli: HR, BP, SpO2, ısı, glukoz, hidrasyon, uyku, stres/ruh hali | MVP | Challenge'ın örnek göstergeleri | I4 CMP/CBC baseline + model |
| 2 | Renk kodlu durum: yeşil / sarı / turuncu / kırmızı | MVP | Challenge önerisi | Eşik tablosu (§5) |
| 3 | Kriz enjeksiyon paneli: 6 senaryo | MVP | Jüri demosu | §6 |
| 4 | Öneri protokolü ve CMO onay butonu | MVP | "guidance on treatments" | ISS formülerisi |
| 5 | Zaman ufku: T+0 / T+6 / T+12 | MVP | Önleme ve tahmin | Durum motoru |
| 6 | Canlı ışık gecikmesi göstergesi | MVP | Problemin kendisi | JPL Horizons |
| 7 | Gerçek SPE doz eğrisi (Mars, Eylül 2017) | MVP | NASA verisi çekirdekte | OSDR RadLab |
| 8 | Tıklanabilir vücut figürü: bölge bölge durum | Hedef | Challenge'ın verdiği örnek | Durum motoru |
| 9 | "Bu ne demek?" açıklama kartları | Hedef | "Do not assume astronauts know everything" | Kaynaklı metin |
| 10 | Ghost Twin paneli: Dünya'nın gördüğü ile gerçek arasındaki fark | Hedef | Yaratıcılık | Durum motoru + gecikme |
| 11 | Canlı NOAA uzay havası bandı (S-ölçeği) | Hedef | Gerçek veri | NOAA SWPC |
| 12 | Ses tasarımı: uğultu, alarm, onay tonu | Hedef | Sunum etkisi | Web Audio |
| 13 | Ölçüm hatırlatmaları ve günlük rutin | Ekstra | Challenge önerisi | Görev takvimi |
| 14 | Oyunlaştırma: günlük sağlık serisi, rozetler | Ekstra | Challenge önerisi | Yerel durum |
| 15 | Ortam seçici: ISS / Mars transit / Mars yüzeyi | Ekstra | Farklı ortamlar | Radyasyon presetleri |
| 16 | Kemik ve kas kaybı, SANS uzun vadeli izleme | Ekstra | 5 tehlike kapsamı | Literatür oranları |

**Kapsam dışı (bilerek yapılmayacak):**

- Gerçek donanım ya da sensör entegrasyonu. Vital bulgular simüle edilir ve bu sunumda açıkça söylenir.
- Otomatik ilaç dozlama. Sistem yalnızca öneri sunar; onayı CMO verir.
- Makine öğrenmesi modeli eğitmek. Inspiration4'te 4 kişi ve 7 zaman noktası var; bu veri eğitim için yetersiz. Motor kural tabanlı ve fizyolojik model temelli olacak.
- Kullanıcı hesabı ya da sunucu taraflı veritabanı.

## 4. Sistem mimarisi

Tüm kararlar gemide, 0 ms gecikmeyle alınır. Dünya aynı durumu 3–22 dakika geriden görür ve Ghost Twin bu farkı tahminle kapatır.

&#91;embedded content: ASTRA-TWIN mimarisi · gemi ve Dünya, 8 bileşen\]

**Gemide (Edge node):**

1. **Vitaller:** Astronot ölçümleri manuel girer ya da simüle sensör akışı veri sağlar. Her ölçüm zaman damgalıdır.
2. **Durum motoru:** Durum vektörünü her zaman adımında günceller. Kişisel baseline'lar NASA verisinden gelir (I4 kan değerleri, RadLab doz, NOAA akısı). Eşikler §5'teki kaynaklı tablodan okunur.
3. **Eskalasyon:** Her göstergenin durumuna göre L0–L4 seviyesi belirlenir. Seviyeler §6'da tanımlı.
4. **Öneri kartı:** Senaryoya uygun protokol gösterilir. Yalnızca ISS formülerisindeki ilaçlar ve NASA'nın operasyonel önlemleri kullanılır; her önerinin yanında kanıt düzeyi ve kaynak vardır.
5. **CMO onayı:** Öneriyi ekip doktoru onaylar ya da reddeder. Sistem kendi başına hiçbir şey uygulamaz. Karar log terminaline yazılır.

**Dünya'da (Ghost Twin):**

1. **Telemetri:** Gemi durumu paket halinde gönderilir ve canlı gecikmeyle ulaşır. Gecikme JPL Horizons'tan hesaplanır.
2. **Ghost Twin:** Son bilinen durumdan ve görev planından yola çıkarak T+6 ve T+12 tahmini üretir. Yeni paket geldiğinde tahminle gerçeği karşılaştırır ve farkı gösterir.
3. **Uçuş doktoru:** L3 olaylarını inceler ve tavsiyesini gönderir. Tavsiye gemiye yine aynı gecikmeyle ulaşır; bu yüzden geminin onu beklemeden karar vermesi gerekir.

**Bu mimari ne değildir:** Makine öğrenmesi modeli değildir. Inspiration4 verisi 4 kişi ve 7 zaman noktasından oluşuyor; bu, model eğitmek için yetersiz. "Edge-AI" ifadesi sunumda "kural tabanlı ve fizyolojik model temelli edge karar desteği" olarak açıklanmalı.

## 5. Parametre matrisi

Motorun her eşiği aşağıdaki kaynaklı değerlerden gelir. İlk taslaktaki yanlış değerler düzeltildi. Kaynaksız tek bir sayı bile kullanılmaz.

### 5.1 Fizyolojik göstergeler

| Gösterge | Uçuş içi baseline | Sarı | Kırmızı | Kaynak |
| --- | --- | --- | --- | --- |
| Nabız (HR) | Kişisel baseline (uçuşta değişmiyor) | Dinlenmede <50 veya >100 bpm | <40 veya >150 bpm | [Norsk 2015](https://doi.org/10.1113/jphysiol.2014.284869) |
| HRV (RMSSD) | Kişisel baseline; popülasyon 27±12 ms | Baseline'dan −1 SD | Baseline'dan −2 SD | Task Force 1996 (PMID 8598068) |
| Kan basıncı | \~112/71 mmHg (uçuşta 8/9 mmHg düşük) | Sistolik <90 veya >140 | Sistolik <80 veya >160 | Norsk 2015 |
| SpO2 | %96–99 (ISS kabini deniz seviyesine eşdeğer) | <%94 | <%90 | Klinik konvansiyon; NASA PIO2 tanımı ([OCHMO-TB-003](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-003-habitable-atmosphere.pdf)) |
| Vücut ısısı | 37.0 °C; uzun görevde \~2.5 ayda +1 °C kayma | <35.5 veya >38.5 °C | <35 veya >40 °C | [Stahn 2017](https://doi.org/10.1038/s41598-017-15560-w) |
| Glukoz | \~90 mg/dL; I4 CMP'deki gerçek değerler | <70 (ADA Seviye 1) | <54 (ADA Seviye 2) | [ADA 2025](https://doi.org/10.2337/dc25-S006), OSD-575 |
| Hidrasyon (kayıp) | 2.5 L/gün alım | Kitle kaybı >%1.5 | Kitle kaybı ≥%2.5 (performans düşüşü) | [OCHMO-TB-027](https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf) |
| Uyku | ISS ortalaması 6.1 sa/gece | <6 sa | <4 sa veya 3 gece üst üste <5 sa | [Barger 2014](<https://doi.org/10.1016/S1474-4422(14)70122-X>) |
| Ruh hali ve stres | Öz-bildirim (1–5) + HRV | 2 gün üst üste ≤2 | ≤1 ya da panik bildirimi | HRP Behavioral Health RAP |

Sarı ve kırmızı eşiklerin bazıları (HR, BP, ısı, SpO2 <%94) klinik konvansiyondan alındı; NASA bu noktalar için tek bir sayı yayımlamıyor. Sunumda bunlar "clinical convention" diye etiketlenmeli.

### 5.2 Ortam göstergeleri

| Gösterge | Nominal | Uyarı | Kritik | Kaynak |
| --- | --- | --- | --- | --- |
| Radyasyon doz hızı | ISS 0.015 / Mars transit 0.075 / Mars yüzeyi 0.027 mSv/sa | NOAA S3 (≥10 MeV ≥ 10³ pfu): EVA'dan kaçın | S4 ve üstü: barınağa geç | [Zeitlin 2013](https://doi.org/10.1126/science.1235989), [NOAA scales](https://www.spaceweather.gov/noaa-scales-explanation) |
| Olay başına doz | — | 100 mSv | 250 mSv (V1 4031) | [OCHMO-TB-020](https://www.nasa.gov/wp-content/uploads/2023/03/radiation-protection-technical-brief-ochmo.pdf) |
| Kariyer dozu | — | 400 mSv | 600 mSv (V1 4030) | OCHMO-TB-020 |
| CO2 | <2.5 mmHg (7 günlük ortalama) | 1 saatlik ortalama >3 mmHg | ≥4.5 mmHg | [OCHMO-TB-004](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf) |
| PIO2 | 145–155 mmHg | <145 | <122 mmHg | OCHMO-TB-003 |
| Giysi basıncı (EMU) | 4.3 psi, %100 O2 | <4.0 psi | <2.5 psia | OCHMO-TB-003, TB-037 |
| Habitat basıncı | 14.7 psia (ISS) | <5.0 psia | Basınç düşüşü >13.5 psi/dk | OCHMO-TB-003 |

Olay başına 100 mSv ve kariyer için 400 mSv uyarı seviyeleri bizim tasarım seçimimiz, NASA değeri değil. Giysi basıncındaki 4.0 psi uyarı eşiği de öyle. Yalnızca limitler NASA'dan.

### 5.3 Metabolik referanslar (82 kg referans mürettebat)

- **Enerji:** 3,035 kcal/gün, EVA'da saatte +200 kcal (NASA-STD-3001 V2 7003/7004).
- **Su:** 2.5 L/gün, EVA'da saatte +240 mL (V2 11030).
- **O2 tüketimi:** 0.82 kg/gün. **CO2 üretimi:** 1.04 kg/gün.
- **Isı üretimi:** uykuda 88 W, nominal 139 W, egzersizde en fazla 826 W ([Ewert & Downs, ICES-2021](https://ntrs.nasa.gov/api/citations/20205010692/downloads/ICES-2021_Metabolic_Paper-Final.pdf)).
- **EVA'da su kaybı:** saatte vücut kitlesinin %0.3–1.2'si; zirve egzersizde \~1 L/sa.
- **Kemik kaybı:** ayda \~%1–1.5 ([NASA HHP white paper](https://www.nasa.gov/wp-content/uploads/2024/01/human-health-and-performance.pdf)).

3,035 kcal değeri NASA-STD-3001'in arama özetinden alındı, standardın kendisinden doğrudan okunmadı. Hackathon'da kontrol edilmeli.

## 6. Kriz senaryoları

Demoda 1 nominal gün ve 6 kriz senaryosu var. Her biri NASA'nın 5 tehlikesinden (RIDGE) birine bağlı. Her senaryo aynı dört adımı izler: tetikleyici, algılama kuralı, öneri ve eskalasyon. Öneriler yalnızca ISS formülerisindeki ilaçları ve NASA'nın operasyonel önlemlerini kullanır.

**Eskalasyon seviyeleri (tüm senaryolarda ortak):**

| Seviye | Renk | Kim karar verir | Ne olur |
| --- | --- | --- | --- |
| L0 Nominal | Yeşil | — | İzleme ve rutin hatırlatmalar |
| L1 Öz-bakım | Sarı | Astronot | Sade talimat ve açıklama kartı |
| L2 CMO onayı | Turuncu | Crew Medical Officer | Öneri ekrana gelir, CMO onaylar ya da reddeder |
| L3 Yer bilgilendirme | Kırmızı | CMO + uçuş doktoru | Paket Dünya'ya gider; cevap gecikmeyle gelir, Ghost Twin farkı gösterir |
| L4 Acil | Yanıp sönen kırmızı | CMO protokole göre hemen uygular | Yer bilgilendirilir ama beklenmez |

**Bu bölümdeki T+6 ve T+12 eğrileri simülasyon modelidir, ölçülmüş veri değildir.** Sunumda "modelled trajectory" diye etiketlenmeli. Gerçek veri yalnızca özellikle belirtilen yerlerde kullanılıyor.

### 6.0 Nominal gün (başlangıç ekranı)

- **Amaç:** Jüriye normal durumu ve öz-değerlendirme döngüsünü göstermek.
- **Gösterilen:** Kişisel baseline'lar. Glukoz, Na ve BUN baseline'ı OSD-575'teki gerçek I4 değerlerinden seçilen bir mürettebatın (ör. C001, L-3 zaman noktası) değerleri olur.
- **Rutin:** Sabah ölçüm hatırlatması, su takibi (2.5 L/gün hedefi), uyku kaydı, ruh hali (1–5).
- **Ekran:** Canlı ışık gecikmesi ve NOAA S-ölçeği bantı.

### 6.1 Güneş parçacık olayı (SPE) — turuncu buton

- **Tehlike:** Radiation.
- **Tetikleyici, iki mod:**
  1. *Gerçek veri modu:* Mars yüzeyindeki Eylül 2017 SPE'si OSDR RadLab'den (MSL-RAD) oynatılır. Günlük ortalamalar: 9 Eylül 7.95, 11 Eylül 17.87 (zirve 23.18), 12 Eylül 12.92, 14 Eylül 6.56 μGy/sa.
  2. *Transit modu:* NASA'nın tasarım referansı olan SPE (toplanmış Ekim 1989 olayları) kullanılır. Limit: olay başına <250 mSv.
  - Opsiyonel: Canlı NOAA ≥10 MeV proton akısı ≥10 pfu (S1) olursa sistem gerçek bir uyarı gösterir.
- **Algılama kuralı:**
  - NOAA S3 ve üstü ya da doz hızı baseline'ın 5 katından fazla → L2.
  - Olay için birikmiş doz tahmini ≥100 mSv → L3.
  - ≥250 mSv projeksiyonu → L4.
- **Öneri (doğrulanmış):**
  - Barınağa geç. Gerekli zarf: \~10 cm su eşdeğeri. Zarf, SPE dozunu \~%50 azaltır.
  - EVA'yı ertele.
  - Kişisel dozimetreyi kontrol et; kümülatif dozu 600 mSv kariyer limitine karşı göster.
  - Bulantı olursa ondansetron. Bu ilaç ISS formülerisinde var; CMO onayı ister.
- **Kaldırılanlar:** Potasyum iyodür, propranolol, antioksidan kokteyli. Nedeni §12'de.
- **Fizyoloji gösterimi:** Bu doz seviyelerinde akut belirti beklenmez. CDC'ye göre hafif belirtiler \~0.3 Gy'de başlar. Ekranda stres ve HR artışı "anksiyete modeli" olarak gösterilir, radyasyon etkisi olarak gösterilmez.
- **T+6 / T+12:** Doz eğrisi gerçek veriden gelir. Stres modeli barınağa geçildikten sonra baseline'a döner.

### 6.2 EVA sırasında dehidrasyon ve hipoglisemi — amber buton

- **Tehlike:** Hostile environment ve Distance.
- **Tetikleyici:** Uzun bir EVA'da içme torbası arızalanır ve beslenme gecikir.
- **Fizyoloji (kaynaklı hızlar):**
  - Su kaybı saatte vücut kitlesinin \~%1'i. Bu, üst sınıra yakın bir egzersiz hızı.
  - İçme torbası olmadan yaklaşık 2.5 saatte %2.5 kayıp eşiğine ulaşılır; performans düşer.
  - EVA saatte +200 kcal ister; karşılanmazsa glukoz düşer.
- **Algılama kuralı:**
  - Kayıp >%1.5 ya da glukoz <70 → L1.
  - Kayıp ≥%2.5 ya da glukoz <54 → L3.
  - Bilinç bulanıklığı bildirilirse → L4.
- **Öneri (doğrulanmış):**
  - ADA 15-15 kuralı: 15 g hızlı karbonhidrat al, 15 dakika sonra tekrar ölç, gerekirse tekrarla.
  - Su hedefi saatte ≥240 mL (V2 11030).
  - Glukoz <54 ya da kayıp ≥%2.5 olursa EVA'yı sonlandır ve airlock'a dön.
  - Habitatta elektrolitli sıvı al.
- **Kaldırılanlar:** Glukagon (ISS'te yok), BCAA "acil ilaç" olarak.
- **Veri:** Glukoz, Na ve BUN/kreatinin oranı baseline'ı ve referans aralıkları OSD-575 CMP'den gelir.

### 6.3 Sirkadiyen bozulma ve akut stres — mor buton

- **Tehlike:** Isolation/confinement ve Distance.
- **Tetikleyici:** 3 gece üst üste <5 saat uyku, HRV'de baseline'dan −2 SD düşüş, ruh hali ≤2.
- **Algılama kuralı:**
  - Tek gece <6 sa → L1.
  - Kombine tetikleyici → L2.
  - Panik bildirimi ya da kritik bir görev sırasında oluşursa → L3.
- **Öneri (doğrulanmış):**
  - Uyku penceresini yeniden planla ve görev yükünü azalt.
  - Melatonin 3 mg (ISS'teki tablet).
  - Zolpidem 5 mg yalnızca CMO onayıyla. Uçuşta en sık kullanılan uyku ilacı bu (Barger 2014).
  - Kritik bir operasyon şartsa modafinil 200 mg, CMO onayıyla.
  - Dünya'yla gecikmeli (asenkron) davranışsal sağlık görüşmesi.
- **Kaldırılanlar:** Melatonin 5 mg (ISS'teki doz 3 mg), L-triptofan (kanıt yok).
- **Not:** Işık tedavisi NASA'nın bilinen bir yöntemi, ama kaynağı bu araştırmada doğrulanmadı. Kullanılmadan önce kontrol edilmeli.

### 6.4 Giysi basınç kaybı ve hipoksi — kırmızı buton

- **Tehlike:** Hostile environment.
- **Tetikleyici:** EVA sırasında giysi basıncı 4.3 psi'den düşmeye başlar.
- **Fizyoloji modeli:** Zincir şu: giysi basıncı → PIO2 → SpO2.
  - PIO2 = (P − 47 mmHg) × FiO2. %100 O2 ile 4.3 psi (222 mmHg) → PIO2 ≈ 175 mmHg, yani normoksi.
  - PIO2 122 mmHg alt sınırına giysi basıncı \~3.3 psi'de inilir.
  - 2.5 psia altında ek O2 yetersiz kalır. 0.91 psi'de ebulizm oluşur.
- **Algılama kuralı:**
  - Basınç <4.0 psi → L2.
  - PIO2 <122 mmHg ya da SpO2 <%90 → L4.
- **Öneri (doğrulanmış):**
  - Secondary Oxygen Pack'i açık devre arıtma modunda devreye al (≥30 dk O2 sağlar).
  - EVA'yı iptal et, airlock'a dön, basıncı geri sağla.
  - Sonrasında dekompresyon hastalığı (DKS) takibi. DKS varsa rekompresyon ve %100 O2.
- **Kaldırılan:** Basınç kaybı tedavisi olarak epinefrin. Epinefrin yalnızca kalp durması ya da anafilaksi için, CMO tarafından.

### 6.5 Habitatta CO2 birikmesi — sarı buton

- **Tehlike:** Hostile/closed environment. ISS'te gerçekten yaşanan bir sorun; ISS'te 1–9 mmHg aralığı gözlenmiş.
- **Tetikleyici:** CO2 temizleme sistemi verimi düşer; CO2 3 mmHg'den 6 mmHg'ye yavaşça yükselir.
- **Fizyoloji (kaynaklı):**
  - 2.8–4.5 mmHg'de baş ağrısı.
  - 4.5–5.0 mmHg'de trigeminal disestezi.
  - NASA'nın acil durum (contingency) önerisi: 6.8–10 mmHg en fazla 4 saat (henüz standart değil).
- **Algılama kuralı:**
  - 1 saatlik ortalama >3 mmHg → L1.
  - ≥4.5 mmHg ya da baş ağrısı bildirimi → L2.
  - ≥6.8 mmHg → L3.
- **Öneri:**
  - Eforu azalt.
  - Havalandırması iyi bir modüle geç.
  - Temizleme sistemi için bakım prosedürünü başlat.
  - Baş ağrısı için analjezik önerilmeden önce ISS formülerisinden hangisinin bulunduğu doğrulanmalı. Şu an açık bir soru.
- **Kaynak:** [OCHMO-TB-004](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf), [ICES-2025](https://ntrs.nasa.gov/api/citations/20250006273/downloads/ICES-2025-402.pdf).

### 6.6 Mars'a inişte ortostatik intolerans — mavi buton

- **Tehlike:** Gravity fields. Uzayda tansiyon düşer; asıl risk inişte ve yerçekimi geçişlerinde ortaya çıkar (NASA HHP white paper).
- **Tetikleyici:** Aylar süren transitin ardından Mars'a iniş. Astronot ayağa kalktığında sistolik basınç düşer, nabız yükselir.
- **Algılama kuralı:**
  - Ayakta sistolik basınç <90 ya da baş dönmesi bildirimi → L2.
  - Bayılma → L4.
- **Öneri (doğrulanmış):**
  - İnişten önce sıvı yüklemesi: ≥1 L su ve tuz tableti. NASA bunu Dünya'ya dönüşte kullanıyor (OCHMO-TB-027).
  - Kademeli aktivite. İnişten sonraki ilk saatlerde kritik görev sınırlaması.
- **Not:** Kompresyon giysisi gibi ek önlemler bu araştırmada doğrulanmadı; kullanılmadan önce kaynak bulunmalı.

Docx'teki dört senaryoya (6.1–6.4) 6.5 ve 6.6 eklendi. Böylece 5 tehlikenin hepsi kapsanıyor. Docx'te enerji krizi "Amber" diye geçiyordu, kod 🔵 mavi kullanıyordu; mavi artık 6.6'nın rengi.

## 7. API ve veri entegrasyonları

Beş kaynaktan dördü anahtarsız çalışıyor ve 9 Kasım 2026'da canlı test edildi. Yalnızca NASA DONKI anahtar istiyor ve testte 503 hatası verdi. Tüm veriler demo öncesi `data/cache/` klasörüne kaydedilecek; uygulama internetsiz de çalışacak.

| Öncelik | Kaynak | Ne için | Anahtar | Durum (9 Kasım) |
| --- | --- | --- | --- | --- |
| 1 | [JPL Horizons](https://ssd-api.jpl.nasa.gov/doc/horizons.html) | Canlı Mars ışık gecikmesi | Yok | Çalışıyor: 11.37 dk |
| 2 | [NOAA SWPC](https://services.swpc.noaa.gov/json/) | Canlı proton akısı, X-ray, S-ölçeği | Yok | Çalışıyor |
| 3 | [OSDR RadLab](https://visualization.osdr.nasa.gov/radlab/gui/data-api) | Gerçek radyasyon doz serileri | Yok | Çalışıyor |
| 4 | [NASA OSDR](https://osdr.nasa.gov/bio/repo) | I4 kan verisi (CMP, CBC, sitokin) | Yok | Çalışıyor; CSV'ler indirildi |
| 5 | [NASA DONKI](https://api.nasa.gov/) | Geçmiş güneş olayları kataloğu | Gerekli | 503 hatası; opsiyonel |

### 7.1 JPL Horizons — ışık gecikmesi

```
GET https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='499'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='OBSERVER'&CENTER='500@399'&START_TIME='2026-11-14'&STOP_TIME='2026-11-15'&STEP_SIZE='1%20h'&QUANTITIES='20,21'
```

- `COMMAND='499'` Mars'ı, `CENTER='500@399'` Dünya merkezini seçer.
- `QUANTITIES='20,21'` uzaklığı (au), uzaklık değişim hızını (km/s) ve `1-way_down_LT` değerini (dakika) döndürür.
- Yanıt JSON içindeki `result` metninin `$$SOE` ile `$$EOE` arasındaki kısmından parse edilir.
- Saatlik önbellek yeterli. Yedek: `skyfield` ya da `astropy` ile yerel hesap.

### 7.2 NOAA SWPC — canlı uzay havası

| Endpoint | Dönen veri |
| --- | --- |
| `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json` | GOES integral proton akısı: ≥1, ≥10, ≥50, ≥100 MeV, 5 dakikada bir |
| `https://services.swpc.noaa.gov/json/goes/primary/xrays-1-day.json` | X-ray akısı (0.05–0.4 ve 0.1–0.8 nm) |
| `https://services.swpc.noaa.gov/json/goes/primary/xray-flares-latest.json` | Son patlamanın başlangıç, zirve ve bitiş zamanı ile sınıfı |
| `https://services.swpc.noaa.gov/products/noaa-scales.json` | Güncel R/S/G ölçekleri ve olasılıklar |
| `https://services.swpc.noaa.gov/products/alerts.json` | Metin uyarıları |

- S1 eşiği: ≥10 MeV akısı ≥10 pfu. Senaryo 6.1'in canlı tetikleyicisi bu.
- `-6-hour`, `-3-day` ve `-7-day` varyantları olduğu belirtiliyor, ama yalnızca `-1-day` test edildi.

### 7.3 OSDR RadLab — gerçek doz verisi

```
GET https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Mars&timestamp%3E=2017-09-08&timestamp%3C2017-09-16&absorbed_dose_rate&format=csv
GET https://visualization.osdr.nasa.gov/radlab/api/?spacecraft=ISS&instrument=DosTel&timestamp%3E=2022-04-01T23%3A00&timestamp%3C2022-04-02T01%3A05&absorbed_dose_rate&altitude&format=json
```

- Alanlar: `absorbed_dose_rate` (μGy/sa), `dose_equivalent_rate` (μSv/sa), `flux`, `altitude`, `latitude`, `longitude`.
- Çıktı biçimleri: csv, tsv, json, html.
- Cihazlar: ISS DosTel, MSL-RAD-Surface (Curiosity, Mars), Liulin-MO (ExoMars TGO), CRaTER (LRO), BioSentinel, Chang'e-4 LND.
- ISS testinde \~429 km irtifada \~6–11 μGy/sa döndü.

### 7.4 NASA OSDR — Inspiration4 kan verisi

| Amaç | Endpoint |
| --- | --- |
| Çalışma metadata'sı | `https://osdr.nasa.gov/osdr/data/osd/meta/575` |
| Dosya listesi | `https://osdr.nasa.gov/osdr/data/osd/files/575?page=1&size=100` (sayfalar 1'den başlar) |
| Dosya indirme | `https://osdr.nasa.gov` + `remote_url` |
| Arama | `https://osdr.nasa.gov/osdr/data/search?term=inspiration4&type=cgene&size=50` |
| Görev bilgisi | `https://osdr.nasa.gov/geode-py/ws/api/mission/SpaceX%20Inspiration4` |

**Projede indirilmiş dosyalar (`data/osdr/`):**

- `OSD-575_LSDS-8_CMP_TRANSFORMED.csv`: glukoz, Na, K, Cl, CO2, BUN, kreatinin, eGFR, Ca, albumin, ALT, AST, ALP, bilirubin. Her değerle birlikte referans aralığı var.
- `OSD-575_LSDS-8_Immune_EvePanel_TRANSFORMED.csv`: IL-6, IL-10, IFN-γ ve diğerleri (pg/mL).
- `OSD-575_LSDS-8_Cardio_EvePanel_TRANSFORMED.csv`: CRP, fibrinojen, haptoglobin.
- `OSD-569_LSDS-7_CBC_TRANSFORMED.csv` ve `..._SUBMITTED.csv`: tam kan sayımı.
- Her dosyada 4 mürettebat (C001–C004) × 7 zaman noktası var: L-92, L-44, L-3, R+1, R+45, R+82, R+194.

**Python ile okuma:**

```python
import requests, pandas as pd, io
B = "https://osdr.nasa.gov"
files = requests.get(f"{B}/osdr/data/osd/files/575", params={"page": 1, "size": 100}).json()["studies"]["OSD-575"]["study_files"]
f = next(x for x in files if x["file_name"].endswith("CMP_TRANSFORMED.csv"))
cmp = pd.read_csv(io.StringIO(requests.get(B + f["remote_url"]).content.decode("utf-8-sig")))
```

**Dikkat:**

- CSV'ler UTF-8 BOM ile başlıyor; `utf-8-sig` ile decode edilmeli.
- OSD-569 TRANSFORMED dosyasında hemoglobin `hemoglobin_value_percent` diye etiketli, ama birimi g/dL.
- Bu veriler yalnızca uçuş öncesi ve sonrasına ait; uçuş sırasında ölçüm yok.

### 7.5 NASA DONKI — opsiyonel

```
GET https://api.nasa.gov/DONKI/SEP?startDate=2024-05-01&endDate=2024-06-30&api_key=KEY
```

- Diğer uçlar: `/FLR`, `/CME`, `/GST`, `/notifications`.
- Ücretsiz anahtar [api.nasa.gov](https://api.nasa.gov/) adresinden alınır. DEMO\_KEY'in limiti çok düşük; testte `x-ratelimit-limit: 10` görüldü.
- 9 Kasım testinde 503 döndü. Yalnızca önbellekli bir "geçmiş olaylar" paneli için kullanılmalı.

### 7.6 Kullanılamayan kaynaklar

- **NASA LSDA:** Yalnızca resmi başvuruyla erişiliyor.
- **Twins Study:** Açık indirme bulunamadı.
- **Polaris Dawn:** Açık veri seti yok.
- **OSD-570, 571, 687:** Omik veri; vital bulgu yok.
- **OSD-815–818:** Astronot verisi değil; donör gözlerle yapılmış bir SANS laboratuvar modeli.

**Önemli:** Hiçbir açık veri seti uçuş sırasında ölçülmüş nabız, SpO2 ya da vücut ısısı vermiyor. Bu değerler modelle simüle edilecek ve sunumda açıkça söylenecek.

## 8. UI/UX

Arayüz, mavi-cyan wireframe astronot görseli (`57110f2f9bef1ff11b47b3e897d3eee2.jpg`) etrafında kurulur. Uzay aracı terminali hissi verir ama metinleri astronot için sade tutar.

**Ekran düzeni (üç sütun):**

| Bölge | İçerik |
| --- | --- |
| Üst bant | Misyon adı, canlı ışık gecikmesi (JPL), NOAA S-ölçeği, ortam seçici (ISS / transit / Mars yüzeyi) |
| Sol sütun | Tıklanabilir wireframe astronot, Health Index halkası, alt sistem ışıkları |
| Orta sütun | Zaman ufku kaydırıcısı (T+0 / T+6 / T+12), gösterge grafikleri, Ghost Twin karşılaştırması |
| Sağ sütun | Kriz paneli (6 buton + reset), öneri kartı ve CMO onay butonu, olay log terminali |
| Alt bant | "Simulation — not medical advice" uyarısı ve veri kaynağı etiketleri |

**Tıklanabilir vücut figürü:**

| Bölge | Gösterilen göstergeler | "Bu ne demek?" kartı |
| --- | --- | --- |
| Kafa / beyin | Uyku, ruh hali, bilişsel yük, CO2 belirtileri | Uyku eksikliği ve CO2'nin karar vermeye etkisi |
| Gözler | SANS izleme (uzun vade) | Kafa içi sıvı kayması ve görme |
| Kalp | HR, HRV, BP | Uzayda tansiyonun neden düştüğü, inişte ne olacağı |
| Akciğer | SpO2, PIO2, giysi basıncı | Basınç düşünce oksijenin nasıl azaldığı |
| Kan / metabolizma | Glukoz, Na, K, BUN (I4 baseline) | 15-15 kuralı, su kaybının performansa etkisi |
| Kemik ve kas | Kemik kaybı (\~%1–1.5/ay), egzersiz uyumu | Egzersizin neden zorunlu olduğu |
| Tüm vücut | Radyasyon dozu, vücut ısısı | Kariyer limiti 600 mSv; barınağın işlevi |

**Renk dili:**

| Anlam | Renk | Hex |
| --- | --- | --- |
| Zemin | Koyu | `#0a0a0c` |
| Bilgi / normal çizgiler | Cyan | `#00d2ff` |
| Nominal (L0) | Yeşil | `#00ff66` |
| Öz-bakım (L1) | Sarı | `#ffd400` |
| CMO onayı (L2) | Turuncu | `#ff8a00` |
| Yer bilgilendirme / acil (L3–L4) | Kırmızı | `#ff0055` |

Yeşil, sarı ve kırmızı renk körlüğünde karışabilir. Her durum renge ek olarak bir ikon ve metin etiketi de taşımalı.

**Ses tasarımı (docx'ten, Web Audio ile):**

- **Arka plan:** 60 Hz sinüs uğultu, düşük ses.
- **Alarm:** 800/1000 Hz kare dalga, 0.4 s aralıkla.
- **Onay:** CMO öneriyi onaylayınca 440 → 880 Hz yükselen ton.
- **Sessiz modu:** Jüri ortamına göre bir tuşla kapatılabilmeli.

Docx'te alarm "Edge-AI otomatik müdahale ettiğinde" susuyordu. Yeni akışta CMO onayı verildiğinde susar.

**Görsel efektler:**

- Kriz anında tehdit rengine göre neon çerçeve ve 150 ms ekran flaşı.
- Yanıp sönen efektlerde sn'de 3 flaşı aşma (fotosensitif epilepsi riski).

**Eğitim katmanı:** Her göstergenin yanında "?" ikonu olacak. Tıklanınca üç şey gösterilir: nasıl ölçülür, normal aralık ve kaynak linki. Bu, challenge'ın "do not assume astronauts know everything" ilkesine birebir karşılık gelir.

## 9. Teknoloji yığını ve repo yapısı

Önerim: Veri hazırlığı Python ile, uygulama statik bir web sayfası olarak yapılsın. Bu seçenek GitHub Pages'te girişsiz bir link verir, internetsiz çalışır ve ses, flaş, tıklanabilir figür gibi özellikleri rahatça yapar. Takım yalnızca Python biliyorsa docx'teki Streamlit yolu da geçerli. Seçim yarınki toplantıda yapılmalı.

| Kriter | A: Statik web (önerilen) | B: Streamlit (docx'teki) |
| --- | --- | --- |
| Dil | Python (veri) + TypeScript/JS (uygulama) | Yalnızca Python |
| Herkese açık link | GitHub Pages, ücretsiz, giriş yok | Streamlit Community Cloud; uykudan uyanması zaman alır |
| İnternetsiz demo | Evet, önbellek JSON'larıyla | Yerel çalışır, kurulum ister |
| Ses ve flaş efektleri | Web Audio ve CSS ile doğrudan | `components.html` ile sınırlı |
| Tıklanabilir vücut figürü | SVG ile doğrudan | Özel bileşen gerekir |
| Her etkileşimde yeniden çizim | Yok | Var; animasyonları bozar |
| Öğrenme eğrisi | Orta | Düşük |

**A seçeneğinin bileşenleri:**

- **Uygulama:** Vite + TypeScript. Vanilla ya da React takıma göre seçilir.
- **Grafik:** Chart.js ya da uPlot.
- **Figür:** Wireframe görselin üzerine SVG bölgeler.
- **Ses:** Web Audio API.
- **Veri hazırlığı:** Python 3 (requests, pandas). API'leri çeker, `public/data/*.json` dosyalarına yazar.
- **Canlı veri:** Tarayıcıdan NOAA ve JPL'e doğrudan istek. Hata olursa önbellekteki JSON kullanılır.

**Repo yapısı (öneri):**

```
astra-twin/
  README.md                 ← İngilizce; kurulum, veri kaynakları, SI kullanım beyanı
  CLAUDE.md                 ← Claude için proje bağlamı
  docs/ARASTIRMA_RAPORU.md  ← kaynaklı araştırma
  scripts/
    fetch_osdr.py           ← OSD-569/575 CSV → baseline JSON
    fetch_radlab.py         ← Eylül 2017 SPE ve ISS DosTel serileri
    fetch_horizons.py       ← hackathon günleri için gecikme tablosu
    fetch_noaa.py           ← son 7 günün örnek verisi (yedek)
  public/data/              ← önbellek JSON'ları (demo internetsiz çalışır)
  src/
    engine/
      thresholds.ts         ← §5'teki tablolar, her değerin yanında kaynak
      state.ts              ← durum vektörü ve zaman adımı
      scenarios/            ← her senaryo bir dosya (§6)
      protocols.ts          ← öneri kartları (ISS formülerisi)
      escalation.ts         ← L0–L4 kuralları
      ghostTwin.ts          ← gecikmeli görünüm ve tahmin
    live/                   ← JPL ve NOAA istemcileri + yedek
    ui/                     ← paneller, figür, grafikler, ses
  tests/engine.test.ts      ← eşik ve eskalasyon testleri
```

**Tasarım kuralı:** Motor (`src/engine/`) arayüzden tamamen ayrı, saf fonksiyonlardan oluşur. Aynı girdi her zaman aynı çıktıyı verir. Bu sayede test edilebilir ve demo her seferinde aynı şekilde oynar.

## 10. Ekip rolleri ve hackathon planı

Teslim penceresi 14 Kasım Cumartesi 09:00'da açılıp 15 Kasım Pazar 23:59'da kapanıyor; toplam \~39 saat. Pazar 18:00'de kod dondurulur. Geriye kalan süre video, slayt ve teslim formu içindir.

**Roller (en fazla 6 kişi; takım küçükse roller birleştirilir):**

| Rol | Sorumluluk | Birleştirilebilir |
| --- | --- | --- |
| Proje lideri / anlatıcı | Kapsam kararları, sunum hikayesi, teslim formu | Bilim sorumlusu |
| Bilim / tıp sorumlusu | Eşikler, senaryolar, kaynak kontrolü, açıklama kartları | Proje lideri |
| Veri mühendisi | OSDR, RadLab, JPL, NOAA scriptleri ve önbellek | Motor geliştirici |
| Motor geliştirici | Durum motoru, senaryolar, eskalasyon, Ghost Twin, testler | Veri mühendisi |
| Arayüz geliştirici | Paneller, grafikler, vücut figürü, ses | Tasarımcı |
| Tasarımcı / video | Görsel dil, 30 saniyelik video, slaytlar, altyazı | Arayüz geliştirici |

**Zaman planı:**

| Zaman | Hedef | Çıktı |
| --- | --- | --- |
| Cmt 09:00–10:00 | Başlangıç | Repo, görev dağılımı; 13 Kasım'da çıkan kaynaklar okunmuş |
| Cmt 10:00–14:00 | Veri ve iskelet | Veri JSON'ları hazır, motorun eşik tablosu, boş üç sütunlu ekran |
| Cmt 14:00–20:00 | MVP | Vital paneli, renk kodları, senaryo 6.1 ve 6.2 çalışıyor, gecikme göstergesi |
| Cmt 20:00 | **Kontrol noktası 1** | Uçtan uca çalışan MVP |
| Paz 08:00–13:00 | Hedef katmanı | Senaryo 6.3–6.6, vücut figürü, Ghost Twin, ses |
| Paz 13:00 | **Kontrol noktası 2** | Tüm senaryolar oynanıyor |
| Paz 13:00–18:00 | Cilalama | Açıklama kartları, testler, GitHub Pages yayını |
| Paz 18:00 | **Kod dondurma** | Yayındaki sürüm |
| Paz 18:00–22:00 | Sunum | 30 saniyelik video + altyazı ya da 7 slayt, README |
| Paz 22:00–23:00 | Teslim | Form dolduruldu; yedek süre 1 saat |

Gece uyku vardır. Uykusuz bir takım pazar günü senaryo 6.3'ü yaşar.

**Hackathon öncesi (şimdi–13 Kasım) yapılabilecekler:** Bu dokümanı okumak, rolleri seçmek, araçları kurmak, NASA kaynaklarını okumak. Proje kodu yazılmaz. Kesin sınırı yerel organizatöre sorun.

## 11. Demo ve sunum akışı

Demo için iki seçenek var: en fazla 30 saniyelik video ya da en fazla 7 slayt (2025 kuralı; 13 Kasım'da teyit edilecek). Video daha etkili, çünkü projenin gücü gerçek zamanlı kriz anında görünüyor.

**30 saniyelik video senaryosu (İngilizce altyazıyla):**

| Saniye | Ekranda | Altyazı |
| --- | --- | --- |
| 0–4 | Mars, Dünya ve canlı gecikme sayacı (ör. 11.4 dk) | "Mars is up to 22 light-minutes from Earth." |
| 4–9 | Nominal ekran, gerçek I4 kan baseline etiketi | "ASTRA-TWIN: a digital twin built on real Inspiration4 crew data." |
| 9–16 | Turuncu buton; RadLab'deki gerçek Mars SPE eğrisi yükseliyor | "A real solar particle event, replayed from NASA data." |
| 16–22 | Öneri kartı: barınağa geç, EVA'yı ertele; CMO onaylar, alarm susar | "It explains the risk and recommends ISS-formulary actions." |
| 22–27 | Ghost Twin: Dünya'nın eski görüntüsü ve T+6 tahmini | "Earth sees the past; the twin forecasts the next 12 hours." |
| 27–30 | Logo ve link | "Built with NASA OSDR, RadLab, NOAA and JPL data." |

**7 slayt alternatifi:**

1. Başlık ve tek cümlelik pitch
2. Problem: ışık gecikmesi ve 5 tehlike
3. Çözüm: mimari diyagramı (§4)
4. NASA verisi: OSDR, RadLab, NOAA, JPL
5. Demo ekran görüntüsü: SPE senaryosu
6. Bilimsel temel: düzeltilen iddialar ve ISS formülerisi
7. Etki ve sonraki adımlar

**Jüri kriterlerine eşleme:**

| Kriter | Demoda nerede görünür |
| --- | --- |
| Impact | Açılıştaki gecikme sayacı ve NASA'nın EIMO hedefi |
| Creativity | Ghost Twin; gerçek SPE verisinin oynatılması |
| Validity | Kaynak etiketleri, ISS formülerisi, "modelled" etiketleri |
| Relevance | NASA verisinin motorun girdisi olması, challenge'ın örnek göstergeleri |
| Presentation | 30 saniyelik tek bir kriz hikayesi |

**Teslim formu için hazırlanacaklar (2025 alanları):** Project Name, High-Level Summary, Demo linki, Final Project linki, Detailed Description, NASA Data, Space Agency Partner & Other Data, Use of AI (2026'da SI beyanı).

Bu projede kodun bir kısmını Claude yazıyor ve araştırmada da Claude kullanıldı. SI beyanında ikisi de belirtilmeli. Wiro ile AI görsel ya da video üretilirse filigran gerekir ve NASA logosu kullanılmaz.

## 12. Riskler, etik ve sınırlamalar

En büyük risk teknik değil, bilimsel: yanlış bir tıbbi iddia Validity puanını düşürür. Bu yüzden ilk taslaktaki aşağıdaki iddialar kaldırıldı.

**Taslaktan kaldırılan iddialar ve nedenleri:**

| İddia | Neden kaldırıldı | Kaynak |
| --- | --- | --- |
| SPE'de potasyum iyodür | SPE protondur; KI yalnızca radyoaktif iyota karşı korur. ISS'te yok. | [CDC](https://www.cdc.gov/radiation-emergencies/treatment/potassium-iodide.html) |
| Radyasyona bağlı hipertansiyonda propranolol | SPE akut hipertansiyon yapmaz; uçuşta tansiyon zaten düşük. ISS'te yok (metoprolol var). | [Norsk 2015](https://doi.org/10.1113/jphysiol.2014.284869), [Diaz 2024](https://doi.org/10.1038/s41526-024-00414-3) |
| Antioksidan kokteyli | İnsanda kanıt yok. 400 IU E vitamini prostat kanseri riskini artırdı (HR 1.17). | [Klein 2011, SELECT](https://doi.org/10.1001/jama.2011.1437) |
| Basınç düşüşünde epinefrin | Doğru müdahale O2 ve basıncı geri sağlamaktır. | OCHMO-TB-003, TB-037 |
| <50 mg/dL'de glukagon | Önce ağızdan glukoz verilir (ADA). Glukagon ISS'te yok. | [ADA 2025](https://doi.org/10.2337/dc25-S006) |
| Melatonin 5 mg, L-triptofan | ISS'teki doz 3 mg; triptofan için kanıt yok. | Diaz 2024, Barger 2014 |
| Saatte %1.5 / EVA'da %8 hidrasyon kaybı | Gerçeğin 5–30 katı. | OCHMO-TB-027, ICES-2021 |
| Giysi içi otomatik transdermal dozlama | Böyle bir NASA donanımı yok. FDA açısından tıbbi cihaz sayılır ve etik sorun doğurur. | [EIMO, Levin 2023](https://doi.org/10.1109/OJEMB.2023.3255513) |

**Etik kurallar:**

- Sistem hiçbir zaman ilaç vermez; yalnızca öneri sunar. Her öneri CMO onayı ister.
- Her öneriyle birlikte kanıt düzeyi ve kaynak gösterilir.
- Ekranda sürekli "Simulation — not medical advice" uyarısı bulunur.
- Inspiration4 mürettebatı yalnızca anonim kimlikle (C001–C004) anılır; gerçek isimlerle eşleştirilmez.
- Docx'teki "CAPT. MUSTAFA P. GÜLDAĞ" kurgusal bir karakter olarak kalabilir, ama gerçek I4 verisi bu isimle gösterilmemeli. Aksi halde gerçek veri gerçek bir kişiye aitmiş gibi görünür.

**Proje riskleri:**

| Risk | Olasılık | Etki | Önlem |
| --- | --- | --- | --- |
| Bir API demo sırasında çalışmaz | Orta (DONKI testte çöktü) | Yüksek | Tüm veriler önbellekte; canlı veri yalnızca bir artı |
| Kapsam taşar, hiçbir senaryo bitmez | Yüksek | Yüksek | MVP önce; Cumartesi 20:00 kontrol noktası |
| 2026 kuralları 2025'ten farklı çıkar | Orta | Orta | 13 Kasım'da rehberleri oku, planı güncelle |
| Video 30 saniyeyi aşar | Orta | Global jürilemeden çıkar | Pazar 18:00'de senaryoyu sabitle, saniye saniye kurgula |
| Jüri simüle veriyi gerçek sanır, sonra fark eder | Düşük | Yüksek | Her grafikte "real" ya da "modelled" etiketi |
| Lisanssız kod kullanılır | Düşük | Yüksek | Aegis Disaster Twin gibi lisanssız repolardan kod kopyalanmaz |

**Bu araştırmada doğrulanamayanlar (doğrulanmış gibi kullanma):**

- Hassler 2014'ün birincil metni (0.64–0.67 mSv/gün değeri ikincil kaynaklardan).
- NASA-STD-3001 Vol 1 Rev C'deki radyasyon değişiklikleri.
- 3,035 kcal ve 145–155 mmHg değerleri (arama özetinden).
- Parsons & Townsend 2000'deki 1.4 Gy/sa değeri (özetten).
- EMU içme torbasının güncel boyutu (kaynaklar 620 mL ile 1,070 mL arasında çelişiyor).
- NASA'nın ışık tedavisi, kompresyon giysisi ve ISS formülerisindeki analjezikler.
- AHA ACLS epinefrin dozu.
- 2026 jüri ve teslim rehberleri (13 Kasım'da yayınlanacak).

## 13. Yarınki toplantı: kararlar ve sorular

Toplantıdan 7 kararla çıkılmalı. Her birinin yanında önerim var; takım farklı karar verebilir.

**Verilecek kararlar:**

- [ ] **Konumlandırma:** "Otonom tedavi" yerine "karar destek + CMO onayı" mi? *Öneri: evet. Hem challenge metnine hem NASA EIMO yaklaşımına uyuyor.*
- [ ] **Teknoloji:** Statik web mi, Streamlit mi? *Öneri: statik web; takım yalnızca Python biliyorsa Streamlit.*
- [ ] **Senaryo sayısı:** 6 senaryonun hepsi mi, yoksa MVP için 6.1 + 6.2 mi? *Öneri: önce 6.1 ve 6.2, kalanlar hedef katmanında.*
- [ ] **Demo biçimi:** 30 saniyelik video mu, 7 slayt mı? *Öneri: video.*
- [ ] **Rol dağılımı:** §10'daki tablodan kim ne alıyor?
- [ ] **Senaryo ortamı:** Mars yüzeyi mi, transit mi, ISS mi? *Öneri: Mars yüzeyi ana senaryo; gerçek SPE verisi orada var.*
- [ ] **Karakter:** Kurgusal astronot adı ve görev adı ne olacak?

**Organizatöre sorulacak sorular:**

- [ ] Hackathon öncesinde tam olarak ne serbest? Araştırma, tasarım taslakları, repo açmak, veri indirme?
- [ ] Yerel etkinlik mi, Universal Event mi? Yerel etkinlik için en az 1, Universal için en az 2 üye gerekiyor.
- [ ] Sunum yerinde yapılacak mı, yoksa yalnızca çevrimiçi teslim mi?

**Toplantıdan önce herkesin okuması gerekenler:** Bu dokümanın §1, §2, §6 ve §13 bölümleri. Toplam \~15 dakika.

**13 Kasım'da yapılacaklar:**

- [ ] Challenge'ın Resources sekmesindeki NASA ve partner kaynaklarını oku.
- [ ] 2026 Project Submission Guide ve Judging & Awards Guide'ı oku; video, slayt ve AI kurallarını teyit et.
- [ ] Gerekirse bu dokümanı ve CLAUDE.md'yi güncelle.

## 14. Kaynakça

Tüm kaynaklar 9 Kasım 2026'da açılıp okundu. Yalnızca özetten alınanlar §12'de işaretli.

**Space Apps**

- [Challenge: Health Monitoring Software](https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/)
- [Space Apps 2026](https://www.spaceappschallenge.org/2026/) · [Participant FAQs](https://www.spaceappschallenge.org/resources/participant-faqs/) · [Awards](https://www.spaceappschallenge.org/awards/)
- [2025 Judging & Awards Guide (arşiv)](https://web.archive.org/web/20251128144656id_/https://www.spaceappschallenge.org/resources/judging-awards-guide/) · [2025 Project Submission Guide (arşiv)](https://web.archive.org/web/20251229205712id_/https://www.spaceappschallenge.org/resources/project-submission-guide/)
- [2014 Astronaut Health Monitoring System](https://2014.spaceappschallenge.org/project/astronaut-health-monitoring-system/) · [2025 Global Winners](https://www.nasa.gov/learning-resources/stem-engagement-at-nasa/nasa-announces-2025-international-space-apps-challenge-global-winners/)

**NASA ve standartlar**

- [NASA Human Research Program](https://www.nasa.gov/hrp/) · [5 tehlike](https://www.nasa.gov/hrp/hazards/) · [Human Research Roadmap](https://humanresearchroadmap.nasa.gov/risks/)
- [HRP Research Approach Plans (Haziran 2026)](https://www.nasa.gov/wp-content/uploads/2026/06/hrp-rap-for-web-62026.pdf)
- [Moon to Mars Human Health & Performance](https://www.nasa.gov/wp-content/uploads/2024/01/human-health-and-performance.pdf)
- [OCHMO-TB-003 Habitable Atmosphere](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-003-habitable-atmosphere.pdf) · [TB-004 CO2](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf) · [TB-020 Radiation](https://www.nasa.gov/wp-content/uploads/2023/03/radiation-protection-technical-brief-ochmo.pdf) · [TB-027 Water](https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf) · [TB-037 DCS](https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-037-decompression-sickness.pdf)
- [Ewert & Downs, ICES-2021 metabolik model](https://ntrs.nasa.gov/api/citations/20205010692/downloads/ICES-2021_Metabolic_Paper-Final.pdf) · [ICES-2025 CO2 acil durum bantları](https://ntrs.nasa.gov/api/citations/20250006273/downloads/ICES-2025-402.pdf)
- [EIMO ConOps](https://ntrs.nasa.gov/citations/20240001402) · [NTRS 20230014168 (OSDR sunumu)](https://ntrs.nasa.gov/citations/20230014168)
- [NSSDC Mars fact sheet](https://nssdc.gsfc.nasa.gov/planetary/factsheet/marsfact.html) · [NOAA space weather scales](https://www.spaceweather.gov/noaa-scales-explanation)
- [CDC: potasyum iyodür](https://www.cdc.gov/radiation-emergencies/treatment/potassium-iodide.html) · [CDC: akut radyasyon sendromu](https://www.cdc.gov/radiation-emergencies/hcp/clinical-guidance/ars.html)

**Veri ve API'ler**

- [NASA OSDR](https://osdr.nasa.gov/bio/repo) · [I4 veri setleri haberi](https://www.nasa.gov/osdr-latest-news-i4-mission-datasets/) · [OSD-569](https://osdr.nasa.gov/bio/repo/data/studies/OSD-569) · [OSD-575](https://osdr.nasa.gov/bio/repo/data/studies/OSD-575)
- [OSDR RadLab API](https://visualization.osdr.nasa.gov/radlab/gui/data-api) · [JPL Horizons API](https://ssd-api.jpl.nasa.gov/doc/horizons.html) · [NOAA SWPC JSON](https://services.swpc.noaa.gov/json/) · [NASA Open APIs / DONKI](https://api.nasa.gov/)

**Hakemli makaleler**

- [Diaz et al. 2024, ISS formülerisi](https://doi.org/10.1038/s41526-024-00414-3)
- [Norsk et al. 2015, uçuş içi kan basıncı](https://doi.org/10.1113/jphysiol.2014.284869)
- [Stahn et al. 2017, vücut ısısı](https://doi.org/10.1038/s41598-017-15560-w)
- [Barger et al. 2014, astronot uykusu](<https://doi.org/10.1016/S1474-4422(14)70122-X>) · [Dijk et al. 2001, melatonin](https://doi.org/10.1152/ajpregu.2001.281.5.R1647)
- [Zeitlin et al. 2013, MSL yolculuk radyasyonu](https://doi.org/10.1126/science.1235989) · [Hassler et al. 2014, Mars yüzeyi](https://doi.org/10.1126/science.1244797)
- [Klein et al. 2011, SELECT](https://doi.org/10.1001/jama.2011.1437) · [Kennedy et al. 2008, antioksidanlar (fare)](https://doi.org/10.1667/RR1296.1)
- [ADA Standards of Care 2025](https://doi.org/10.2337/dc25-S006) · [Levin et al. 2023, EIMO](https://doi.org/10.1109/OJEMB.2023.3255513)
- [Overbey et al. 2024, SOMA](https://doi.org/10.1038/s41586-024-07639-y)
