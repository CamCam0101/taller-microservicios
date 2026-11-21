@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\retro\*.java
java -cp out retro.App
