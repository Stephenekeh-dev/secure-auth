#!/bin/bash
# Runs all validation scripts

echo ""
echo "########################################"
echo "  RUNNING ALL VALIDATION SCRIPTS"
echo "########################################"
echo ""

SCRIPTS=(
    "scripts/validate-env.sh"
    "scripts/validate-dockerfile.sh"
    "scripts/validate-compose.sh"
    "scripts/validate-nginx.sh"
    "scripts/validate-github-actions.sh"
)

PASSED=0
FAILED=0

for script in "${SCRIPTS[@]}"; do
    echo ""
    if bash "$script"; then
        PASSED=$((PASSED + 1))
    else
        FAILED=$((FAILED + 1))
        echo "$script FAILED"
    fi
done

echo ""
echo "########################################"
echo "  RESULTS: $PASSED passed, $FAILED failed"
echo "########################################"

if [ $FAILED -gt 0 ]; then
    exit 1
fi

exit 0