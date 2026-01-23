# Optimization Algorithms Comparison - Multiple Scenarios

## Algorithm Implementations

- **Level 0: Greedy** - Fast, greedy heuristic (selects best immediate action)
- **Level 1: Beam Search** - Explores multiple paths (beam width=16)
- **Level 2: Branch & Bound A*** - Exhaustive search with pruning (5s timeout)

---

## Test Results

### 1. Solar System (250 ticks) - Infinite renewable energy scenario

| Level | Algorithm | Final Time | Energy | Panels | Research | Status |
|-------|-----------|------------|--------|--------|----------|--------|
| **0** | Greedy | 250 | **2,832** | **463** | **27** | ✓ Reached delay |
| **1** | Beam Search | 250 | 520 | 10 | 0 | ✓ Reached delay |
| **2** | Branch & Bound A* | 250 | **2,832** | **463** | **27** | ✓ Reached delay |

**Winner: Level 0 & 2 (tie)** - Both achieved balanced results (energy + infrastructure + research)

---

### 2. Coffee Shop (500 ticks) - Customer satisfaction optimization

| Level | Algorithm | Final Time | Happy Customers | Money | Status |
|-------|-----------|------------|-----------------|-------|--------|
| **0** | Greedy | 281 | 660 | €20 | No more processes |
| **1** | Beam Search | 241 | **799** | €28 | No more processes |
| **2** | Branch & Bound A* | 281 | 660 | €20 | No more processes |

**Winner: Level 1 (Beam Search)** - 21% more happy customers (799 vs 660)

---

### 3. IKEA (150 ticks) - Furniture assembly optimization

| Level | Algorithm | Final Time | Armoire | Etagere | Components | Status |
|-------|-----------|------------|---------|---------|------------|--------|
| **0** | Greedy | 30 | 0 | 1 | 2 fond, 2 montant | No more processes |
| **1** | Beam Search | 10 | 0 | 7 | None | No more processes |
| **2** | Branch & Bound A* | 50 | **1** ✓ | 0 | None | No more processes |

**Winner: Level 2 (Branch & Bound A*)** - Only algorithm that assembled complete furniture!

---

### 4. Pomme (200 ticks) - Production optimization

| Level | Algorithm | Euro | Pomme | Pâte | Status |
|-------|-----------|------|-------|------|--------|
| **0** | Greedy | €8,800 | 1,400 | 400 | Reached delay |
| **1** | Beam Search | €0 | **70,000** | 0 | No more processes |
| **2** | Branch & Bound A* | €8,800 | 1,400 | 400 | Reached delay |

**Winner: Level 1 (Beam Search)** - Maximized pomme production (50x more!)

---

## Overall Analysis

### Algorithm Strengths

#### Greedy (Level 0)
- ✅ Fast and reliable
- ✅ Balanced multi-objective optimization
- ✅ Good for complex scenarios with many trade-offs
- ❌ May miss optimal long-term strategies

#### Beam Search (Level 1)
- ✅ Excellent for single-objective maximization
- ✅ Explores multiple strategies in parallel
- ✅ Best for "maximize X" scenarios (customers, resources)
- ❌ Can ignore important secondary goals
- ❌ May get stuck in local optima

#### Branch & Bound A* (Level 2)
- ✅ Best solution quality when time allows
- ✅ Finds complex multi-step plans (IKEA armoire!)
- ✅ Exhaustive search with smart pruning
- ❌ Limited by 5s timeout on complex scenarios
- ❌ May fall back to greedy if timeout exceeded

### Scenario-Specific Recommendations

- **Multi-objective optimization** (solar_system) → Use **Greedy** or **Branch & Bound**
- **Single resource maximization** (coffee_shop, pomme) → Use **Beam Search**
- **Complex assembly/recipes** (ikea) → Use **Branch & Bound A***
- **Time-critical/large search spaces** → Use **Greedy**

### Conclusion

No single algorithm dominates all scenarios. **Level 2 (Branch & Bound A*)** found the best solutions in complex scenarios like IKEA, while **Level 1 (Beam Search)** excelled at resource maximization. **Level 0 (Greedy)** provides reliable baseline performance across all scenarios.

---

*Generated: January 23, 2026*
*Command: `java -cp target/classes krpsim.Krpsim krpsim/solar_system 250 --optimize-level [0|1|2]`*
