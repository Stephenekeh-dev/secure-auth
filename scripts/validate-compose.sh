#!/bin/bash
# Validates docker-compose.yml configuration

echo "====================================="
echo "  Validating docker-compose.yml..."
echo "====================================="

# Check file exists
if [ ! -f "docker-compose.yml" ]; then
    echo "docker-compose.yml not found"
    exit 1
fi
echo "docker-compose.yml found"

# Check required services exist
check_service() {
    if grep -q "$1:" docker-compose.yml; then
        echo "Service '$1' found"
    else
        echo " Service '$1' missing"
        exit 1
    fi
}

check_service "app"
check_service "postgres"
check_service "redis"
check_service "nginx"

# Check required environment variables are referenced
check_env() {
    if grep -q "$1" docker-compose.yml; then
        echo " Environment variable '$1' referenced"
    else
        echo "Environment variable '$1' missing"
        exit 1
    fi
}

check_env "DB_USERNAME"
check_env "DB_PASSWORD"
check_env "JWT_SECRET"
check_env "MAIL_USERNAME"
check_env "AWS_S3_BUCKET_NAME"

# Check healthchecks exist
if grep -q "healthcheck" docker-compose.yml; then
    echo " Healthchecks configured"
else
    echo " No healthchecks found"
    exit 1
fi

# Validate compose syntax
echo ""
echo "Validating compose syntax..."
if docker compose config > /dev/null 2>&1; then
    echo " docker-compose.yml syntax is valid"
else
    echo "docker-compose.yml has syntax errors"
    docker compose config
    exit 1
fi

echo ""
echo "====================================="
echo "  docker-compose.yml validation PASSED "
echo "====================================="