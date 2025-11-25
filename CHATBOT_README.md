# AI Chatbot Feature

## Overview

This chatbot feature provides an AI-powered assistant for your email registration system. It supports conversation history, context awareness, and can integrate with OpenAI's GPT models or use rule-based responses.

## Features

- ✅ **Conversational AI**: Natural language understanding and responses
- ✅ **Conversation History**: Maintains context across multiple messages
- ✅ **OpenAI Integration**: Optional GPT-3.5/GPT-4 integration
- ✅ **Mock Responses**: Built-in rule-based responses for testing without API keys
- ✅ **User Authentication**: Integrated with existing JWT authentication
- ✅ **Persistent Storage**: Conversations and messages stored in database
- ✅ **RESTful API**: Easy integration with any frontend

## API Endpoints

### 1. Send Message
```http
POST /api/chat/message
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "message": "How do I register?",
  "conversationId": "optional-uuid"
}
```

**Response:**
```json
{
  "message": "To register, simply provide your email address...",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1700000000000,
  "conversationHistory": [...]
}
```

### 2. Get Conversation History
```http
GET /api/chat/history/{conversationId}
Authorization: Bearer <jwt-token>
```

### 3. Delete Conversation
```http
DELETE /api/chat/conversation/{conversationId}
Authorization: Bearer <jwt-token>
```

### 4. Health Check
```http
GET /api/chat/health
```

## Configuration

### Basic Setup (Mock Responses)

No configuration needed! The chatbot works out of the box with rule-based responses.

### OpenAI Integration (Optional)

1. Get an API key from [OpenAI Platform](https://platform.openai.com/api-keys)

2. Set environment variable:
```bash
export OPENAI_API_KEY=sk-...your-key-here
```

Or update `application.properties`:
```properties
chatbot.api.key=sk-...your-key-here
chatbot.model=gpt-3.5-turbo
chatbot.max.history=10
chatbot.system.message=You are a helpful AI assistant...
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `chatbot.api.key` | (empty) | OpenAI API key |
| `chatbot.api.url` | OpenAI API | API endpoint URL |
| `chatbot.model` | gpt-3.5-turbo | AI model to use |
| `chatbot.max.history` | 10 | Max messages in context |
| `chatbot.system.message` | Default prompt | System instruction |

## Testing

### Web Interface

1. Start the application:
```bash
./gradlew bootRun
```

2. Open the chatbot demo:
```
http://localhost:8081/chatbot.html
```

### API Testing with cURL

```bash
# Send a message (anonymous)
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello!"}'

# Send a message (authenticated)
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "How do I reset my password?",
    "conversationId": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Get conversation history
curl http://localhost:8080/api/chat/history/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Database Schema

### chat_conversations
- `id`: Primary key
- `conversation_id`: Unique conversation UUID
- `user_email`: Associated user email
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp
- `message_count`: Total messages in conversation

### chat_messages
- `id`: Primary key
- `conversation_id`: Foreign key to conversations
- `content`: Message text
- `role`: "user" or "assistant"
- `timestamp`: Message timestamp
- `token_count`: Optional token usage tracking

## Frontend Integration

### Angular Example

```typescript
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ChatService {
  constructor(private http: HttpClient) {}

  sendMessage(message: string, conversationId?: string) {
    return this.http.post('/api/chat/message', {
      message,
      conversationId
    });
  }
}
```

### React Example

```javascript
async function sendMessage(message, conversationId) {
  const response = await fetch('/api/chat/message', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ message, conversationId })
  });
  return response.json();
}
```

### Android (Retrofit)

```kotlin
interface ChatApi {
    @POST("/api/chat/message")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse
}
```

## Mock Response Behavior

Without an OpenAI API key, the chatbot uses rule-based responses:

| User Input | Response Type |
|------------|---------------|
| "help", "?" | Show available features |
| "register", "sign up" | Registration instructions |
| "password", "forgot" | Password reset guide |
| "hello", "hi" | Greeting |
| "thank" | Acknowledgment |
| Other | Clarification request |

## Security

- ✅ Requires authentication for all chatbot endpoints (except health check)
- ✅ API keys stored in environment variables
- ✅ Conversation isolation per user
- ✅ Input validation on all requests
- ✅ CORS protection enabled

## Performance Considerations

- Conversation history limited to recent messages (default: 10)
- Database indexes on `conversation_id` and `timestamp`
- Async processing recommended for production
- Consider rate limiting for API calls

## Troubleshooting

### Chatbot not responding
- Check if API key is set (if using OpenAI)
- Verify database tables created (check migrations)
- Check authentication token is valid

### OpenAI API errors
- Verify API key is correct
- Check API quota/billing
- Review model availability

### Database errors
- Run migrations: `./gradlew bootRun`
- Check database connection
- Verify user permissions

## Future Enhancements

- [ ] Streaming responses (Server-Sent Events)
- [ ] Multi-language support
- [ ] Voice input/output
- [ ] File upload support
- [ ] Custom AI model integration
- [ ] Analytics dashboard
- [ ] A/B testing framework

## Contributing

To add new features or improve responses:

1. Update `ChatbotService.generateMockResponse()` for rule-based improvements
2. Modify `chatbot.system.message` for AI behavior changes
3. Extend DTOs for additional metadata
4. Add new endpoints in `ChatbotController`

## License

Same as parent project.
