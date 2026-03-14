#!/bin/bash
# Validates Dockerfile builds successfully

echo "====================================="
echo "  Validating Dockerfile..."
echo "====================================="

# Check Dockerfile exists
if [ ! -f "Dockerfile" ]; then
    echo "❌ Dockerfile not found in project root"
    exit 1
fi
echo "✅ Dockerfile found"

# Check required instructions exist
check_instruction() {
    if grep -q "$1" Dockerfile; then
        echo "✅ $1 instruction found"
    else
        echo "❌ $1 instruction missing"
        exit 1
    fi
}

check_instruction "FROM maven"
check_instruction "FROM eclipse-temurin"
check_instruction "WORKDIR"
check_instruction "COPY"
check_instruction "RUN mvn"
check_instruction "EXPOSE 8080"
check_instruction "ENTRYPOINT"
check_instruction "spring.profiles.active=prod"

# Try to build the image
echo ""
echo "Building Docker image..."
if docker build -t secure-auth-test . ; then
    echo " Docker image built successfully"
    # Clean up test image
    docker rmi secure-auth-test
else
    echo "Docker build failed"
    exit 1
fi

echo ""
echo "====================================="
echo "  Dockerfile validation PASSED "
echo "====================================="