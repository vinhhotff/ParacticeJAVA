#!/bin/bash

# RAMS AI Features Test Script
# Runs API test requests using curl and outputs formatted JSON results.

echo "===================================================="
echo "TEST 1: AI Resource Matcher (Semantic Search)"
echo "Query: Role='Java', minAvailable=30%"
echo "===================================================="
curl -s "http://localhost:8080/api/ai/recommend-resources?role=Java&minAvailable=30" | json_pp 2>/dev/null || curl -s "http://localhost:8080/api/ai/recommend-resources?role=Java&minAvailable=30"
echo -e "\n\n"

echo "===================================================="
echo "TEST 2: AI Team Risk Analysis (Gemini API)"
echo "Prompt: 'Check for overloaded team members'"
echo "===================================================="
curl -s "http://localhost:8080/api/ai/detect-risks?customPrompt=Check%20for%20overloaded%20team%20members" | json_pp 2>/dev/null || curl -s "http://localhost:8080/api/ai/detect-risks?customPrompt=Check%20for%20overloaded%20team%20members"
echo -e "\n"
