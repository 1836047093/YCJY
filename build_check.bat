@echo off
gradlew.bat :app:compileDebugKotlin > build_errors.txt 2>&1
type build_errors.txt | findstr /C:"MainActivity.kt:" > main_errors.txt
type main_errors.txt
