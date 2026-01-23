#!/bin/bash
# Comprehensive optimization comparison script
# Tests all scenarios with all three optimization levels

SCENARIOS=(
    "simple:100"
    "ikea:150"
    "steak:200"
    "recre:150"
    "inception:200"
    "pomme:200"
    "coffee_shop:500"
    "solar_system:250"
    "test_relevance:100"
)

RESULTS_FILE="OPTIMIZATION_COMPARISON_RESULTS.md"

echo "# Comprehensive Optimization Algorithm Comparison" > "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "**Test Date:** $(date '+%Y-%m-%d %H:%M:%S')" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "## Algorithm Implementations" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "- **Level 0: Greedy** - Fast greedy heuristic" >> "$RESULTS_FILE"
echo "- **Level 1: Beam Search** - Explores multiple paths (beam width=16)" >> "$RESULTS_FILE"
echo "- **Level 2: Branch & Bound A*** - Exhaustive search with pruning (5s timeout)" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "---" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

for scenario_config in "${SCENARIOS[@]}"; do
    IFS=':' read -r scenario max_delay <<< "$scenario_config"
    
    echo "" >> "$RESULTS_FILE"
    echo "## Scenario: $scenario (maxDelay=$max_delay)" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo '```' >> "$RESULTS_FILE"
    
    for level in 0 1 2; do
        echo "======================================" >> "$RESULTS_FILE"
        echo "Level $level:" >> "$RESULTS_FILE"
        echo "--------------------------------------" >> "$RESULTS_FILE"
        
        timeout 30 java -jar target/krpsim-1.0.jar "krpsim/$scenario" "$max_delay" --optimize-level "$level" 2>&1 | \
            grep -E "Nice file|Evaluating|no more|Reached|Stock :|^[a-z_]+=>|^time=>" >> "$RESULTS_FILE"
        
        echo "" >> "$RESULTS_FILE"
    done
    
    echo '```' >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
done

echo "" >> "$RESULTS_FILE"
echo "---" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "## Test completed" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "Results saved to: $RESULTS_FILE" >> "$RESULTS_FILE"

echo "Full comparison test completed! Results in: $RESULTS_FILE"
