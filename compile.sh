#!/usr/bin/env bash

# compile from root
javac -cp .:lib/junit-4.12.jar -d bin $(find . -name "*.java")

# run customer controller test from root
java -cp .:bin/:lib/* org.junit.runner.JUnitCore test.CustomerControllerTest

# run customer test from root
# java -cp .:bin/:lib/* org.junit.runner.JUnitCore test.CustomerTest
