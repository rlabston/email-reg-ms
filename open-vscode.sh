#!/bin/bash
# Open VS Code with memory limits to prevent crashes

# Increase max memory and disable GPU acceleration which can cause issues
code --max-memory=8192 --disable-gpu ~/dev/mobile/spring/email-reg-ms
