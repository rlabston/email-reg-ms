# System Architecture

## Communication Flow

All client communication goes through the **Gateway (port 8080)** for scalability and security:

```
┌─────────────────┐
│   Web Browser   │────────────┐
│  localhost:8080 │            │
└────────┬────────┘            │
                               │
                               ▼
┌─────────────────┐       ┌──────────────────┐
│  Mobile App     │─────▶│   Gateway:8080   │
│ (10.0.2.2:8080) │       │  Single Entry    │
└─────────────────┘       │     Point        │
                          └────────┬─────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
           ┌────────────┐  ┌──────────────┐  ┌──────────┐
           │  Backend   │  │ Static Files │  │   CORS   │
           │ :8081/api  │  │ (web-frontend│  │ Handling │
           └─────┬──────┘  │   /build)    │  └──────────┘
                 │         └──────────────┘
                 ▼
        ┌────────────────┐
        │ MindsDB        │
        │ localhost:47334│
        │ or Cloud       │
        └────────────────┘
```

## Gateway Responsibilities

1. **Static File Serving**: Serves the Angular web app from `/web-frontend/build`
2. **API Proxying**: Routes `/api/*` requests to backend on `localhost:8081`
3. **CORS Management**: Handles cross-origin requests
4. **Single Entry Point**: All clients (web, mobile) use one endpoint
5. **Load Balancing**: Can scale backend instances behind the gateway

## Client Endpoints

### Web App
- Served from: `http://localhost:8080`
- API calls: Relative `/api/*` paths (proxied by gateway)
- Dev mode: `http://localhost:4200` (calls `http://127.0.0.1:8080/api/*`)

### Mobile App (Android Emulator)
- Gateway: `http://10.0.2.2:8080`
- All API calls: `http://10.0.2.2:8080/api/*`
- MindsDB chatbot: `http://10.0.2.2:8080/api/mindsdb/query`
- Email services: `http://10.0.2.2:8080/api/emails/*`

### Mobile App (Physical Device/External)
- Gateway: `http://YOUR_HOST_IP:8080/api/*`

## Benefits

✅ **Scalability**: Backend can be horizontally scaled
✅ **Security**: Backend never exposed directly to clients
✅ **Flexibility**: Can swap backend implementations without client changes
✅ **Monitoring**: Single point for logging/metrics
✅ **SSL Termination**: Gateway handles HTTPS, internal services use HTTP
✅ **Rate Limiting**: Applied at gateway level
✅ **Caching**: Gateway can cache responses

## Port Summary

- **8080**: Gateway (public entry point)
- **8081**: Backend (internal only, not exposed)
- **47334**: MindsDB local (internal only)
- **4200**: Angular dev server (development only)
- **8082**: React Native Metro (development only, bypassed in release builds)
