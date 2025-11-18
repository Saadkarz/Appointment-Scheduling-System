# SonarCloud Analysis Script for Windows PowerShell
# This script runs SonarCloud analysis on your Spring Boot project

Write-Host "Starting SonarCloud Analysis..." -ForegroundColor Green

# Set environment variable
$env:SONAR_TOKEN = "50ddead4065d1f422755905c6cec8b10e1be50ff"

# Navigate to backend directory
Set-Location -Path ".\backend"

Write-Host "Running Maven verify and SonarCloud scan..." -ForegroundColor Yellow

# Run Maven with SonarCloud plugin
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
    -Dsonar.projectKey=saad-g3_tp `
    -Dsonar.organization=saad-g3 `
    -Dsonar.host.url=https://sonarcloud.io

Write-Host "SonarCloud Analysis Complete!" -ForegroundColor Green
Write-Host "View your results at: https://sonarcloud.io/project/overview?id=saad-g3_tp" -ForegroundColor Cyan

# Return to root directory
Set-Location -Path ".."
