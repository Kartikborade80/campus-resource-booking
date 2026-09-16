@echo off
if not exist "bin\Main.class" (
    echo Compiling application first...
    call build.bat
)
java -cp "bin;lib\mysql-connector-j-8.3.0.jar" Main
