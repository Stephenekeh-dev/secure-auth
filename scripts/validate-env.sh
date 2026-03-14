#!/bin/bash
# Validates .env file has all required variables

echo "====================================="
echo "  Validating .env file..."
echo "====================================="

if [ ! -f ".env" ]; then
    echo ".env file not found"
    exit 1
fi
echo ".env file found"

check_var() {
    if grep -q "^$1=" .env; then
        VALUE=$(grep "^$1=" .env | cut -d'=' -f2)
        if [ -z "$VALUE" ]; then
            echo "$1 is empty"
        else
            echo "$1 is set"
        fi
    else
        echo "$1 is missing"
        exit 1
    fi
}

check_var "DB_USERNAME"
check_var "DB_PASSWORD"
check_var "JWT_SECRET"
check_var "JWT_EXPIRATION_MS"
check_var "MAIL_USERNAME"
check_var "MAIL_PASSWORD"
check_var "APP_BASE_URL"
check_var "AWS_S3_BUCKET_NAME"
check_var "AWS_S3_REGION"
check_var "AWS_S3_ACCESS_KEY"
check_var "AWS_S3_SECRET_KEY"

echo ""
echo "====================================="
echo "  .env validation PASSED "
echo "====================================="