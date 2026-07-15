#!/usr/bin/env python3
"""
Embedding Service for AI Engine
Generates vector embeddings for employee roles and project descriptions using sentence-transformers.
Provides REST API for Java Spring Boot to call.
"""

import os
import logging
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import numpy as np

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Load model on startup (use a small, fast model)
MODEL_NAME = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2")
model = None

def load_model():
    global model
    logger.info(f"Loading embedding model: {MODEL_NAME}")
    model = SentenceTransformer(MODEL_NAME)
    logger.info("Model loaded successfully")

load_model()

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy", "model": MODEL_NAME})

@app.route("/embed", methods=["POST"])
def embed():
    """
    Generate embeddings for a list of texts.
    Input: {"texts": ["text1", "text2", ...]}
    Output: {"embeddings": [[...], [...], ...]}
    """
    try:
        data = request.get_json()
        if not data or "texts" not in data:
            return jsonify({"error": "Missing 'texts' field"}), 400

        texts = data["texts"]
        if not isinstance(texts, list) or not texts:
            return jsonify({"error": "'texts' must be a non-empty list"}), 400

        # Generate embeddings
        embeddings = model.encode(texts, normalize_embeddings=True)

        # Convert to list for JSON serialization
        embeddings_list = embeddings.tolist()

        return jsonify({"embeddings": embeddings_list})

    except Exception as e:
        logger.error(f"Embedding error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route("/embed/single", methods=["POST"])
def embed_single():
    """
    Generate embedding for a single text.
    Input: {"text": "some text"}
    Output: {"embedding": [...]}
    """
    try:
        data = request.get_json()
        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400

        text = data["text"]
        if not isinstance(text, str) or not text.strip():
            return jsonify({"error": "'text' must be a non-empty string"}), 400

        embedding = model.encode([text], normalize_embeddings=True)[0]
        return jsonify({"embedding": embedding.tolist()})

    except Exception as e:
        logger.error(f"Single embedding error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port)