<#
PowerShell helper to run common Makefile-like targets on Windows
Usage: .\make.ps1 <target> [-Scenario <path>] [-Steps <n>]
#>
param(
    [Parameter(Mandatory=$true, Position=0)] [string]$Target,
    [string]$Scenario = 'krpsim/simple',
    [int]$Steps = 100
)

function Run-MvnBuild {
    Write-Host "==> Running mvn clean package -DskipTests"
    mvn clean package -DskipTests
}

function Run-DockerBuild {
    Write-Host "==> Building Docker image 'krpsim'"
    docker build -t krpsim .
}

switch ($Target.ToLower()) {
    'mvn-build' { Run-MvnBuild; break }
    'jar' { Run-MvnBuild; break }
    'docker-build' { Run-DockerBuild; break }
    'build' { Run-DockerBuild; break }
    'run' {
        $jar = Join-Path -Path 'target' -ChildPath 'krpsim-1.0.jar'
        if (Test-Path $jar) {
            Write-Host "Found $jar — running locally"
            java -jar $jar $Scenario $Steps
        } else {
            Write-Host "Jar not found — building image and running in Docker"
            Run-DockerBuild
            docker run --rm -it krpsim java -jar app.jar $Scenario $Steps
        }
        break
    }
    'run-local' {
        $jar = Join-Path -Path 'target' -ChildPath 'krpsim-1.0.jar'
        if (Test-Path $jar) { java -jar $jar $Scenario $Steps } else { Write-Error "Jar not found. Run '.\make.ps1 mvn-build' first." }
        break
    }
    'shell' {
        Run-DockerBuild
        docker run --rm -it krpsim /bin/bash
        break
    }
    'clean' {
        Write-Host "Removing Docker image 'krpsim' (if exists)"
        docker rmi krpsim -f
        break
    }
    default {
        Write-Host "Usage: .\make.ps1 <target> [-Scenario <path>] [-Steps <n>]"
        Write-Host "Available targets: mvn-build, jar, docker-build, build, run, run-local, shell, clean"
    }
}
