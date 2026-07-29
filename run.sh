#!/usr/bin/env bash
echo "⚡ Starting PlacePro Campus Placement Portal Server..."

if command -v node &> /dev/null
then
    echo "🚀 Launching Node Web Engine on http://localhost:8080..."
    node server.js
else
    echo "🔨 Compiling Standalone Server..."
    javac Server.java && java Server
fi
