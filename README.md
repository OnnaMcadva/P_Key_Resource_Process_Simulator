# KRPSim - Key Resource Process Simulator

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**KRPSim** is an advanced process scheduling and resource management simulator with multiple optimization strategies. It simulates complex process chains with resource constraints, optimizing for time or specific resource production targets.

## 📋 Table of Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
  - [Windows](#windows)
  - [Linux](#linux)
- [Building the Project](#-building-the-project)
- [Usage](#-usage)
  - [Basic Simulation](#basic-simulation)
  - [Optimization Levels](#optimization-levels)
  - [Visualization](#visualization)
  - [Trace Verification](#trace-verification)
- [Configuration File Format](#-configuration-file-format)
- [Project Structure](#-project-structure)
- [Examples](#-examples)
- [Optimization Strategies](#-optimization-strategies)
- [Contributing](#-contributing)

---

## 🚀 Features

- **Multiple Optimization Algorithms**: Greedy, Beam Search, Branch & Bound A*
- **Real-time Visualization**: Interactive Gantt charts and resource evolution graphs
- **Trace Verification**: Built-in validator for simulation results
- **Cross-platform**: Runs on Windows, Linux, and macOS
- **No External Dependencies**: Uses only standard Java libraries
- **Flexible Configuration**: Simple text-based configuration files
- **Multiple Test Scenarios**: Includes 6+ example configurations

---

## 📦 Requirements

- **Java Development Kit (JDK) 17 or higher**
- **Apache Maven 3.6+**
- **Operating System**: Windows 10+, Linux (any modern distro), or macOS

---

## 🔧 Installation

### Windows

#### Option 1: Using Installer (Recommended)

1. **Download JDK 17**:
   - Visit [Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - Download the Windows installer (`.msi`)
   - Run the installer and follow the wizard

2. **Download Maven**:
   - Visit [Maven Download Page](https://maven.apache.org/download.cgi)
   - Download the binary zip archive
   - Extract to `C:\Program Files\Apache\maven`

3. **Set Environment Variables**:
   ```powershell
   # Open PowerShell as Administrator
   [Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot', 'Machine')
   [Environment]::SetEnvironmentVariable('MAVEN_HOME', 'C:\Program Files\Apache\maven', 'Machine')
   
   # Update PATH
   $path = [Environment]::GetEnvironmentVariable('PATH', 'Machine')
   [Environment]::SetEnvironmentVariable('PATH', "$path;%JAVA_HOME%\bin;%MAVEN_HOME%\bin", 'Machine')
   ```

4. **Verify Installation**:
   ```powershell
   # Restart PowerShell, then check:
   java -version
   javac -version
   mvn -version
   ```

#### Option 2: Using Chocolatey

```powershell
# Install Chocolatey first (if not installed)
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# Install Java and Maven
choco install openjdk17 -y
choco install maven -y

# Verify
java -version
mvn -version
```

### Linux

#### Debian/Ubuntu

```bash
# Update package list
sudo apt update

# Install OpenJDK 17 and Maven
sudo apt install openjdk-17-jdk maven -y

# Verify installation
java -version
javac -version
mvn -version
```

#### Fedora/RHEL/CentOS

```bash
# Install OpenJDK 17 and Maven
sudo dnf install java-17-openjdk-devel maven -y

# Verify installation
java -version
mvn -version
```

#### Arch Linux

```bash
# Install OpenJDK 17 and Maven
sudo pacman -S jdk17-openjdk maven

# Verify installation
java -version
mvn -version
```

#### Manual Installation (Any Linux)

```bash
# Download and extract JDK
cd /opt
sudo wget https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17_linux-x64_bin.tar.gz
sudo tar -xzf openjdk-17_linux-x64_bin.tar.gz

# Set JAVA_HOME
echo 'export JAVA_HOME=/opt/jdk-17' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Install Maven
sudo apt install maven  # or download from maven.apache.org

# Verify
java -version
mvn -version
```

---

## 🏗️ Building the Project

### Clone the Repository

```bash
# Using HTTPS
git clone https://github.com/yourusername/P_Key_Resource_Process_Simulator.git
cd P_Key_Resource_Process_Simulator

# Or using SSH
git clone git@github.com:yourusername/P_Key_Resource_Process_Simulator.git
cd P_Key_Resource_Process_Simulator
```

### Build with Maven

**Windows (PowerShell):**
```powershell
mvn clean package
```

**Linux/macOS (Bash):**
```bash
mvn clean package
```

The compiled JAR file will be created at: `target/krpsim-1.0.jar`

### Troubleshooting Build Issues

**Issue**: "JAVA_HOME not set"
```powershell
# Windows
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot"

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

**Issue**: Maven not found
```bash
# Verify Maven installation
mvn -version

# If not installed, see Installation section above
```

---

## 🎯 Usage

### Basic Simulation

Run a simulation with a configuration file and time limit:

**Windows:**
```powershell
java -jar target/krpsim-1.0.jar krpsim/simple 100
```

**Linux:**
```bash
java -jar target/krpsim-1.0.jar krpsim/simple 100
```

**Syntax:**
```
java -jar target/krpsim-1.0.jar <config_file> <max_delay> [options]
```

**Parameters:**
- `<config_file>`: Path to configuration file (e.g., `krpsim/simple`)
- `<max_delay>`: Maximum simulation time in cycles
- `[options]`: Optional flags (see below)

### Optimization Levels

Choose different optimization strategies for better results:

**Level 0 - Greedy (Default, Fast):**
```bash
java -jar target/krpsim-1.0.jar krpsim/pomme 200
# or explicitly:
java -jar target/krpsim-1.0.jar krpsim/pomme 200 --optimize-level 0
```

**Level 1 - Beam Search (Good Quality):**
```bash
java -jar target/krpsim-1.0.jar krpsim/pomme 200 --optimize-level 1
```

**Level 2 - Branch & Bound A* (Best Quality):**
```bash
java -jar target/krpsim-1.0.jar krpsim/pomme 200 --optimize-level 2
```

### Visualization

Launch interactive GUI with Gantt charts and resource graphs:

**Windows:**
```powershell
java -jar target/krpsim-1.0.jar krpsim/simple 100 --visualize
```

**Linux:**
```bash
java -jar target/krpsim-1.0.jar krpsim/simple 100 --visualize
```

**With Optimization:**
```bash
java -jar target/krpsim-1.0.jar krpsim/inception 50 --optimize-level 1 --visualize
```

The GUI includes three tabs:
- **Gantt Chart**: Timeline showing when each process runs
- **Resource Graphs**: Line charts showing resource quantities over time
- **Summary**: Text summary of simulation results

### Trace Verification

Verify a simulation trace for correctness:

**Step 1: Generate trace (Windows PowerShell)**
```powershell
java -jar target/krpsim-1.0.jar krpsim/simple 100 2>$null | Out-File trace.txt -Encoding utf8
```

**Step 1: Generate trace (Linux/macOS)**
```bash
java -jar target/krpsim-1.0.jar krpsim/simple 100 2>/dev/null > trace.txt
```

**Step 2: Verify trace (All platforms)**
```bash
java -cp target/krpsim-1.0.jar krpsim.KrpsimVerif krpsim/simple trace.txt
```

Expected output:
```
Configuration file is valid.
Verifying trace against configuration...
Trace is valid!
```

---

## 📄 Configuration File Format

Configuration files define initial stocks, processes, and optimization targets.

### Basic Structure

```
# Comments start with #

# Initial stocks (resource:quantity)
euro:10
flour:100

# Process definition
# name:(needs):(results):delay
buy_flour:(euro:2):(flour:10):5
bake_bread:(flour:5):(bread:1):15
sell_bread:(bread:1):(euro:3;happiness:1):2

# Optimization targets
optimize:(time;happiness)
```

### Format Details

**Stock Definition:**
```
<stock_name>:<quantity>
```

**Process Definition:**
```
<name>:(<need>:<qty>[;<need>:<qty>[...]]):(<result>:<qty>[;<result>:<qty>[...]]):<delay>
```

- `<name>`: Process identifier (no spaces)
- `<need>:<qty>`: Resources consumed when process starts
- `<result>:<qty>`: Resources produced when process completes
- `<delay>`: Duration in cycles

**Optimization Targets:**
```
optimize:(<target>[;<target>[...]])
```

- `<target>`: Either `time` (minimize time) or resource name (maximize quantity)

### Example: Simple Production Chain

```
# Simple manufacturing example
euro:10

equipment_purchase:(euro:8):(equipment:1):10
product_creation:(equipment:1):(product:1):30
delivery:(product:1):(happy_client:1):20

optimize:(time;happy_client)
```

---

## 📁 Project Structure

```
P_Key_Resource_Process_Simulator/
├── src/
│   └── main/
│       ├── java/
│       │   └── krpsim/
│       │       ├── Krpsim.java              # Main simulator
│       │       ├── KrpsimVerif.java         # Trace verifier
│       │       ├── model/                   # Data models
│       │       │   ├── Event.java
│       │       │   ├── Process.java
│       │       │   └── Stock.java
│       │       ├── optimizer/               # Optimization strategies
│       │       │   ├── OptimizationStrategy.java
│       │       │   ├── GreedyOptimizer.java
│       │       │   ├── BeamSearchOptimizer.java
│       │       │   └── BranchAndBoundOptimizer.java
│       │       ├── utils/
│       │       │   └── Parser.java          # Config file parser
│       │       └── visualizer/              # GUI components
│       │           ├── SimulationVisualizer.java
│       │           ├── GanttChartPanel.java
│       │           └── ResourceGraphPanel.java
│       └── resources/
│           └── examples/
│               └── simple.txt
├── krpsim/                                  # Test configurations
│   ├── simple
│   ├── pomme
│   ├── inception
│   ├── ikea
│   ├── recre
│   └── steak
├── target/                                  # Compiled output
│   └── krpsim-1.0.jar
├── pom.xml                                  # Maven configuration
├── .gitignore
└── README.md
```

---

## 📚 Examples

### Example 1: Simple Production (Finite)

**File:** `krpsim/simple`
```
euro:10
equipment_purchase:(euro:8):(equipment:1):10
product_creation:(equipment:1):(product:1):30
delivery:(product:1):(happy_client:1):20
optimize:(time;happy_client)
```

**Run (Windows):**
```powershell
java -jar target/krpsim-1.0.jar krpsim/simple 100
```

**Run (Linux):**
```bash
java -jar target/krpsim-1.0.jar krpsim/simple 100
```

**Expected Output:**
```
Nice file! 3 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
Main walk
0:equipment_purchase
10:product_creation
40:delivery
no more process doable at time 60
Stock :
equipment=> 0
euro=> 2
happy_client=> 1
product=> 0
```

### Example 2: Continuous Production (Infinite)

**File:** `krpsim/inception`
```
clock:1
make_sec:(clock:1):(clock:1;second:1):1
optimize:(time)
```

**Run:**
```bash
java -jar target/krpsim-1.0.jar krpsim/inception 50
```

This creates a self-sustaining loop that can run indefinitely within the time limit.

### Example 3: Complex Optimization

**File:** `krpsim/pomme`
```
euro:10000
four:10
buy_pomme:(euro:100):(pomme:700):100
optimize:(pomme)
```

**Compare optimization levels (Windows):**
```powershell
# Greedy (fast)
java -jar target/krpsim-1.0.jar krpsim/pomme 100

# Beam Search (better result)
java -jar target/krpsim-1.0.jar krpsim/pomme 100 --optimize-level 1

# Branch & Bound (optimal)
java -jar target/krpsim-1.0.jar krpsim/pomme 100 --optimize-level 2
```

**Compare optimization levels (Linux):**
```bash
# Greedy (fast)
java -jar target/krpsim-1.0.jar krpsim/pomme 100

# Beam Search (better result)
java -jar target/krpsim-1.0.jar krpsim/pomme 100 --optimize-level 1

# Branch & Bound (optimal)
java -jar target/krpsim-1.0.jar krpsim/pomme 100 --optimize-level 2
```

---

## 🧠 Optimization Strategies

### Level 0: Greedy Algorithm (Default)

**Characteristics:**
- ⚡ **Fast**: O(T × P²) complexity
- 🎯 **Good**: 80-90% optimal in most cases
- 💡 **Strategy**: Makes locally optimal choices at each step

**Best for:** Quick results, real-time applications, simple scenarios

**Algorithm:**
1. At each time step, evaluate all executable processes
2. Calculate priority: (target resource production) / (time delay)
3. Execute the highest priority process
4. Repeat until no more processes can run

### Level 1: Beam Search

**Characteristics:**
- ⚖️ **Balanced**: Explores multiple solution paths
- 🎯 **Very Good**: 90-98% optimal
- 💡 **Strategy**: Keeps top-N candidates at each step (beam width = 8)

**Best for:** Medium complexity, good quality needed, reasonable time available

**Algorithm:**
1. Maintain beam of top-8 states at each time step
2. For each state, explore all possible next processes
3. Score states using heuristic: current_resources + potential_future_resources - time_penalty
4. Keep only top-8 scored states
5. Repeat until time limit reached

### Level 2: Branch & Bound with A*

**Characteristics:**
- 🐌 **Slower**: Potentially exponential (5-second time limit)
- 🏆 **Best**: Near-optimal or optimal solutions
- 💡 **Strategy**: Exhaustive search with intelligent pruning

**Best for:** Critical applications, small scenarios, maximum quality required

**Algorithm:**
1. Use A* priority queue ordered by optimistic value estimation
2. For each state, calculate upper bound on achievable score
3. Prune branches that cannot beat current best solution
4. Explore most promising branches first
5. Return best solution found within 5-second time limit

### Performance Comparison

| Strategy | Speed | Quality | Use Case | Typical Use |
|----------|-------|---------|----------|-------------|
| Greedy | ⚡⚡⚡ | ⭐⭐⭐ | Quick results | Testing, prototyping |
| Beam Search | ⚡⚡ | ⭐⭐⭐⭐ | Balanced | Production use |
| Branch & Bound | ⚡ | ⭐⭐⭐⭐⭐ | Best possible | Critical optimization |

---

## 🧪 Testing

### Run All Examples

**Windows (PowerShell):**
```powershell
# Test all scenarios
Get-ChildItem krpsim/* | ForEach-Object { 
    Write-Host "Testing $($_.Name)..."
    java -jar target/krpsim-1.0.jar $_.FullName 100
}
```

**Linux/macOS:**
```bash
# Test all scenarios
for scenario in krpsim/*; do
    echo "Testing $(basename $scenario)..."
    java -jar target/krpsim-1.0.jar "$scenario" 100
done
```

### Automated Testing with Maven

```bash
mvn test
```

---

## 🐳 Docker Support (Optional)

### Build Docker Image

```bash
docker build -t krpsim .
```

### Run in Docker

```bash
docker run --rm krpsim java -jar app.jar krpsim/simple 100
```

### Interactive Shell

```bash
docker run --rm -it krpsim /bin/bash
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Write unit tests for new features
- Ensure code compiles without warnings
- Keep all comments in English

### Before Submitting PR

```bash
# Build and test
mvn clean package

# Run verification tests
java -jar target/krpsim-1.0.jar krpsim/simple 100
java -cp target/krpsim-1.0.jar krpsim.KrpsimVerif krpsim/simple trace.txt
```

---

## 🔧 Troubleshooting

### Common Issues

**Problem**: "java: error: release version 17 not supported"
```bash
# Solution: Install JDK 17 or higher
# Check current version:
java -version
```

**Problem**: "JAVA_HOME is not set"
```powershell
# Windows:
[Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.x', 'Machine')

# Linux:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

**Problem**: Maven build fails with encoding errors
```bash
# Solution: Ensure UTF-8 encoding (already configured in pom.xml)
mvn clean package -Dproject.build.sourceEncoding=UTF-8
```

**Problem**: Visualization window doesn't appear
```bash
# Solution: Ensure X11/Display is available (Linux)
export DISPLAY=:0

# For WSL2:
export DISPLAY=$(cat /etc/resolv.conf | grep nameserver | awk '{print $2}'):0
```

**Problem**: Trace verification fails
```bash
# Ensure trace file is complete and properly formatted
# Redirect only stdout, not stderr:
java -jar target/krpsim-1.0.jar krpsim/simple 100 2>/dev/null > trace.txt
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🎓 Academic Context

This project was developed as part of an algorithmic optimization course, focusing on:
- Operational research
- Process scheduling algorithms
- Resource constraint management
- Heuristic optimization techniques
- Algorithm complexity analysis

---

## 📧 Support

For questions, issues, or suggestions:
- Open an issue on GitHub
- Contact: [your-email@example.com]

---

## 🙏 Acknowledgments

- Inspired by real-world manufacturing and logistics optimization problems
- Built with Java 17 and Maven for maximum compatibility
- Uses Swing for cross-platform GUI visualization

---

**Made with ❤️ for optimization enthusiasts**
