@echo off
echo Checking for Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Java is installed! You are ready to run the app.
    echo.
) else (
    echo.
    echo [ERROR] Java is NOT installed.
    echo.
    echo Please download and install Java from: https://www.java.com/download/
    echo.
)
pause
