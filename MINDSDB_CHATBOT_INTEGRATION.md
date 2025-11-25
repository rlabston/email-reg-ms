# MindsDB Chatbot Integration

## Overview

The chatbot now uses **MindsDB as the primary AI service**, with fallback to OpenAI and mock responses. This provides a flexible, cost-effective solution that can leverage MindsDB's AI capabilities.

## How It Works

### Response Generation Flow

```
1. Try MindsDB chatbot_model
   ↓ (if fails or not available)
2. Try OpenAI API (if OPENAI_API_KEY is set)
   ↓ (if fails or not configured)
3. Use Mock Rule-Based Responses
```

## Setup Instructions

### Step 1: Start MindsDB

Choose one of these options:

#### Option A: Local Python MindsDB (Current Setup)
```bash
# Already running at http://localhost:47334
# Check status:
curl http://localhost:47334/api/status
```

#### Option B: MindsDB Cloud (Free Tier)
```bash
# Sign up at https://cloud.mindsdb.com
# Update application.properties:
mindsdb.api.url=https://cloud.mindsdb.com
mindsdb.api.key=YOUR_API_KEY
```

#### Option C: Docker MindsDB
```bash
docker run -p 47334:47334 mindsdb/mindsdb
```

### Step 2: Create the Chatbot Model

Run the setup script in MindsDB:

```bash
# If using MindsDB Python client:
python3 << EOF
import mindsdb_sdk
server = mindsdb_sdk.connect('http://localhost:47334')

# Create OpenAI engine (replace with your API key)
server.ml_engines.create(
    name='openai_engine',
    handler='openai',
    params={'api_key': 'sk-your-key-here'}
)

# Create chatbot model
server.models.create(
    name='chatbot_model',
    predict='response',
    engine='openai_engine',
    options={
        'model_name': 'gpt-3.5-turbo',
        'prompt_template': '''Context: {{context}}

User Question: {{question}}

Provide a helpful response as an AI assistant for an email registration system. 
Help with registration, authentication, password reset, and general support questions.

Response:'''
    }
)
EOF
```

Or use SQL via MindsDB Console:

```sql
-- Copy from mindsdb-chatbot-setup.sql
CREATE MODEL chatbot_model
PREDICT response
USING
  engine = 'openai_engine',
  model_name = 'gpt-3.5-turbo',
  prompt_template = 'Context: {{context}}\n\nUser Question: {{question}}\n\nResponse:';
```

### Step 3: Test the Integration

```bash
# Start the application
./gradlew bootRun

# Test via web UI
open http://localhost:8081/chatbot.html

# Test via API
curl -X POST http://localhost:8081/api/chat/message \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "How do I register?"}'
```

## Configuration

### Application Properties

```properties
# MindsDB Chatbot (Primary)
chatbot.mindsdb.model=chatbot_model
chatbot.mindsdb.enabled=true

# OpenAI (Fallback)
chatbot.api.key=${OPENAI_API_KEY:}

# Common Settings
chatbot.max.history=10
chatbot.system.message=You are a helpful AI assistant...
```

### Environment Variables

```bash
# Enable/disable MindsDB
export MINDSDB_CHATBOT_ENABLED=true

# OpenAI fallback (optional)
export OPENAI_API_KEY=sk-...

# MindsDB connection (if using cloud)
export MINDSDB_API_URL=https://cloud.mindsdb.com
export MINDSDB_API_KEY=your-cloud-key
```

## MindsDB Model Options

### Option 1: OpenAI via MindsDB (Recommended)
- **Pros**: Best quality responses, production-ready
- **Cons**: Requires OpenAI API key, costs per token
- **Setup**: See `mindsdb-chatbot-setup.sql`

### Option 2: Hugging Face Models (Free)
```sql
CREATE MODEL chatbot_model
PREDICT response
USING
  engine = 'huggingface',
  model_name = 'microsoft/DialoGPT-medium',
  input_column = 'question';
```

### Option 3: MindsDB Built-in LLM (Free)
```sql
CREATE MODEL chatbot_model
PREDICT response
USING
  engine = 'mindsdb',
  prompt_template = 'Context: {{context}}\nQuestion: {{question}}\nAnswer:';
```

### Option 4: Anthropic Claude
```sql
CREATE ML_ENGINE anthropic_engine
FROM anthropic
USING
  api_key = 'your-anthropic-key';

CREATE MODEL chatbot_model
PREDICT response
USING
  engine = 'anthropic_engine',
  model_name = 'claude-3-sonnet-20240229';
```

## Query Format

The chatbot sends queries to MindsDB in this format:

```sql
SELECT response 
FROM chatbot_model 
WHERE question='User question here' 
AND context='user: Previous message\nassistant: Previous response\n...';
```

MindsDB processes this and returns an AI-generated response based on:
- The user's current question
- Conversation history (last 10 messages by default)
- System prompt from the model configuration

## Response Flow Example

**User**: "How do I register?"

**MindsDB Query**:
```sql
SELECT response FROM chatbot_model 
WHERE question='How do I register?' 
AND context='';
```

**MindsDB Response**:
```json
{
  "data": [{
    "response": "To register, provide your email address. You'll receive a verification link to complete the process."
  }]
}
```

**Chatbot**: "To register, provide your email address..."

## Monitoring

### Check MindsDB Connection
```java
// In your application logs, look for:
"Generated response using MindsDB"  // Success
"MindsDB response generation failed, trying OpenAI: ..."  // Fallback to OpenAI
"Using mock response (no AI service available)"  // All AI failed
```

### Test MindsDB Model
```bash
# Via MindsDB API directly
curl -X POST http://localhost:47334/api/sql/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "SELECT response FROM chatbot_model WHERE question='test' AND context=''"
  }'
```

## Troubleshooting

### MindsDB Not Responding
```bash
# Check MindsDB status
curl http://localhost:47334/api/status

# Restart MindsDB (if using Python)
# Stop existing process and restart:
python -m mindsdb --api http

# Check logs in application:
grep "MindsDB" logs/application.log
```

### Model Not Found
```sql
-- List all models
SHOW MODELS;

-- Check if chatbot_model exists
SELECT * FROM models WHERE name = 'chatbot_model';

-- Recreate if missing (see mindsdb-chatbot-setup.sql)
```

### Slow Responses
- MindsDB adds ~2-5 seconds for AI processing
- Consider caching common questions
- Use smaller models (DialoGPT-small vs medium)
- Reduce context history limit

### Fallback Behavior
If MindsDB fails, the chatbot automatically:
1. Tries OpenAI (if `OPENAI_API_KEY` is set)
2. Uses mock rule-based responses (always works)

You can disable MindsDB fallback:
```properties
chatbot.mindsdb.enabled=false
```

## Advantages of MindsDB Integration

✅ **Flexibility**: Switch between different LLM providers without code changes
✅ **Cost Control**: Use free models (Hugging Face) or paid (OpenAI, Claude)
✅ **Unified Interface**: Single SQL interface for all AI operations
✅ **Context Management**: MindsDB handles conversation context automatically
✅ **Monitoring**: Query logs and model performance in MindsDB console
✅ **Fine-tuning**: Retrain models on your domain-specific data

## Next Steps

1. **Deploy MindsDB**: Move to MindsDB Cloud for production
2. **Fine-tune Model**: Train on your support ticket data
3. **Add More Models**: Create specialized models for different tasks
4. **Monitor Performance**: Track response quality and latency
5. **Scale**: MindsDB Cloud handles horizontal scaling automatically

## Resources

- [MindsDB Documentation](https://docs.mindsdb.com/)
- [MindsDB AI Tables](https://docs.mindsdb.com/sql/create/model)
- [MindsDB ML Engines](https://docs.mindsdb.com/integrations/ai-engines/openai)
- [Example Queries](mindsdb-chatbot-setup.sql)
