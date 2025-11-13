# MindsDB Integration Guide

## Overview
MindsDB adds AI/ML capabilities to your email registration system, enabling:
- **Fraud detection**: Identify suspicious registration patterns
- **User analytics**: Predict user behavior and engagement
- **Anomaly detection**: Flag unusual registration spikes or patterns
- **Time series forecasting**: Predict future registration trends

## Setup Instructions

### 1. Access MindsDB Web UI
Open your browser to: http://localhost:47334

### 2. Connect to Your Database
In the MindsDB SQL Editor, run:
```sql
CREATE DATABASE email_db
WITH ENGINE = 'mysql',
PARAMETERS = {
    "host": "host.docker.internal",
    "port": 3306,
    "database": "email_reg_db",
    "user": "emailapp",
    "password": "emailapp123"
};
```

**Note**: Use `host.docker.internal` instead of `localhost` when connecting from MindsDB Docker to your host MySQL.

### 3. Verify Connection
```sql
SELECT * FROM email_db.registered_emails LIMIT 5;
```

### 4. Create ML Models
All SQL commands are in `mindsdb-setup.sql`. Key models:

#### Fraud Detection
```sql
CREATE MODEL fraud_detector
FROM email_db (
    SELECT email, username, registration_date
    FROM registered_emails
)
PREDICT is_suspicious;
```

#### Check Training Status
```sql
SELECT status FROM mindsdb.models WHERE name = 'fraud_detector';
```

## Usage Examples

### Detect Suspicious Registration
```sql
SELECT 
    email,
    username,
    is_suspicious,
    is_suspicious_confidence
FROM fraud_detector
WHERE email = 'test@example.com' 
AND username = 'testuser';
```

### List All Suspicious Users
```sql
SELECT * FROM suspicious_registrations
WHERE is_suspicious_confidence > 0.8;
```

### Domain Analysis
```sql
SELECT 
    SUBSTRING_INDEX(email, '@', -1) as domain,
    COUNT(*) as registration_count
FROM email_db.registered_emails
GROUP BY domain
ORDER BY registration_count DESC;
```

## Integration with Spring Boot

### Option 1: Query MindsDB via MySQL Protocol
Add to `application.properties`:
```properties
# MindsDB MySQL connection
mindsdb.url=jdbc:mysql://localhost:47335/mindsdb
mindsdb.username=mindsdb
mindsdb.password=
```

### Option 2: HTTP API (Recommended)
MindsDB exposes HTTP endpoints at `http://localhost:47334/api/sql/query`

Example Spring service:
```java
@Service
public class FraudDetectionService {
    
    @Value("${mindsdb.api.url:http://localhost:47334}")
    private String mindsdbUrl;
    
    private final RestTemplate restTemplate;
    
    public FraudDetectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public boolean isSuspicious(String email, String username) {
        String query = String.format(
            "SELECT is_suspicious, is_suspicious_confidence " +
            "FROM fraud_detector " +
            "WHERE email='%s' AND username='%s'",
            email, username
        );
        
        // Call MindsDB HTTP API
        Map<String, Object> request = Map.of("query", query);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            mindsdbUrl + "/api/sql/query",
            request,
            Map.class
        );
        
        // Parse response
        // ... implement response parsing
        return false; // placeholder
    }
}
```

## Use Cases for Email Registration

### 1. Real-time Fraud Detection
- Check each registration against ML model before saving
- Block or flag high-confidence suspicious registrations
- Reduce spam and fake accounts

### 2. User Behavior Prediction
- Predict which users will become active vs. dormant
- Optimize onboarding for high-engagement predictions
- Identify at-risk users early

### 3. Registration Trend Forecasting
- Predict future registration volumes
- Plan infrastructure scaling
- Detect anomalies (bot attacks, viral growth)

### 4. Email Domain Intelligence
- Identify disposable email domains
- Track corporate vs. personal email patterns
- Flag bulk registrations from same domain

## Next Steps

1. **Test the connection**: Run the setup SQL in MindsDB UI
2. **Train initial models**: Execute predictor creation commands
3. **Integrate with backend**: Add MindsDB queries to registration flow
4. **Monitor performance**: Track model accuracy and retrain as needed

## Resources

- MindsDB Documentation: https://docs.mindsdb.com
- SQL API Reference: https://docs.mindsdb.com/sql/api
- Predictors Guide: https://docs.mindsdb.com/sql/create/predictor

## Troubleshooting

### Connection Issues
- Use `host.docker.internal` (not `localhost`) when MindsDB is in Docker
- Verify MySQL is accessible: `mysql -h 127.0.0.1 -P 3306 -u emailapp -p`
- Check MySQL grants: `GRANT ALL ON email_reg_db.* TO 'emailapp'@'%'`

### Model Training Fails
- Ensure sufficient data (at least 50-100 rows recommended)
- Check data quality (no NULL values in training columns)
- Monitor status: `SELECT status, error FROM mindsdb.models WHERE name='model_name'`

### Predictions Return NULL
- Model may still be training (check status)
- Input data format must match training data
- Try simpler queries first to verify model works
