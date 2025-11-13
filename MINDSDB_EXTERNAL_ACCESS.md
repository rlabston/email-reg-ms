# Exposing MindsDB to External Users

## Current Setup
MindsDB is running in Docker Desktop and binding to:
- **HTTP API**: localhost:47334
- **MySQL Protocol**: localhost:47335
- **MongoDB Protocol**: localhost:47336

These are only accessible from your local machine by default.

## Option 1: Docker Port Publishing (Simplest)

### Step 1: Find your MindsDB container
```bash
docker ps --filter "ancestor=mindsdb/mindsdb" --format "{{.ID}}\t{{.Names}}"
```

### Step 2: Stop the current container
```bash
docker stop <container_id>
docker rm <container_id>
```

### Step 3: Restart with external port binding
```bash
docker run -d \
  --name mindsdb \
  -p 0.0.0.0:47334:47334 \
  -p 0.0.0.0:47335:47335 \
  -p 0.0.0.0:47336:47336 \
  -v mindsdb_data:/root/mindsdb \
  mindsdb/mindsdb
```

**Note**: Using `0.0.0.0` binds to all network interfaces. External users can now access:
- HTTP API: `http://YOUR_IP:47334`
- MySQL: `mysql -h YOUR_IP -P 47335 -u mindsdb`

### Security Considerations
- ⚠️ **No authentication by default** - anyone can access
- ⚠️ Open to internet if your firewall allows
- ⚠️ Use firewall rules to restrict access

---

## Option 2: Nginx Reverse Proxy (Recommended for Production)

### Step 1: Install Nginx
```bash
sudo apt update
sudo apt install nginx -y
```

### Step 2: Create Nginx configuration
```bash
sudo nano /etc/nginx/sites-available/mindsdb
```

Add this configuration:
```nginx
# HTTP API Proxy with Authentication
server {
    listen 80;
    server_name mindsdb.yourdomain.com;

    # Basic authentication
    auth_basic "MindsDB Access";
    auth_basic_user_file /etc/nginx/.htpasswd;

    location / {
        proxy_pass http://localhost:47334;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, OPTIONS";
        add_header Access-Control-Allow-Headers "Authorization, Content-Type";
    }
}

# MySQL Protocol Proxy
stream {
    server {
        listen 3306;
        proxy_pass localhost:47335;
    }
}
```

### Step 3: Create password file
```bash
sudo apt install apache2-utils -y
sudo htpasswd -c /etc/nginx/.htpasswd admin
# Enter password when prompted
```

### Step 4: Enable and test
```bash
sudo ln -s /etc/nginx/sites-available/mindsdb /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Step 5: Access externally
```bash
# HTTP API with authentication
curl -u admin:password http://YOUR_IP/api/sql/query

# From your Spring Boot app
curl -u admin:password \
  -X POST http://YOUR_IP/api/sql/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM mindsdb.models"}'
```

---

## Option 3: SSH Tunnel (Secure Development Access)

Allow remote developers to access MindsDB securely without exposing it publicly.

### On remote user's machine:
```bash
# Forward local port 47334 to your MindsDB instance
ssh -L 47334:localhost:47334 \
    -L 47335:localhost:47335 \
    user@your-server-ip

# Now they can access MindsDB as if it were local
# http://localhost:47334
```

### Advantages:
- ✅ Encrypted connection
- ✅ Uses existing SSH authentication
- ✅ No public exposure
- ✅ Perfect for development teams

---

## Option 4: VPN Access (Enterprise)

Set up a VPN (WireGuard, OpenVPN) so external users join your private network.

### Quick WireGuard Setup:
```bash
# Install WireGuard
sudo apt install wireguard -y

# Generate keys
wg genkey | tee privatekey | wg pubkey > publickey

# Configure /etc/wireguard/wg0.conf
[Interface]
PrivateKey = <your_private_key>
Address = 10.0.0.1/24
ListenPort = 51820

[Peer]
PublicKey = <client_public_key>
AllowedIPs = 10.0.0.2/32
```

Users connect via VPN and access MindsDB at `10.0.0.1:47334`

---

## Option 5: API Gateway (Spring Boot Proxy)

Keep MindsDB internal, expose it through your Spring Boot application.

### Create MindsDB Proxy Controller:
```java
@RestController
@RequestMapping("/api/mindsdb")
public class MindsDBProxyController {
    
    @Value("${mindsdb.url:http://localhost:47334}")
    private String mindsdbUrl;
    
    private final RestTemplate restTemplate;
    
    @PostMapping("/query")
    public ResponseEntity<?> executeQuery(
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        // Add authentication/authorization checks
        if (!isAuthorized(authentication)) {
            return ResponseEntity.status(403).build();
        }
        
        // Proxy request to MindsDB
        String query = request.get("query");
        
        // Sanitize/validate query
        if (!isSafeQuery(query)) {
            return ResponseEntity.badRequest()
                .body("Invalid query");
        }
        
        // Forward to MindsDB
        Map<String, Object> mindsdbRequest = Map.of("query", query);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            mindsdbUrl + "/api/sql/query",
            mindsdbRequest,
            Map.class
        );
        
        return ResponseEntity.ok(response.getBody());
    }
    
    private boolean isSafeQuery(String query) {
        // Whitelist allowed operations
        String upperQuery = query.toUpperCase().trim();
        return upperQuery.startsWith("SELECT") || 
               upperQuery.startsWith("DESCRIBE") ||
               upperQuery.startsWith("SHOW");
    }
}
```

### Security Configuration:
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/mindsdb/**").authenticated()
                .requestMatchers("/api/emails/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
}
```

### Usage:
```bash
# External users call your Spring Boot API
curl -u user:pass \
  -X POST http://your-server:8080/api/mindsdb/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM mindsdb.models"}'
```

**Advantages**:
- ✅ Full control over access
- ✅ Query validation/sanitization
- ✅ Existing authentication
- ✅ MindsDB stays private
- ✅ Audit logging

---

## Option 6: Cloud Deployment (MindsDB Cloud)

Use MindsDB's managed cloud service instead of self-hosting.

1. Sign up at https://cloud.mindsdb.com
2. Get API credentials
3. Update your Spring Boot config:

```properties
mindsdb.cloud.url=https://cloud.mindsdb.com
mindsdb.cloud.api-key=your_api_key
```

**Advantages**:
- ✅ No infrastructure management
- ✅ Built-in security
- ✅ Automatic scaling
- ✅ SSL/TLS by default

---

## Recommendation by Use Case

| Use Case | Recommended Option | Why |
|----------|-------------------|-----|
| **Development Team** | SSH Tunnel (Option 3) | Secure, simple, no config changes |
| **Internal Company** | VPN (Option 4) | Centralized access control |
| **Public API** | Spring Proxy (Option 5) | Full control, security, validation |
| **Production SaaS** | MindsDB Cloud (Option 6) | Managed, scalable, secure |
| **Quick Demo** | Nginx Proxy (Option 2) | Easy setup, basic auth |

---

## Security Checklist

Before exposing MindsDB externally:

- [ ] **Authentication**: Add password protection or API keys
- [ ] **Firewall**: Use UFW/iptables to restrict IP ranges
  ```bash
  sudo ufw allow from 203.0.113.0/24 to any port 47334
  ```
- [ ] **SSL/TLS**: Use Let's Encrypt for HTTPS
  ```bash
  sudo certbot --nginx -d mindsdb.yourdomain.com
  ```
- [ ] **Rate Limiting**: Prevent abuse
  ```nginx
  limit_req_zone $binary_remote_addr zone=mindsdb:10m rate=10r/s;
  limit_req zone=mindsdb burst=20;
  ```
- [ ] **Query Validation**: Sanitize SQL inputs
- [ ] **Audit Logging**: Track who queries what
- [ ] **Network Segmentation**: Keep database separate from MindsDB
- [ ] **Backup**: Secure MindsDB data volume

---

## Quick Start: Nginx with SSL (Recommended)

```bash
# 1. Install Nginx and Certbot
sudo apt update
sudo apt install nginx certbot python3-certbot-nginx -y

# 2. Create Nginx config
sudo tee /etc/nginx/sites-available/mindsdb << 'EOF'
server {
    listen 80;
    server_name mindsdb.yourdomain.com;

    location / {
        proxy_pass http://localhost:47334;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# 3. Enable site
sudo ln -s /etc/nginx/sites-available/mindsdb /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# 4. Get SSL certificate
sudo certbot --nginx -d mindsdb.yourdomain.com

# 5. Test external access
curl https://mindsdb.yourdomain.com/api/sql/query
```

Now external users can access MindsDB securely at `https://mindsdb.yourdomain.com`!
