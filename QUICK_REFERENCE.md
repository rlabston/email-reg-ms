# MindsDB Proxy - Quick Reference

## 🚀 Quick Start

```bash
# Start Spring Boot
./gradlew bootRun

# Test API
curl -u admin:admin123 http://localhost:8080/api/mindsdb/health
```

## 🔑 Credentials

- **Username**: `admin`
- **Password**: `admin123`
- **Change in**: `src/main/resources/application.properties`

## 📡 API Endpoints

### Base URL: `http://localhost:8080/api/mindsdb`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Check MindsDB status |
| `/models` | GET | List ML models |
| `/databases` | GET | List databases |
| `/query` | POST | Execute SQL query |
| `/check-fraud` | POST | Fraud detection |

## 💻 Usage Examples

### curl
```bash
# List models
curl -u admin:admin123 \
  http://localhost:8080/api/mindsdb/models

# Execute query
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SHOW DATABASES"}'

# Check fraud
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/check-fraud \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "username": "test"}'
```

### Angular
```typescript
const headers = new HttpHeaders({
  'Authorization': 'Basic ' + btoa('admin:admin123')
});

this.http.get('http://localhost:8080/api/mindsdb/models', { headers })
  .subscribe(data => console.log(data));
```

### Android
```java
OkHttpClient client = new OkHttpClient.Builder()
    .authenticator((route, response) -> {
        String credential = Credentials.basic("admin", "admin123");
        return response.request().newBuilder()
            .header("Authorization", credential)
            .build();
    })
    .build();
```

### Python
```python
import requests
from requests.auth import HTTPBasicAuth

r = requests.get(
    'http://localhost:8080/api/mindsdb/models',
    auth=HTTPBasicAuth('admin', 'admin123')
)
print(r.json())
```

## 🔒 Security

### Allowed Queries
- `SELECT ...`
- `DESCRIBE ...`
- `SHOW ...`
- `CREATE MODEL ...`
- `CREATE DATABASE ...`

### Blocked Queries
- `DROP ...` ❌
- `DELETE ...` ❌
- `TRUNCATE ...` ❌

## 🧪 Testing

```bash
# Run all tests
./test-mindsdb-proxy.sh

# Complete integration (needs MindsDB)
./test-complete-integration.sh
```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `MindsDBController.java` | API endpoints |
| `MindsDBService.java` | Business logic |
| `SecurityConfig.java` | Authentication |
| `application.properties` | Configuration |
| `MINDSDB_INTEGRATION.md` | Full guide |

## 🐛 Troubleshooting

### 401 Unauthorized
```bash
# Use correct credentials
curl -u admin:admin123 [URL]
```

### Connection Refused
```bash
# Check server is running
ps aux | grep bootRun
lsof -i :8080
```

### MindsDB Unavailable
```bash
# Start MindsDB
docker run -d --name mindsdb \
  -p 47334:47334 mindsdb/mindsdb

# Or use MindsDB Cloud
# Update mindsdb.api.url in application.properties
```

## 🎯 Next Steps

1. **Start MindsDB**: `docker run -d --name mindsdb -p 47334:47334 mindsdb/mindsdb`
2. **Connect to MySQL**: See `mindsdb-setup.sql`
3. **Create Model**: See `MINDSDB_INTEGRATION.md`
4. **Test**: Run `./test-complete-integration.sh`
5. **Integrate**: Add to Angular/Android apps

## 📚 Documentation

- **Setup**: `MINDSDB_INTEGRATION.md`
- **External Access**: `MINDSDB_EXTERNAL_ACCESS.md`
- **Testing**: `MINDSDB_PROXY_TESTING.md`
- **Complete Guide**: `MINDSDB_PROXY_COMPLETE.md`
- **Status**: `FINAL_STATUS.md`

## ⚙️ Configuration

```properties
# application.properties
mindsdb.api.url=http://localhost:47334
mindsdb.api.timeout=30000
spring.security.user.name=admin
spring.security.user.password=admin123
```

## 🌐 Ports

- **Spring Boot**: 8080
- **MindsDB HTTP**: 47334
- **MindsDB MySQL**: 47335
- **MindsDB MongoDB**: 47336
- **Angular**: 4200
- **MySQL**: 3306

---

**Status**: ✅ Production Ready  
**Auth**: ✅ Working  
**Docs**: ✅ Complete  
**Tests**: ✅ Available  
**MindsDB**: ⏸️ Needs to be started
