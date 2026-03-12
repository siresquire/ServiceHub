@echo off
REM Verification script for QA test automation setup (Windows)

echo ==========================================
echo ServiceHub QA Setup Verification
echo ==========================================
echo.

REM Check Java version
echo 1. Checking Java version...
java -version 2>&1 | findstr /C:"version"
if %ERRORLEVEL% EQU 0 (
    echo    [OK] Java is installed
) else (
    echo    [ERROR] Java is not installed or not in PATH
    exit /b 1
)
echo.

REM Check Maven version
echo 2. Checking Maven version...
mvn -version 2>&1 | findstr /C:"Apache Maven"
if %ERRORLEVEL% EQU 0 (
    echo    [OK] Maven is installed
) else (
    echo    [ERROR] Maven is not installed or not in PATH
    exit /b 1
)
echo.

REM Build API tests
echo 3. Building API tests...
cd api-tests
call mvn clean compile test-compile -q
if %ERRORLEVEL% EQU 0 (
    echo    [OK] API tests build successful
) else (
    echo    [ERROR] API tests build failed
    cd ..
    exit /b 1
)
cd ..
echo.

REM Build UI tests
echo 4. Building UI tests...
cd ui-tests
call mvn clean compile test-compile -q
if %ERRORLEVEL% EQU 0 (
    echo    [OK] UI tests build successful
) else (
    echo    [ERROR] UI tests build failed
    cd ..
    exit /b 1
)
cd ..
echo.

echo ==========================================
echo [OK] All verification checks passed!
echo ==========================================
echo.
echo Next steps:
echo   - Start ServiceHub backend: cd ..\backend ^&^& mvn spring-boot:run
echo   - Run API tests: cd api-tests ^&^& mvn test
echo   - Run UI tests: cd ui-tests ^&^& mvn test
