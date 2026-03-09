#!/bin/bash
# Verification script for QA test automation setup

echo "=========================================="
echo "ServiceHub QA Setup Verification"
echo "=========================================="
echo ""

# Check Java version
echo "1. Checking Java version..."
java -version 2>&1 | head -n 1
if [ $? -eq 0 ]; then
    echo "   ✓ Java is installed"
else
    echo "   ✗ Java is not installed or not in PATH"
    exit 1
fi
echo ""

# Check Maven version
echo "2. Checking Maven version..."
mvn -version | head -n 1
if [ $? -eq 0 ]; then
    echo "   ✓ Maven is installed"
else
    echo "   ✗ Maven is not installed or not in PATH"
    exit 1
fi
echo ""

# Build API tests
echo "3. Building API tests..."
cd api-tests
mvn clean compile test-compile -q
if [ $? -eq 0 ]; then
    echo "   ✓ API tests build successful"
else
    echo "   ✗ API tests build failed"
    exit 1
fi
cd ..
echo ""

# Build UI tests
echo "4. Building UI tests..."
cd ui-tests
mvn clean compile test-compile -q
if [ $? -eq 0 ]; then
    echo "   ✓ UI tests build successful"
else
    echo "   ✗ UI tests build failed"
    exit 1
fi
cd ..
echo ""

echo "=========================================="
echo "✓ All verification checks passed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "  - Start ServiceHub backend: cd ../backend && mvn spring-boot:run"
echo "  - Run API tests: cd api-tests && mvn test"
echo "  - Run UI tests: cd ui-tests && mvn test"
