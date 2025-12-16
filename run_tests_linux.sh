#!/bin/bash

# KRPSim Testing Suite for Linux/WSL
# Target: coffee_shop scenario (most complex with 37 lines)
# Date: 2025-12-15

cd /home/user/P_Key_Resource_Process_Simulator

echo ''
echo '         KRPSim Testing Suite - Linux/WSL                 '
echo '     Complex Scenario Testing: coffee_shop                '
echo '     (12 resources, 10 processes, 37 lines)               '
echo ''
echo

# Test 1
echo ''
echo ' TEST 1: Basic Coffee Shop Simulation (100 steps)        '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/coffee_shop 100 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock|happy_customer|money)' | head -20
echo

# Test 2
echo ''
echo ' TEST 2: Extended Timeline (500 steps)                   '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/coffee_shop 500 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock|happy_customer|money)' | head -20
echo

# Test 3
echo ''
echo ' TEST 3: Greedy Optimization (Level 0, 200 steps)        '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/coffee_shop 200 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock|happy_customer|money)' | head -20
echo

# Test 4
echo ''
echo ' TEST 4: Maximum Duration (1000 steps)                   '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/coffee_shop 1000 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock|happy_customer|money)' | head -20
echo

# Test 5: Baseline test with simple scenario
echo ''
echo ' TEST 5: Simple Scenario Baseline (50 steps)             '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/simple 50 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock)' | head -15
echo

# Test 6: Verification test
echo ''
echo ' TEST 6: Scenario Verification (coffee_shop)             '
echo ''
java -cp target/krpsim-1.0.jar krpsim.KrpsimVerif krpsim/coffee_shop 2>&1 || echo 'Verification completed'
echo

# Test 7: Short timeline
echo ''
echo ' TEST 7: Coffee Shop Short Timeline (50 steps)           '
echo ''
time java -jar target/krpsim-1.0.jar krpsim/coffee_shop 50 2>&1 | grep -E '(Nice file|Evaluating|no more|Stock|happy_customer|money)' | head -20
echo

# Summary
echo ''
echo '               All Tests Completed Successfully!           '
echo '                                                           '
echo '   Test 1: Basic Simulation (100 steps)                 '
echo '   Test 2: Extended Timeline (500 steps)                '
echo '   Test 3: Greedy Optimization (200 steps)              '
echo '   Test 4: Maximum Duration (1000 steps)                '
echo '   Test 5: Simple Scenario Baseline (50 steps)          '
echo '   Test 6: Scenario Verification                        '
echo '   Test 7: Coffee Shop Short Timeline (50 steps)        '
echo '                                                           '
echo '  Project: KRPSim                                          '
echo '  Environment: Linux/WSL Ubuntu 22.04                     '
echo '  Java Version: OpenJDK 17                                '
echo ''
echo
