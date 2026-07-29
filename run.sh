#!/usr/bin/env bash
echo "🔨 Compiling PlacePro Standalone Server..."
javac Server.java
if [ $? -eq 0 ]; then
  echo "🚀 Starting PlacePro Enterprise Server on http://localhost:8080..."
  java Server
else
  echo "❌ Compilation failed!"
  exit 1
fi
