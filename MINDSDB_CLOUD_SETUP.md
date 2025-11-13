# MindsDB Cloud Setup Guide

## Quick Solution to Port Binding Issues

Instead of fighting with local MindsDB port conflicts, use **MindsDB Cloud** - it's free, instant, and production-ready!

## 🚀 5-Minute Setup

### Step 1: Create MindsDB Cloud Account

1. Go to: https://cloud.mindsdb.com/register
2. Sign up (free account)
3. Verify your email
4. Login to your dashboard

### Step 2: Get Your API Credentials

1. After login, click your profile (top right)
2. Go to **Settings** → **API Keys**
3. Create a new API key
4. Copy the API key (you'll need this)

### Step 3: Update Spring Boot Configuration

Edit `src/main/resources/application.properties`:

```properties
# MindsDB Cloud Configuration (recommended)
mindsdb.api.url=https://cloud.mindsdb.com
mindsdb.api.timeout=30000

# MindsDB Cloud Authentication
# Option 1: API Key (recommended)
mindsdb.api.key=YOUR_API_KEY_HERE

# Option 2: Email/Password
mindsdb.cloud.email=your-email@example.com
mindsdb.cloud.password=your-password
```

**For now, use the simpler approach without API key:**

```properties
# MindsDB Cloud - No Auth Required for Testing
mindsdb.api.url=https://cloud.mindsdb.com
mindsdb.api.timeout=30000
```

### Step 4: Restart Spring Boot

```bash
cd /home/ubuntu/dev/mobile/spring/email-reg-ms
./gradlew bootRun
```

### Step 5: Test Connection

```bash
# Test health endpoint
curl -u admin:admin123 http://localhost:8080/api/mindsdb/health

# List databases
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SHOW DATABASES"}'

# List models
curl -u admin:admin123 http://localhost:8080/api/mindsdb/models
```

## 📊 Connect to Your MySQL Database

### Step 1: Create Database Connection

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "CREATE DATABASE email_db WITH ENGINE=\"mysql\", PARAMETERS={\"host\":\"YOUR_MYSQL_HOST\",\"port\":3306,\"database\":\"email_reg_db\",\"user\":\"emailapp\",\"password\":\"emailapp123\"}"
  }'
```

**Important:** Replace `YOUR_MYSQL_HOST` with:
- Your public IP address, OR
- Use a service like ngrok to expose localhost:3306

**Using ngrok (recommended for testing):**

```bash
# Install ngrok
sudo snap install ngrok

# Expose MySQL
ngrok tcp 3306

# Use the ngrok URL in your CREATE DATABASE command
# Example: 0.tcp.ngrok.io:12345
```

### Step 2: Verify Connection

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM email_db.registered_emails LIMIT 5"}'
```

## 🤖 Create Fraud Detection Model

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "CREATE MODEL fraud_detector FROM email_db (SELECT email, username, created_at FROM registered_emails) PREDICT is_suspicious"
  }'
```

### Check Model Status

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "DESCRIBE fraud_detector"}'
```

Wait for status to be "complete" (may take a few minutes).

## 🧪 Test Fraud Detection

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/check-fraud \
  -H "Content-Type: application/json" \
  -d '{
    "email": "suspicious@temp-mail.com",
    "username": "hacker123"
  }'
```

## 🔧 Alternative: MindsDB Cloud Direct Access

You can also use MindsDB Cloud directly in your browser:

1. Login to https://cloud.mindsdb.com
2. Go to **SQL Editor**
3. Run queries directly:

```sql
-- Connect to MySQL
CREATE DATABASE email_db 
WITH ENGINE = "mysql",
PARAMETERS = {
  "host": "your-host",
  "port": 3306,
  "database": "email_reg_db",
  "user": "emailapp",
  "password": "emailapp123"
};

-- View data
SELECT * FROM email_db.registered_emails LIMIT 5;

-- Create model
CREATE MODEL fraud_detector
FROM email_db
  (SELECT email, username, created_at FROM registered_emails)
PREDICT is_suspicious;

-- Check model
DESCRIBE fraud_detector;

-- Make prediction
SELECT email, username, is_suspicious, is_suspicious_confidence
FROM fraud_detector
WHERE email = 'test@example.com' AND username = 'testuser';
```

## 🌐 MindsDB Cloud Advantages

✅ **No Installation** - No Python, no dependencies, no port conflicts  
✅ **Always Available** - 99.9% uptime  
✅ **Auto-Scaling** - Handles heavy workloads  
✅ **Free Tier** - Generous limits for development  
✅ **Pre-trained Models** - Access to GPT, Claude, Hugging Face  
✅ **Data Connectors** - MySQL, PostgreSQL, MongoDB, etc.  
✅ **GUI Interface** - Visual query editor and model management  

## 📱 Integration Examples

### Angular Frontend

```typescript
export class MindsDBService {
  private apiUrl = 'http://localhost:8080/api/mindsdb';
  private auth = 'Basic ' + btoa('admin:admin123');

  checkFraud(email: string, username: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/check-fraud`, 
      { email, username },
      { headers: { 'Authorization': this.auth } }
    );
  }

  queryMindsDB(query: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/query`,
      { query },
      { headers: { 'Authorization': this.auth } }
    );
  }
}
```

### Android Client

```java
public interface MindsDBApi {
    @POST("mindsdb/check-fraud")
    Call<FraudResponse> checkFraud(
        @Header("Authorization") String auth,
        @Body FraudRequest request
    );
    
    @POST("mindsdb/query")
    Call<QueryResponse> query(
        @Header("Authorization") String auth,
        @Body QueryRequest request
    );
}

// Usage
String auth = Credentials.basic("admin", "admin123");
mindsDBApi.checkFraud(auth, new FraudRequest(email, username))
    .enqueue(new Callback<FraudResponse>() {
        @Override
        public void onResponse(Call<FraudResponse> call, Response<FraudResponse> response) {
            if (response.isSuccessful()) {
                boolean isSuspicious = response.body().isSuspicious();
                // Handle result
            }
        }
    });
```

## 🔒 Security Best Practices

### For Production:

1. **Change Default Password**
   ```properties
   # application.properties
   spring.security.user.name=your-admin-username
   spring.security.user.password=your-strong-password
   ```

2. **Use Environment Variables**
   ```bash
   export MINDSDB_URL=https://cloud.mindsdb.com
   export MINDSDB_API_KEY=your-api-key
   export SPRING_SECURITY_USER_PASSWORD=your-password
   ```

3. **Enable HTTPS**
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=your-keystore-password
   ```

4. **Use JWT Instead of Basic Auth**
   - Add Spring Security JWT dependency
   - Implement JWT token generation/validation
   - Use tokens for API authentication

## 📊 Monitoring

Monitor your MindsDB Cloud usage:

1. Login to https://cloud.mindsdb.com
2. Go to **Dashboard**
3. View:
   - API calls
   - Model predictions
   - Database connections
   - Resource usage

## 🆘 Troubleshooting

### Connection Issues

**Error:** `MindsDB service is not available`

**Solutions:**
1. Check MindsDB Cloud status: https://status.mindsdb.com
2. Verify `mindsdb.api.url` is set to `https://cloud.mindsdb.com`
3. Check your internet connection
4. Try direct browser access: https://cloud.mindsdb.com

### Authentication Issues

**Error:** `401 Unauthorized`

**Solutions:**
1. Check if you're using the correct API key
2. Verify email/password if using credential auth
3. Check MindsDB Cloud account is active

### Database Connection Issues

**Error:** `Cannot connect to MySQL database`

**Solutions:**
1. Use ngrok to expose MySQL: `ngrok tcp 3306`
2. Check MySQL allows remote connections:
   ```sql
   GRANT ALL PRIVILEGES ON email_reg_db.* TO 'emailapp'@'%';
   FLUSH PRIVILEGES;
   ```
3. Check firewall allows port 3306
4. Verify credentials are correct

## 🎉 Summary

MindsDB Cloud solves all local installation issues:

- ❌ No more port binding conflicts
- ❌ No Python environment management
- ❌ No dependency hell
- ✅ Works immediately
- ✅ Free tier available
- ✅ Production-ready infrastructure

**Your Spring Boot proxy is ready to use with MindsDB Cloud!** 🚀

---

**Next Steps:**

1. ✅ Sign up for MindsDB Cloud
2. ✅ Update application.properties
3. ✅ Restart Spring Boot
4. ✅ Test connection
5. ✅ Connect to MySQL
6. ✅ Create fraud detection model
7. ✅ Test predictions
8. ✅ Integrate with Angular/Android

**All your code is ready - just point it to MindsDB Cloud!**
