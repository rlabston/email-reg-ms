# MindsDB Spring Boot Proxy - Complete Setup

## ✅ What's Been Created

### 1. Configuration Files
- **`application.properties`** - Added MindsDB connection settings
  ```properties
  mindsdb.api.url=http://localhost:47334
  mindsdb.api.timeout=30000
  spring.security.user.name=admin
  spring.security.user.password=admin123
  ```

### 2. DTO Classes
- **`MindsDBQueryRequest.java`** - Request DTO with query validation
- **`MindsDBQueryResponse.java`** - Response DTO for MindsDB results

### 3. Service Layer
- **`MindsDBService.java`** - Core service with:
  - ✅ Query validation (allows SELECT, DESCRIBE, SHOW, CREATE MODEL/DATABASE)
  - ✅ SQL injection prevention
  - ✅ Blocks dangerous operations (DROP, DELETE, TRUNCATE)
  - ✅ Fraud detection helper method
  - ✅ RestTemplate integration

### 4. Controller Layer
- **`MindsDBController.java`** - REST endpoints:
  - `POST /api/mindsdb/query` - Execute custom SQL queries
  - `GET /api/mindsdb/models` - List all ML models
  - `GET /api/mindsdb/databases` - List connected databases
  - `POST /api/mindsdb/check-fraud` - Check if registration is suspicious
  - `GET /api/mindsdb/health` - Health check endpoint

### 5. Security Configuration
- **`SecurityConfig.java`** - Updated to:
  - ✅ Require authentication for `/api/mindsdb/**` endpoints
  - ✅ Enable HTTP Basic authentication
  - ✅ Keep `/api/emails/**` public
  - ✅ Maintain CORS for Angular frontend

### 6. Application Configuration
- **`RegisterEmail.java`** - Added RestTemplate bean for HTTP requests

### 7. Documentation
- **`MINDSDB_INTEGRATION.md`** - Full integration guide
- **`MINDSDB_EXTERNAL_ACCESS.md`** - External access options
- **`MINDSDB_PROXY_TESTING.md`** - Testing guide
- **`mindsdb-setup.sql`** - SQL commands for MindsDB setup
- **`test-mindsdb-proxy.sh`** - Automated test script

## 🚀 How to Use

### Start the Server
```bash
cd /home/ubuntu/dev/mobile/spring/email-reg-ms
./gradlew bootRun
```

Wait for: `Started RegisterEmail in X seconds`

### Test the Proxy
```bash
# Run automated tests
./test-mindsdb-proxy.sh

# Or manual tests:

# 1. Health check
curl -u admin:admin123 http://localhost:8080/api/mindsdb/health

# 2. List databases
curl -u admin:admin123 http://localhost:8080/api/mindsdb/databases

# 3. Execute custom query
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SHOW DATABASES"}'
```

### Connect MindsDB to MySQL Database
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{ 
    "query": "CREATE DATABASE email_db WITH ENGINE = '\''mysql'\'', PARAMETERS = {\"host\": \"host.docker.internal\", \"port\": 3306, \"database\": \"email_reg_db\", \"user\": \"emailapp\", \"password\": \"emailapp123\"}"
  }'
```

### Query Your Database via MindsDB
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM email_db.registered_emails LIMIT 5"}'
```

## 🔒 Security Features

### Authentication
- All MindsDB endpoints require HTTP Basic Authentication
- Default credentials: `admin` / `admin123` (**CHANGE IN PRODUCTION!**)
- Public endpoints (`/api/emails/**`) remain accessible without auth

### Query Validation
```java
// ✅ Allowed queries
SELECT * FROM mindsdb.models
DESCRIBE email_db.registered_emails
SHOW DATABASES
CREATE MODEL fraud_detector FROM email_db ...
CREATE DATABASE email_db WITH ENGINE = 'mysql' ...

// ❌ Blocked queries
DROP DATABASE email_db
DELETE FROM mindsdb.models  
TRUNCATE TABLE ...
```

### SQL Injection Protection
- All user inputs are sanitized
- Single quotes escaped automatically
- Pattern matching for dangerous commands

## 📊 Available Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/mindsdb/health` | ✅ Yes | Check MindsDB connection |
| GET | `/api/mindsdb/models` | ✅ Yes | List all ML models |
| GET | `/api/mindsdb/databases` | ✅ Yes | List connected databases |
| POST | `/api/mindsdb/query` | ✅ Yes | Execute custom SQL query |
| POST | `/api/mindsdb/check-fraud` | ✅ Yes | Check if user is suspicious |
| POST | `/api/emails/register` | ❌ No | Register new email (public) |

## 🔗 Integration Examples

### Angular Frontend
```typescript
import { HttpClient, HttpHeaders } from '@angular/common/http';

constructor(private http: HttpClient) {}

queryMindsDB(query: string) {
  const headers = new HttpHeaders({
    'Authorization': 'Basic ' + btoa('admin:admin123'),
    'Content-Type': 'application/json'
  });

  return this.http.post(
    'http://localhost:8080/api/mindsdb/query',
    { query },
    { headers }
  );
}

// Usage
this.queryMindsDB('SHOW DATABASES').subscribe(
  result => console.log('Databases:', result),
  error => console.error('Error:', error)
);
```

### Android App
```java
OkHttpClient client = new OkHttpClient.Builder()
    .authenticator((route, response) -> {
        String credential = Credentials.basic("admin", "admin123");
        return response.request().newBuilder()
            .header("Authorization", credential)
            .build();
    })
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8080/api/")
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();

MindsDBService service = retrofit.create(MindsDBService.class);
```

### Python Script
```python
import requests
from requests.auth import HTTPBasicAuth

response = requests.post(
    'http://localhost:8080/api/mindsdb/query',
    json={'query': 'SELECT * FROM mindsdb.models'},
    auth=HTTPBasicAuth('admin', 'admin123')
)

print(response.json())
```

## 🎯 Next Steps

1. **Test the proxy**: Run `./test-mindsdb-proxy.sh`
2. **Connect to MySQL**: Use the CREATE DATABASE command above
3. **Create ML models**: Follow `mindsdb-setup.sql` for examples
4. **Integrate with apps**: Add MindsDB queries to Angular/Android
5. **Secure for production**: 
   - Change default password
   - Enable HTTPS
   - Implement proper user management
   - Add rate limiting

## 📝 Files Created

```
/home/ubuntu/dev/mobile/spring/email-reg-ms/
├── src/main/
│   ├── java/com/technet7/microsvc/email/
│   │   ├── controller/MindsDBController.java
│   │   ├── service/MindsDBService.java
│   │   ├── dto/MindsDBQueryRequest.java
│   │   ├── dto/MindsDBQueryResponse.java
│   │   ├── config/SecurityConfig.java (updated)
│   │   └── RegisterEmail.java (updated)
│   └── resources/application.properties (updated)
├── MINDSDB_INTEGRATION.md
├── MINDSDB_EXTERNAL_ACCESS.md  
├── MINDSDB_PROXY_TESTING.md
├── mindsdb-setup.sql
└── test-mindsdb-proxy.sh
```

## ✨ Features Summary

- ✅ Secure proxy to MindsDB via Spring Boot
- ✅ HTTP Basic Authentication
- ✅ Query validation and SQL injection prevention
- ✅ RESTful API endpoints
- ✅ CORS enabled for web frontends
- ✅ Fraud detection integration ready
- ✅ Complete documentation
- ✅ Automated test script
- ✅ Multi-platform support (Angular, Android, Python, curl)

## 🐛 Troubleshooting

### 401 Unauthorized
```bash
# Make sure you're using the correct credentials
curl -u admin:admin123 http://localhost:8080/api/mindsdb/health
```

### Connection Refused
```bash
# Check if Spring Boot is running
ps aux | grep bootRun

# Check if port 8080 is listening
lsof -i :8080
```

### MindsDB Not Available
```bash
# Check if MindsDB is running
lsof -i :47334

# Test MindsDB directly
curl http://localhost:47334/api/sql/query
```

### 400 Bad Request (Invalid Query)
```bash
# Check your query is allowed
# Only SELECT, DESCRIBE, SHOW, CREATE MODEL, CREATE DATABASE allowed
# DROP, DELETE, TRUNCATE are blocked
```

## 🔐 Production Checklist

- [ ] Change default password in `application.properties`
- [ ] Enable HTTPS/SSL
- [ ] Implement JWT tokens instead of Basic Auth
- [ ] Add rate limiting
- [ ] Set up proper user database
- [ ] Configure firewall rules
- [ ] Add audit logging
- [ ] Monitor MindsDB health
- [ ] Set up backups
- [ ] Configure proper CORS origins

---

**Status**: ✅ Complete and Ready to Test

**Authentication**: admin / admin123

**Base URL**: http://localhost:8080/api/mindsdb

**Test Script**: `./test-mindsdb-proxy.sh`
