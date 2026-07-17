# Secure Mail Analyzer Backend

E-posta içeriği ve linkleri kural tabanlı olarak analiz eden, oltalama (phishing) ve sosyal mühendislik risklerini fark ettirmeyi amaçlayan **eğitim odaklı** bir güvenlik farkındalık platformunun backend servisi.

> Bu proje gerçek saldırı yapmak için değil; kullanıcıların şüpheli içerikleri tanımasına yardımcı olmak için geliştirilmiştir. Testlerde yalnızca örnek / yapay içerikler kullanılmalıdır.

---

## Özellikler

- Kullanıcı kaydı ve JWT ile giriş
- E-posta içeriği risk analizi (aciliyet dili, şifre/kişisel veri isteği, marka taklidi, ek dosya, sahte link vb.)
- Link güvenlik kontrolleri (HTTPS, kısa URL, IP adresi, şüpheli kelime, marka taklidi)
- Risk skoru (0–100) ve seviye: **LOW / MEDIUM / HIGH**
- Analiz geçmişi ve detay görüntüleme
- Admin dashboard: toplam analiz, risk dağılımı, en sık risk tipleri
- Role-based yetkilendirme (`USER` / `ADMIN`)
- Merkezi hata yönetimi
- Docker Compose ile tek komutta çalıştırma
- Kubernetes YAML dosyaları ile local cluster deploy

---

## Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| Backend | Java 17, Spring Boot 3.5 |
| Güvenlik | Spring Security, JWT (jjwt), BCrypt |
| Veri | Spring Data JPA, PostgreSQL 16 |
| Container | Docker, Docker Compose |
| Orkestrasyon | Kubernetes (Docker Desktop K8s) |

---

## Proje Yapısı

```
secure-mail-analyzer-backend/
├── src/main/java/com/securemail/analyzer/
│   ├── controller/     # REST endpoint'ler
│   ├── service/        # İş kuralları (analiz, auth, admin)
│   ├── entity/         # JPA entity'ler
│   ├── repository/     # Veri erişimi
│   ├── security/       # JWT filter, UserDetails
│   ├── config/         # Security, CORS, admin seed
│   ├── dto/            # Request / Response
│   ├── enums/          # Role, RiskLevel, RiskType...
│   └── exception/      # GlobalExceptionHandler
├── Dockerfile
├── docker-compose.yml
├── k8s/                # Kubernetes manifest'leri
└── pom.xml
```

---

## Gereksinimler

- Java 17+
- Maven 3.9+ (veya projedeki `mvnw`)
- Docker Desktop
- Kubernetes (Docker Desktop → Settings → Kubernetes → Enable)
- `kubectl`

---

## 1) Yerel geliştirme (IntelliJ / Maven)

### PostgreSQL'i ayağa kaldır

```bash
docker compose up -d postgres
```

Veritabanı: `localhost:5433`  
DB: `secure_mail_db` / User: `securemail` / Password: `securemail123`

### Uygulamayı çalıştır

IntelliJ'den `SecureMailAnalyzerBackendApplication` çalıştır veya:

```bash
./mvnw spring-boot:run
```

API: `http://localhost:8080`

İlk açılışta varsayılan admin oluşturulur:

| Alan | Değer |
|------|--------|
| Email | `admin@securemail.com` |
| Şifre | `Admin123!` |

---

## 2) Docker Compose ile çalıştırma

Backend + PostgreSQL birlikte container'da çalışır:

```bash
docker compose up -d --build
```

Kontrol:

```bash
docker compose ps
docker compose logs -f backend
```

Durdurma:

```bash
docker compose down
```

> Not: Veriyi silmek istemiyorsan `docker compose down -v` kullanma.

---

## 3) Kubernetes ile çalıştırma

### Önkoşul

1. Docker Desktop açık olsun
2. Kubernetes enabled olsun (`kubectl get nodes` → `Ready`)
3. Backend image lokalde olsun:

```bash
docker build -t secure-mail-analyzer-backend:local .
```

4. Compose çalışıyorsa port çakışmasın diye durdur:

```bash
docker compose down
```

### Deploy

```bash
kubectl apply -f k8s/
kubectl get pods
```

Pod'lar `Running` olana kadar bekle (backend ilk seferde Postgres hazır olmadan birkaç kez restart edebilir; ardından ayağa kalkar).

### API'ye erişim (port-forward)

Bir terminalde açık bırak:

```bash
kubectl port-forward service/backend 8080:8080
```

Sonra Postman / curl ile `http://localhost:8080` kullan.

### Faydalı komutlar

```bash
kubectl get pods
kubectl get svc
kubectl logs -f deployment/backend
kubectl logs -f deployment/postgres

# Kaynakları sil
kubectl delete -f k8s/
```

### k8s/ içeriği

| Dosya | Açıklama |
|-------|----------|
| `postgres-secret.yaml` | DB kullanıcı / şifre |
| `postgres-pvc.yaml` | Kalıcı disk |
| `postgres-deployment.yaml` | Postgres pod |
| `postgres-service.yaml` | Cluster içi `postgres:5432` |
| `backend-configmap.yaml` | JDBC URL |
| `backend-deployment.yaml` | Spring Boot pod |
| `backend-service.yaml` | Cluster içi `backend:8080` |

---

## API Endpoint'leri

### Auth (public)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/auth/register` | Kullanıcı kaydı |
| POST | `/api/auth/login` | JWT token alır |

**Register örneği:**

```json
{
  "fullName": "Test User",
  "email": "user@test.com",
  "password": "User123!"
}
```

**Login örneği:**

```json
{
  "email": "admin@securemail.com",
  "password": "Admin123!"
}
```

Protected endpoint'lerde header:

```
Authorization: Bearer <token>
```

### Analiz (authenticated)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/analyses/mail` | E-posta analizi |
| POST | `/api/analyses/link` | Link analizi |
| GET | `/api/analyses/history` | Kullanıcının geçmişi |
| GET | `/api/analyses/{id}` | Analiz detayı |

**Analiz body:**

```json
{
  "input": "Acil! Şifrenizi hemen güncelleyin. https://bit.ly/ornek"
}
```

### Kullanıcı

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/users/me` | Giriş yapan kullanıcı |

### Admin (sadece ADMIN)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/admin/dashboard` | Toplam analiz, risk dağılımı, top risk tipleri |

---

## Analiz Mantığı (özet)

Sistem LLM kullanmaz; **kural tabanlı (heuristic)** çalışır.

### Mail riskleri

- Aciliyet / baskı dili
- Şifre veya giriş bilgisi isteği
- Kişisel / finansal bilgi isteği
- Linke yönlendirme
- Marka / kurum taklidi
- Ek dosya uyarısı
- Yazım hatası ipuçları

### Link riskleri

- HTTPS eksikliği
- Kısa URL servisleri
- Şüpheli path kelimeleri
- Marka adı kullanımı
- IP adresi ile host

Skor 0–100 aralığında toplanır; **LOW ≤ 30**, **MEDIUM ≤ 70**, **HIGH ≥ 71**.

---

## Varsayılan hesaplar / bağlantılar

| Ortam | URL |
|-------|-----|
| API | `http://localhost:8080` |
| Postgres (compose / local) | `localhost:5433` |
| Postgres (container / k8s ağı) | `postgres:5432` |

| Admin | Değer |
|-------|--------|
| Email | `admin@securemail.com` |
| Password | `Admin123!` |

> Production'da varsayılan admin şifresi ve JWT secret mutlaka değiştirilmelidir.

---

## Ekran Görüntüleri

Frontend tamamlandığında buraya Postman / UI ekran görüntüleri eklenecektir:

- Login / kayıt
- Mail ve link analiz sonucu
- Analiz geçmişi
- Admin dashboard

---

## Ekip

| Üye        | Görev |
|------------|--------|
| Ömer Kumek | Backend, Docker, Kubernetes |

*(Frontend ve diğer görev dağılımı proje tamamlandıkça güncellenecektir.)*

---


