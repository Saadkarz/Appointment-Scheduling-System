# SonarCloud Analysis - Quick Command

To run SonarCloud analysis on your project:

## Option 1: Using PowerShell Script (Recommended)
```powershell
.\run-sonar-analysis.ps1
```

## Option 2: Manual Command
```powershell
# Set the token
$env:SONAR_TOKEN = "a4fa9c3185eef7517c3a3909f2a7387b6a2cb479"

# Navigate to backend
cd backend

# Run the analysis
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=saad-g3_tp

# Go back
cd ..
```

## View Results
After the scan completes, view your analysis at:
https://sonarcloud.io/project/overview?id=saad-g3_tp

## Configuration
- **Organization**: saad-g3
- **Project Key**: saad-g3_tp
- **Token**: Stored in `.env` file

## What SonarCloud Analyzes
- Code quality issues
- Security vulnerabilities
- Code smells
- Technical debt
- Test coverage
- Code duplications
- Maintainability ratings
