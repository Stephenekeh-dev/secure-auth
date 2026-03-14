#!/bin/bash
# Validates nginx configuration

echo "====================================="
echo "  Validating Nginx config..."
echo "====================================="

# Check file exists
if [ ! -f "nginx/nginx.conf" ]; then
    echo "❌ nginx/nginx.conf not found"
    exit 1
fi
echo "✅ nginx/nginx.conf found"

# Check required directives
check_directive() {
    if grep -q "$1" nginx/nginx.conf; then
        echo "✅ Directive '$1' found"
    else
        echo "❌ Directive '$1' missing"
        exit 1
    fi
}

check_directive "listen 80"
check_directive "proxy_pass"
check_directive "proxy_set_header"
check_directive "client_max_body_size"

# Validate using nginx Docker image
echo ""
echo "Validating nginx config syntax..."
if docker run --rm \
    -v "$(pwd)/nginx/nginx.conf:/etc/nginx/conf.d/default.conf:ro" \
    nginx:alpine nginx -t 2>&1; then
    echo " Nginx config syntax is valid"
else
    echo " Nginx config has syntax errors"
    exit 1
fi

echo ""
echo "====================================="
echo "  Nginx validation PASSED "
echo "====================================="