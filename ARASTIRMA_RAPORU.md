# ASTRA-TWIN — Doğrulanmış Araştırma Raporu

> **Update 2026-11-14:** The verified NASA data source inventory is in [`NASA_DATA_SOURCES.md`](NASA_DATA_SOURCES.md). Values listed in its §9 supersede this report: CO2 congestion threshold, light-time, NOAA exclusion, DONKI endpoint, Chang'e-4 in RadLab, compression garments, and the ISS formulary source.

**Tarih:** 2026-11-09
**Yöntem:** Canlı web sayfaları, NASA teknik dokümanları, PubMed ve canlı API sorguları. Her iddianın yanında kaynağı var.

**Etiketler:**
- ✅ DOĞRU
- ⚠️ KISMEN DOĞRU
- ❌ YANLIŞ
- ❓ DOĞRULANAMADI

Doğrulanamayan şeyler açıkça belirtildi. Varsayım yapılmadı.

---

## 1. Challenge: "Create Health Monitoring Software for Astronauts on Space Missions"

URL: https://www.spaceappschallenge.org/2026/challenges/health-monitoring-software/

- **Zorluk:** Intermediate
- **Konular:** Human Exploration, Software, Space Exploration
- **Ana hedef (alıntı):** "create a visual tool that helps astronauts assess their own health during space missions and provides guidance on treatments and health improvements."
- **Girdi:** Her türlü olabilir: klavye, fare, kamera görüntüsü.
- **Veri:** Her türlü sağlık göstergesi olabilir, örneğin mg/dL kan şekeri ya da ultrason görüntüsü.
- **Örnek göstergeler:** Kan basıncı, nabız, kan şekeri, vücut ısısı, günlük uyku saati, ruh hali.
- **Rehber sorular:**
  - Uzayda hangi ölçümler yapılabilir?
  - Muayene neden gerekli?
  - Tehlikeler göstergeleri nasıl etkiler?
  - Bir sonuç ne zaman mürettebatın ya da **yerdeki uçuş doktorlarının** dikkatini gerektirir?
  - Kritik durumu önlemek için ne yapılabilir?
- **UI vurgusu:** Animasyon ve etkileşim. Örnek olarak tıklanabilir bir insan figürü veriliyor; her vücut bölümü kendi işlevini ve sağlık durumunu gösteriyor.
- **Opsiyonel fikirler:**
  - Ortam seçimi: uzay aracı, istasyon, rover, Ay/gezegen üssü.
  - Rutin ölçüm hatırlatmaları.
  - Oyunlaştırma.
  - Zamana göre dinamik grafikler.
  - Renk kodları.
  - "Do not assume that astronauts know everything" ilkesi: Ölçümler ve riskler basit talimatlarla açıklanmalı.
- **Kaynaklar sekmesi:** "Resources will be available on Friday, November 13." Kaynaklar 13 Kasım'da yeniden kontrol edilmeli.

### Taslaktaki iddiayla tutarsızlıklar

- **"Otonom" kelimesi metinde yok.** Metin yerdeki uçuş doktorlarına iletmeyi bekliyor, yani Ghost Twin ve eskalasyon fikri bu metne uyuyor.
- **Edge-AI ve dijital ikiz geçmiyor.** Bunlar bizim ekstramız; asıl iş **görsel öz-değerlendirme aracı** olmak.
- **Kelime tercihi:** Metinde "Medical/restorative" değil "treatments and health improvements" ve önleme geçiyor.

## 2. Space Apps 2026 kuralları ve tarihler

Kaynaklar:
- https://www.spaceappschallenge.org/2026/
- https://www.spaceappschallenge.org/resources/participant-faqs/ (5 Kasım 2026)

### Tarihler

- **Hackathon:** 14–15 Kasım 2026.
- **Teslim penceresi:** 14 Kasım Cumartesi 09:00'dan 15 Kasım Pazar 23:59'a kadar, yerel saatle.
- **13 Kasım:** Kaynaklar, Project Submission Guide ve Judging & Awards Guide yayınlanır.
- **Sonuçlar:** Aralık 2026'da Global Nominees/Finalists, Ocak 2027'de Global Winners açıklanır.

### Kurallar

- **Takım:** En fazla 6 kişi. Her takım tek challenge seçebilir.
- **⚠️ "Teams are not allowed to begin working on the challenges prior to the hackathon."** Kod geliştirmeye 14 Kasım'dan önce başlama. Sınırı yerel organizatöre sor.
- **Yapay zekâ (2026'da adı "Super Intelligence (SI)"):** Kullanmak serbest. Teslimde SI araçlarının nasıl kullanıldığını açıklamak **zorunlu**.

### Global Awards (10 adet)

https://www.spaceappschallenge.org/awards/

Best Use of Science, Best Use of Data, Best Use of Technology, Galactic Impact, Best Mission Concept, Most Inspirational, Best Use of Storytelling, Global Connection, Art & Technology, Local Impact.

### Jüri kriterleri — yalnızca 2025 rehberinden doğrulandı

2026 rehberi henüz yayında değil. Arşiv: https://web.archive.org/web/20251128144656id_/https://www.spaceappschallenge.org/resources/judging-awards-guide/

Her kriter 1–5 arası puanlanıyor, ağırlık verilmemiş:

- **Impact:** Problemin büyüklüğü ve projenin ne kadar geniş kitleye yardım ettiği ya da ilham verdiği.
- **Creativity:** Özgünlük.
- **Validity:** Bilimsel temel, teknik fizibilite, tamlık, kullanılabilirlik.
- **Relevance:** Challenge'ı tam karşılaması ve **NASA verisini çekirdek bileşen** olarak kullanması ("rather than a superficial addition").
- **Presentation:** Hikâye anlatımı ve demo.

### Teslim gereksinimleri — 2025 rehberi

Arşiv: https://web.archive.org/web/20251229205712id_/https://www.spaceappschallenge.org/resources/project-submission-guide/

- **Demo:** En fazla **30 sn video** veya en fazla **7 slayt** (başlık dahil). Sınır aşılırsa global jürilemeye giremez.
- **Barındırma:** Herkese açık, giriş gerektirmeyen bir link (YouTube, Drive, GitHub).
- **Dil:** Tümüyle İngilizce. Videoda İngilizce altyazı olmalı.
- **NASA verisi:** Global ödül için zorunlu.
- **2025'teki AI kuralları:**
  - AI ile üretilen görsel ve videoda görünür filigran olmalı.
  - AI ile üretilen kod, veri ve ses metinde belirtilmeli.
  - Üretilen içerikte NASA markası/logosu kullanılmamalı.

## 3. Benzer ve ilham verici projeler

| Proje | Durum | Not |
|---|---|---|
| 2014 "Astronaut Health Monitoring System" | ✅ Var | Arduino, EEG/ECG ve sıcaklık sensörlerinden yer istasyonuna veri. Kod: https://github.com/NazS2/Astronaut-health-monitoring-system (AFL-3.0). Yer merkezli izleme; 2026 ise öz-değerlendirme istiyor. |
| EmergencyTwin voice | ✅ Var, ama uzay veya sağlıkla ilgili değil | Bina acil durum ikizi. Tek bir index.html, Three.js ve AssemblyAI Voice Agent kullanıyor. https://github.com/Hamna-Munir/emergencytwin-voice. README'de MIT yazıyor ama LICENSE dosyası yok. |
| Aegis Disaster Twin | ✅ Var, ama uzay veya sağlıkla ilgili değil | FastAPI, LangGraph çoklu ajan, FAISS RAG, Three.js, Docker. https://github.com/Nikhil-creat/aegis-disaster-twin. **Lisans yok, kodu kopyalanamaz.** |
| SpaceGenes+ (2025 Global Winner, Best Use of Science) | ✅ | Radyasyon ve mikrogravitenin hücreye etkisini gösteren dashboard. |
| Skylab (2022 Global Nominee) | ✅ | GeneLab verisiyle astronot hastalık tahmini. https://github.com/imostafizur/CSE498R |
| Spacemed (2018 Global Nominee) | ✅ | Vital bulgu ve biyomekanik izleme. |

Astronot dijital ikizi üzerine kurulu bir Space Apps kazananı **bulunamadı**. Bu durum özgünlük açısından avantaj.

---

## 4. Fizyolojik değerlerin doğrulaması (docx taslağı)

| Taslak | Etiket | Düzeltme ve kaynak |
|---|---|---|
| Mars–Dünya gecikmesi 4–24 dk | ⚠️ | Fiziksel hesap **~3.1–22.3 dk tek yön** veriyor (NSSDC'ye göre uzaklık 55.7–401.3 milyon km). NASA HHP white paper: "up to 22 minutes". |
| UI'da sabit "22 MINUTES" | ⚠️ | Bu bir maksimum değer. **9 Kasım 2026'daki gerçek değer 11.37 dk** (JPL Horizons, canlı sorgu). Canlı hesaplanmalı. |
| HR 72, aralık 40–180 | ⚠️ | ISS'te 24 saatlik nabız değişmiyor (Norsk 2015, doi:10.1113/jphysiol.2014.284869). Dinlenmede 50–100 dışı "uyarı" sayılmalı. |
| HRV 65 ms, kritik <20 | ❓ | Metrik (SDNN/RMSSD) belirtilmemiş. Literatürde "kritik <20" eşiği yok. Normlar: 24 saatlik SDNN 141±39 ms, RMSSD 27±12 ms (Task Force 1996). Kişisel baseline ve z-skoru kullanılmalı. |
| BP 120/80 | ⚠️ | Uçuşta 24 saatlik tansiyon **~8/9 mmHg düşüyor**, kalp debisi %35–41 artıyor (Norsk 2015). Uçuş içi baseline yaklaşık **112/71** olmalı. Asıl risk ortostatik intolerans. |
| SpO2 %99, kritik <88 | ⚠️ | ISS kabini 14.7 psia ve %21 O2, yani deniz seviyesine eşdeğer; normal değer %96–99. <90 hipoksemi, 88 klinik konvansiyon. NASA eşiği PIO2 olarak tanımlıyor: deniz seviyesinde 149 mmHg, alt limit 122 mmHg (1 saatlik zaman ağırlıklı ortalama, OCHMO-TB-003). |
| Vücut ısısı 37.0, aralık 35–41.5 | ⚠️ | Uzun görevde dinlenme ısısı **~2.5 ayda ~+1 °C** artıyor (Stahn 2017, doi:10.1038/s41598-017-15560-w). |
| Glukoz 90, hipoglisemi <60 | ❌ | ADA 2025: **Seviye 1 <70, Seviye 2 <54, Seviye 3 = başkasının yardımı gereken ağır durum** (doi:10.2337/dc25-S006). |
| Hidrasyon saatte %1.5 azalıyor | ❌ | Gerçekte günde 2.5 L, yani vücut kütlesinin saatte ~%0.13'ü. **Vücut ağırlığının %2.5'i kaybedilince performans düşüyor.** Kaynak: OCHMO-TB-027 https://www.nasa.gov/wp-content/uploads/2023/03/water-technical-brief-ochmo.pdf |
| EVA'da saatte %8 | ❌ | Fizyolojik olarak imkânsız (~6.5 kg/sa). Gerçekçi değer **saatte %0.3–1.2**, zirve egzersizde ~1 L/sa (Ewert & Downs, ICES-2021). |
| EVA su ihtiyacı | ✅ (referans) | Saatte **≥240 mL** su [V2 11030] ve saatte **+200 kcal** [V2 7004]. |
| EMU içme torbası | ⚠️ | Kaynaklar çelişiyor: 21 oz (~620 mL, NASA 2019), 32 oz (~0.95 L, Etlin 2024) ve 1,070 mL. |

### Radyasyon

| Taslak | Etiket | Düzeltme |
|---|---|---|
| Arka plan 0.05 mSv/sa | ⚠️ | Değer konuma göre değişiyor. **ISS ~0.015**, **Mars yolculuğu ~0.075** (MSL RAD 1.8 mSv/gün, Zeitlin 2013), **Mars yüzeyi ~0.027 mSv/sa** (Hassler 2014: 0.64–0.67 mSv/gün). |
| SPE zirvesi 75 mSv/sa | ⚠️ | Hafif zırhlı bir habitatta büyük bir olay için makul. Ağustos 1972 SPE'sinde 1 g/cm² Al arkasında deri dozu zirvesi ~1.4 Gy/sa (Parsons & Townsend 2000). Hangi organ, hangi büyüklük (Gy veya Sv) ve hangi zırh olduğu belirtilmeli. Bu değerde 75 mSv/sa × 3.3 sa olay başı 250 mSv limitine ulaşıyor. |
| Kariyer limiti 600 mSv | ✅ | [V1 4030], yaş ve cinsiyetten bağımsız. https://www.nasa.gov/wp-content/uploads/2023/03/radiation-protection-technical-brief-ochmo.pdf |
| 30 günlük limitler | ⚠️ eski | Rev B'de yerini **[V1 4031]** aldı: tasarım referansı SPE (Ekim 1989) için <250 mSv efektif doz. Vol 1 Rev C da var; kontrol edilmeli. |
| NOAA S-ölçeği | ✅ (referans) | S1 = ≥10 MeV ≥10 pfu. S3 (10³): EVA'dan kaçınma öneriliyor. S4 (10⁴): EVA'da kaçınılmaz tehlike. S5 (10⁵). https://www.spaceweather.gov/noaa-scales-explanation |
| Gerçek müdahale | ✅ | Barınak: ~10 cm su eşdeğeri zırh ekleniyor. Zırh SPE dozunu ~%50 azaltıyor. Uyarı, dozimetri, ALARA. |

### Taslaktaki ilaç ve beslenme reçeteleri

Referans: 2023 ISS formülerisi, 106 ilaç (Diaz et al. 2024, npj Microgravity, doi:10.1038/s41526-024-00414-3, https://pmc.ncbi.nlm.nih.gov/articles/PMC11266549/).

| Taslak | Etiket | Gerçek |
|---|---|---|
| Propranolol (radyasyona bağlı hipertansiyon) | ❌ | SPE akut hipertansiyon yapmıyor. Uçuşta tansiyon zaten düşük, beta-bloker ortostatik riski artırabilir. ISS'te propranolol yok, **metoprolol** var. |
| Potasyum iyodür | ❌ | SPE protonlardan oluşuyor; radyoaktif iyot yok. CDC'ye göre KI yalnızca radyoiyota karşı korur. ISS'te yok. |
| Antioksidan kokteyli (C 1000 mg, E 400 IU, ALA, Se) | ❓/zararlı olabilir | Kanıt yalnızca farelerde var (Kennedy 2008). SELECT çalışmasında **tam olarak 400 IU E vitamini prostat kanseri riskini artırdı** (HR 1.17, Klein 2011 JAMA). NASA radyoprotektör tanımlamayı 2033'te planlıyor. Prodromal semptomlar için ISS'te **ondansetron** var. |
| Glukagon <50'de | ⚠️ | ADA önce **15 g ağızdan glukoz, 15 dk sonra tekrar ölçüm** öneriyor. Glukagon yalnızca ağızdan alınamıyorsa veriliyor. ISS'te glukagon yok. Sağlıklı mürettebatta hipoglisemi olasılığı düşük. |
| Dekstroz/maltodekstrin jeli | ⚠️ | ADA'nın 15 g kuralına uyuyor. EMU içme torbasında zaten az miktarda glukoz var. |
| Elektrolit | ⚠️ | NASA dönüşte tuz tableti ve ≥1 L su kullanıyor. Görev içinde rutin bir doz standardı yok. |
| BCAA 5 g | ❓ | NASA'da kriz kullanımı yok. Beslenme olarak ele alınmalı. |
| Melatonin 5 mg | ⚠️ | ISS'te **3 mg** tablet var. Uzay mekiğinde tek dozların %73'ü zolpidem, %2'si melatonin. 0.3 mg melatonin uykuyu iyileştirmedi (Dijk 2001). ISS'te mürettebat gecede 6.09 saat uyuyor ve %75'i uyku ilacı kullanıyor (Barger 2014). |
| L-triptofan | ❓ | ISS'te yok, uzay kanıtı yok. |
| Basınç düşüşünde epinefrin | ❌ | Doğru müdahale: **O2 maskesi veya giysi O2'si ve basıncı geri sağlamak**. DKS'de rekompresyon ve %100 O2. Epinefrin ISS'te var ama anafilaksi (0.3 mg IM) ve kalp durması için. |
| Giysi içi otomatik transdermal ilaç | ❓ yok | Böyle bir NASA donanımı yok. En yakını ISS'teki deneysel nanokanal implant (CRS-26). NASA'nın yönü EIMO (Earth-Independent Medical Operations): kararı insanın verdiği bir karar destek sistemi. Otomatik dozlama FDA açısından tıbbi cihaz sayılır ve etik sorun doğurur. |

**ISS formülerisinde olan ve öneri olarak kullanılabilecek ilaçlar:** ondansetron, prometazin, metoprolol, lisinopril, nitrogliserin, aspirin, apiksaban, enoksaparin, asetazolamid, epinefrin, nalokson, flumazenil, ketamin, deksametazon, zolpidem 5 mg, modafinil 200 mg, melatonin 3 mg.

### Basınç düşüşüne ait gerçek değerler

- EMU 4.3 psi, %100 O2. ISS kabini 14.7 psia, %21 O2.
- Secondary Oxygen Pack açık devre arındırma modunda ≥30 dk O2 sağlıyor.
- Ebullizm eşiği 0.91 psi. 2.5 psia altında ek O2 yetersiz kalabilir.
- Habitat basıncı süresiz olarak 5.0–15.0 psia aralığında tutulabilir. Basınç değişimi en fazla 13.5 psi/dk olmalı.
- Model zinciri şöyle kurulmalı: **giysi basıncı → PIO2 → SpO2** (OCHMO-TB-003, TB-037).

### Simülatörde kullanılabilecek NASA referans değerleri

| Parametre | Değer | Kaynak |
|---|---|---|
| Enerji | 3,035 kcal/gün; EVA'da saatte +200 kcal | NASA-STD-3001 V2 7003/7004 (arama özetinden) |
| Metabolik yük (82 kg) | O2 0.82 kg/gün, CO2 1.04 kg/gün; ısı üretimi uykuda 88 W, nominal 139 W, egzersizde ≤826 W | Ewert & Downs ICES-2021 |
| Su | 2.5 L/gün; EVA'da saatte +240 mL; vücut ağırlığının %2.5'i kaybedilince performans düşüyor | OCHMO-TB-027 |
| CO2 | 1 saatlik ortalama ≤3 mmHg; 7 günlük ortalama <2.5 mmHg; 2.8–4.5 mmHg'de baş ağrısı | OCHMO-TB-004 |
| O2 | PIO2 alt limiti 122 mmHg; normoksi aralığı 145–155 mmHg | OCHMO-TB-003/002 |
| Uyku | ISS'te 6.1 sa/gece | Barger 2014 |
| Kemik kaybı | ayda ~%1–1.5 | NASA HHP white paper |

### 5 tehlike (RIDGE) ve karşı önlemler

Kaynak: https://www.nasa.gov/hrp/hazards/ ve https://www.nasa.gov/wp-content/uploads/2024/01/human-health-and-performance.pdf

- **Radiation (radyasyon):** Zırh, barınak, uyarı sistemi, dozimetri.
- **Isolation (izolasyon):** Özel alan, iletişim, davranışsal sağlık önlemleri.
- **Distance (uzaklık):** Karar destek sistemi, gemi içi tıp kapasitesi (EIMO).
- **Gravity (yerçekimi):** Egzersiz. SANS için VTC, B-kompleks ve GLP-1 araştırılıyor.
- **Hostile environment (düşmanca/kapalı ortam):** ECLSS, CO2 kontrolü, DKS tedavisi.

HRP Research Approach Plans (Haziran 2026) Behavioral Health, Team ve EIHSO alanlarında "semi-autonomous monitoring" ve dashboard hedefliyor (ara hedef 2031, nihai 2034). **ASTRA-TWIN tam bu boşluğa oturuyor.**

---

## 5. NASA OSDR veri setlerinin doğrulaması

| ID | Gerçekte ne | Kullanılabilir sayısal veri? | Taslaktaki iddia |
|---|---|---|---|
| OSD-569 | I4 tam kan: **CBC** + RNA-seq/WGS | ✅ `LSDS-7_Complete_Blood_Count_CBC_TRANSFORMED.csv`: 4 mürettebat × 7 zaman noktası (L-92 … R+194), referans aralıklarıyla | I4 olduğu doğru, "güneş patlaması için ideal" iddiası ❌ (yalnızca uçuş öncesi ve sonrası, radyasyon ölçümü yok) |
| OSD-570 | I4 PBMC tek hücre omik | ❌ omik | Yalnızca omik veri |
| OSD-571 | I4 plazma proteomik/metabolomik | ❌ omik | Yalnızca omik veri |
| OSD-575 | I4 **CMP + sitokin** panelleri | ✅ **En iyisi.** Glukoz, Na, K, Cl, BUN, kreatinin, eGFR, Ca, ALT/AST + IL-6, IL-10, IFN-γ, CRP, fibrinojen (pg/mL) | Hipoglisemi ve dehidrasyon senaryosuna uygun |
| OSD-687 | I4 T-hücre histon modifikasyonu | ❌ n=1, yalnızca R+194, FASTQ erişimi kısıtlı | ❌ "bağışıklık verisi" değil |
| OSD-815–818 | **Yer çalışması:** donör gözleriyle ex vivo SANS laboratuvar modeli (TRISH) | Çoğunlukla görsel; 816'da laboratuvar basınç logları | ❌ Astronot SANS fizyolojisi **değil** |

Diğer I4 setleri: OSD-572 (mikrobiyom), 573 (kapsül), 574 (deri), 630 (dışkı), 656 (idrar inflamasyon paneli). Ax-1 setleri: OSD-903 ve 943.

**Hiçbir açık veri seti uçuş içi nabız, SpO2 veya vücut ısısı vermiyor.** Bu değerler fizyolojik modelle simüle edilmeli; baseline ve referans aralıkları I4 CBC/CMP'den alınmalı. Sunumda bu açıkça söylenmeli.

---

## 6. Doğrulanmış API'ler (9 Kasım 2026'da canlı test edildi)

| Öncelik | Kaynak | Uç nokta | Anahtar |
|---|---|---|---|
| 1 | JPL Horizons (gecikme) | `https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='499'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='OBSERVER'&CENTER='500@399'&START_TIME='2026-11-09'&STOP_TIME='2026-11-10'&STEP_SIZE='1%20d'&QUANTITIES='20,21'` → `1-way_down_LT` (dk); `$$SOE`…`$$EOE` arası parse edilir | Yok |
| 2 | NOAA SWPC (canlı uzay havası) | `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json`, `.../xrays-1-day.json`, `.../xray-flares-latest.json`, `https://services.swpc.noaa.gov/products/noaa-scales.json`, `.../products/alerts.json` | Yok |
| 3 | OSDR RadLab (gerçek doz) | `https://visualization.osdr.nasa.gov/radlab/api/?celestial_body=Mars&timestamp%3E=2017-09-08&timestamp%3C2017-09-16&absorbed_dose_rate&format=csv` | Yok |
| 4 | OSD-575 CMP | `https://osdr.nasa.gov/geode-py/ws/studies/OSD-575/download?source=datamanager&file=LSDS-8_Comprehensive_Metabolic_Panel_CMP_TRANSFORMED.csv` | Yok |
| 4b | OSD-575 sitokin | `...&file=LSDS-8_Multiplex_serum_immune_EvePanel_TRANSFORMED.csv` | Yok |
| 5 | OSD-569 CBC | `https://osdr.nasa.gov/geode-py/ws/studies/OSD-569/download?source=datamanager&file=LSDS-7_Complete_Blood_Count_CBC_TRANSFORMED.csv` | Yok |
| 6 | NASA DONKI (FLR/SEP/CME) | `https://api.nasa.gov/DONKI/SEP?startDate=...&endDate=...&api_key=KEY` | Gerekli. **Testte 503 döndü**, yedek yol şart |

### RadLab'deki gerçek olay: Mars'ta Eylül 2017 SPE'si

Günlük ortalama doz oranları (MSL-RAD):

| Tarih | Doz oranı | Not |
|---|---|---|
| 9 Eylül | 7.95 μGy/sa | |
| 11 Eylül | 17.87 μGy/sa | Zirve 23.18 |
| 12 Eylül | 12.92 μGy/sa | |
| 14 Eylül | 6.56 μGy/sa | Forbush düşüşü |

Bu seri güneş patlaması senaryosunda **gerçek veri** olarak oynatılabilir. RadLab'de ISS DosTel, CRaTER (LRO), BioSentinel ve Chang'e-4 verileri de var.

### OSDR API notları

- Metadata: `https://osdr.nasa.gov/osdr/data/osd/meta/<id>`
- Dosya listesi: `https://osdr.nasa.gov/osdr/data/osd/files/<id>?page=1&size=100`. Sayfalar **1'den başlıyor**.
- İndirme: `https://osdr.nasa.gov` + `remote_url`, `curl -L` ile.
- Arama: `https://osdr.nasa.gov/osdr/data/search?term=inspiration4&type=cgene&size=50`
- CSV dosyaları UTF-8 BOM ile başlıyor, `utf-8-sig` ile decode edilmeli.
- OSD-569 TRANSFORMED dosyasında hemoglobin `hemoglobin_value_percent` olarak etiketlenmiş ama birimi g/dL.

### Erişilemeyen veya açık olmayan kaynaklar

- **NASA LSDA:** Yalnızca resmi başvuruyla erişiliyor.
- **Twins Study:** Açık indirme bulunamadı.
- **Polaris Dawn:** Açık veri seti yok.
- **ISS SRAG dozimetrisi:** Toplu açık indirme yok; yerine RadLab kullanılmalı.
- **SOMA atlası** (Overbey 2024 Nature): Omik veri, vital bulgu yok.

---

## 7. Doğrulanamayanlar (doğrulandı diye kullanma)

- Hassler 2014 birincil makalesi (değer ikincil kaynaklardan alındı).
- AHA ACLS epinefrin dozu.
- NASA aydınlatma karşı önlemleri.
- NASA-STD-3001 Vol 1 Rev C metni.
- Güncel EMU içme torbası boyutu.
- 3,035 kcal ve 145–155 mmHg değerleri (arama özetinden alındı).
- Parsons & Townsend 2000'deki 1.4 Gy/sa değeri (özetten alındı).
- 2026 jüri ve teslim rehberleri (13 Kasım'da yayınlanacak).

## 8. Diğer kaynaklar

**NASA:**
- HRP: https://www.nasa.gov/hrp/
- RAP PDF: https://www.nasa.gov/wp-content/uploads/2026/06/hrp-rap-for-web-62026.pdf
- Human Research Roadmap: https://humanresearchroadmap.nasa.gov/risks/ (22 risk; ayrı bir ARS riski artık yok)
- NTRS 20230014168: Bu bir OSDR tanıtım sunumu, fizyoloji dokümanı değil. Veri kaynağı olarak atıf yap. https://ntrs.nasa.gov/citations/20230014168
- EIMO ConOps: https://ntrs.nasa.gov/citations/20240001402
- OCHMO teknik özetleri:
  - TB-003: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-003-habitable-atmosphere.pdf
  - TB-004: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-004-carbon-dioxide.pdf
  - TB-037: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-037-decompression-sickness.pdf
  - TB-002: https://www.nasa.gov/wp-content/uploads/2023/12/ochmo-tb-002-eclss.pdf

**Hakemli makaleler:**
- Levin 2023 (EIMO): doi:10.1109/OJEMB.2023.3255513
- Zeitlin 2013: doi:10.1126/science.1235989
- Hassler 2014: doi:10.1126/science.1244797
- Overbey 2024: doi:10.1038/s41586-024-07639-y
- Garrett-Bakelman 2019: doi:10.1126/science.aau8650

**Diğer:**
- CDC KI: https://www.cdc.gov/radiation-emergencies/treatment/potassium-iodide.html
- CDC ARS: https://www.cdc.gov/radiation-emergencies/hcp/clinical-guidance/ars.html
