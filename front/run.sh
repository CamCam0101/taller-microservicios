#!/usr/bin/env bash
# Compila y arranca el front en http://localhost:8080  (requiere JDK 17+)
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/retro/*.java
java -cp out retro.App
