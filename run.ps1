param(
    [string]$scenario = "krpsim/simple",
    [int]$steps = 100
)

$jar = "target/krpsim-1.0.jar"
if (-not (Test-Path $jar)) {
    Write-Error "Jar not found: $jar. Run `mvn clean package -DskipTests` first.`"
    exit 1
}

java -jar $jar $scenario $steps
