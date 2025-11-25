-- MindsDB Chatbot Model Setup
-- This script creates an AI chatbot model in MindsDB for the email registration system
-- Run these commands in MindsDB console or via API

-- 1. Create a connection to OpenAI (or other LLM provider)
CREATE ML_ENGINE openai_engine
FROM openai
USING
  api_key = 'your-openai-api-key-here';  -- Replace with your actual API key

-- 2. Create the chatbot model
CREATE MODEL chatbot_model
PREDICT response
USING
  engine = 'openai_engine',
  model_name = 'gpt-3.5-turbo',
  prompt_template = 'Context: {{context}}\n\nUser Question: {{question}}\n\nProvide a helpful response as an AI assistant for an email registration system. Help with registration, authentication, password reset, and general support questions.\n\nResponse:';

-- 3. Test the model
SELECT response 
FROM chatbot_model 
WHERE question='How do I register?' 
AND context='';

-- Alternative: Use Hugging Face models (free, no API key needed)
-- CREATE MODEL chatbot_model
-- PREDICT response
-- USING
--   engine = 'huggingface',
--   model_name = 'microsoft/DialoGPT-medium',
--   input_column = 'question',
--   api_key = 'optional-huggingface-token';

-- Alternative: Use MindsDB's built-in LLM
-- CREATE MODEL chatbot_model
-- PREDICT response
-- USING
--   engine = 'mindsdb',
--   prompt_template = 'You are a helpful assistant for an email registration system.\n\nContext: {{context}}\nQuestion: {{question}}\nAnswer:';

-- 4. View model status
SELECT * FROM models WHERE name = 'chatbot_model';

-- 5. Test queries
SELECT response FROM chatbot_model WHERE question='What is password policy?' AND context='';
SELECT response FROM chatbot_model WHERE question='How do I reset my password?' AND context='user: I forgot my password\nassistant: I can help with that!';

-- 6. Update model if needed
-- RETRAIN chatbot_model;

-- 7. Drop model if you want to recreate
-- DROP MODEL chatbot_model;
