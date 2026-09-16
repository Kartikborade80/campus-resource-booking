@echo off
if not exist "bin" mkdir bin
powershell -NoProfile -Command "Get-ChildItem -Path 'src' -Filter '*.java' -Recurse | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' } | Out-File -FilePath 'sources.txt' -Encoding ascii"
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar;bin" -d bin @sources.txt
if %ERRORLEVEL% equ 0 (
    echo Compilation Successful! Classes generated in bin/
) else (
    echo Compilation Failed!
)
if exist sources.txt del sources.txt
