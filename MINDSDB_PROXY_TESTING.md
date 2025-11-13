# MindsDB Spring Boot Proxy - Testing Guide

## Overview
The Spring Boot application now exposes MindsDB through secure REST endpoints at `/api/mindsdb/*`

## Authentication
All MindsDB endpoints require HTTP Basic Authentication.

### Default Test Credentials
For testing, you can add users to `application.properties`:

```properties
# Add to application.properties for testing
spring.security.user.name=admin
spring.security.user.password=admin123
```

## Available Endpoints

### 1. Health Check
Check if MindsDB connection is working:
```bash
curl -u admin:admin123 http://localhost:8080/api/mindsdb/health
```

### 2. Execute Custom Query
Run any SELECT, DESCRIBE, SHOW, CREATE MODEL, or CREATE DATABASE query:
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SHOW DATABASES"}'
```

### 3. List Models
View all MindsDB models:
```bash
curl -u admin:admin123 http://localhost:8080/api/mindsdb/models
```

### 4. List Databases
View all connected databases:
```bash
curl -u admin:admin123 http://localhost:8080/api/mindsdb/databases
```

### 5. Check Fraud (ML Prediction)
Check if a registration is suspicious:
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/check-fraud \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "username": "testuser"}'
```

## Security Features

### Query Validation
The proxy automatically validates queries:
- ✅ **Allowed**: SELECT, DESCRIBE, SHOW, CREATE MODEL, CREATE DATABASE
- ❌ **Blocked**: DROP, DELETE, TRUNCATE

### Example - Blocked Query
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "DROP DATABASE email_db"}'

# Response: 400 Bad Request
# {"error": "Query validation failed. Only SELECT, DESCRIBE, SHOW, and CREATE MODEL/DATABASE are allowed."}
```

### SQL Injection Protection
All inputs are sanitized to prevent SQL injection attacks.

## Setup Steps

### Step 1: Add Test User (Optional)
Add to `application.properties`:
```properties
spring.security.user.name=admin
spring.security.user.password=admin123
```

### Step 2: Restart Spring Boot
```bash
./gradlew bootRun
```

### Step 3: Connect MindsDB to MySQL
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "CREATE DATABASE email_db WITH ENGINE = '\''mysql'\'', PARAMETERS = {\"host\": \"host.docker.internal\", \"port\": 3306, \"database\": \"email_reg_db\", \"user\": \"emailapp\", \"password\": \"emailapp123\"}"
  }'
```

### Step 4: Verify Connection
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM email_db.registered_emails LIMIT 5"}'
```

## Integration Examples

### From Angular Frontend
```typescript
const headers = new HttpHeaders({
  'Authorization': 'Basic ' + btoa('admin:admin123'),
  'Content-Type': 'application/json'
});

this.http.post('http://localhost:8080/api/mindsdb/query', 
  { query: 'SELECT * FROM mindsdb.models' },
  { headers }
).subscribe(response => {
  console.log('Models:', response);
});
```

### From Android App
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
```

### From Python
```python
import requests
from requests.auth import HTTPBasicAuth

response = requests.post(
    'http://localhost:8080/api/mindsdb/query',
    json={'query': 'SHOW DATABASES'},
    auth=HTTPBasicAuth('admin', 'admin123')
)
print(response.json())
```

## Error Handling

### 401 Unauthorized
```bash
curl http://localhost:8080/api/mindsdb/health
# Returns 401 - authentication required
```

### 400 Bad Request
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": ""}'
# Returns 400 - query cannot be blank
```

### 500 Internal Server Error
```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM nonexistent.table"}'
# Returns 500 - MindsDB error
```

## Production Recommendations

1. **Use Strong Passwords**: Replace default credentials
2. **HTTPS Only**: Deploy with SSL/TLS certificates
3. **Rate Limiting**: Add rate limiting to prevent abuse
4. **Audit Logging**: Log all MindsDB queries for compliance
5. **User Management**: Implement proper user database instead of application.properties
6. **API Keys**: Consider JWT tokens or API key authentication
7. **Input Validation**: Add additional validation based on your use case

## Next Steps

1. Test the health endpoint
2. Connect MindsDB to your MySQL database
3. Create fraud detection model
4. Integrate with your Angular/Android apps
5. Set up proper user authentication
