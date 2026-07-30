#!/usr/bin/env bash
echo "⚡ Starting PlacePro Campus Placement Portal Server..."

# Free port 8080 if currently occupied by a previous process
lsof -ti:8080 | xargs kill -9 2>/dev/null || true

if command -v node > /dev/null 2>&1; then
    echo "🚀 Launching Node Web Engine on http://localhost:8080..."
    node server.js
else
    echo "🔨 Compiling Standalone Server..."
    javac Server.java && java Server
fi
