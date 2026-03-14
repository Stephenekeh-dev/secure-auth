#!/bin/bash
# Validates GitHub Actions workflow

echo "====================================="
echo "  Validating GitHub Actions workflow..."
echo "====================================="

WORKFLOW=".github/workflows/deploy.yml"

# Check file exists
if [ ! -f "$WORKFLOW" ]; then
    echo "$WORKFLOW not found"
    exit 1
fi
echo "$WORKFLOW found"

# Check required sections
check_section() {
    if grep -q "$1" "$WORKFLOW"; then
        echo " Section '$1' found"
    else
        echo "Section '$1' missing"
        exit 1
    fi
}

check_section "on:"
check_section "push:"
check_section "branches:"
check_section "main"
check_section "jobs:"
check_section "runs-on: ubuntu-latest"
check_section "aws-actions/configure-aws-credentials"
check_section "amazon-ecr-login"
check_section "docker build"
check_section "docker push"
check_section "appleboy/ssh-action"
check_section "docker compose down"
check_section "docker compose up"

# Check required secrets are referenced
check_secret() {
    if grep -q "secrets.$1" "$WORKFLOW"; then
        echo "Secret '$1' referenced"
    else
        echo " Secret '$1' not referenced"
        exit 1
    fi
}

check_secret "AWS_ACCESS_KEY_ID"
check_secret "AWS_SECRET_ACCESS_KEY"
check_secret "EC2_HOST"
check_secret "EC2_SSH_KEY"

# Validate YAML syntax using Python (available on most systems)
echo ""
echo "Validating YAML syntax..."
if python3 -c "import yaml; yaml.safe_load(open('$WORKFLOW'))" 2>&1; then
    echo " YAML syntax is valid"
else
    echo "YAML syntax errors found"
    exit 1
fi

echo ""
echo "====================================="
echo "  GitHub Actions validation PASSED "
echo "====================================="