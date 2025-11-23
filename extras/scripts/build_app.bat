@echo off
echo Building Coffee Shop Application...
call mvn clean package
echo.
echo Build Complete!
echo Your app is located at: target/inventory-system-1.0-SNAPSHOT.jar
pause
