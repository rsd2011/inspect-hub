#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Compile and run a simple test
./gradlew :backend:auth:test --tests "SecurityConfigTest\$LoginEndpoints.shouldAllowLocalLoginWithoutAuth" 2>&1 | grep -E "(PASSED|FAILED|Status)"

echo "---"

./gradlew :backend:auth:test --tests "SecurityConfigTest\$LoginEndpoints.shouldAllowTokenRefreshWithoutAuth" 2>&1 | grep -E "(PASSED|FAILED|Status)"
