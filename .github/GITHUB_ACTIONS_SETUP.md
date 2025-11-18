# GitHub Actions Setup Guide

## Adding SonarCloud Token to GitHub Secrets

1. Go to your GitHub repository: https://github.com/Saadkarz/Appointment-Scheduling-System

2. Click on **Settings** → **Secrets and variables** → **Actions**

3. Click **New repository secret**

4. Add the following secret:
   - Name: `SONAR_TOKEN`
   - Value: `50ddead4065d1f422755905c6cec8b10e1be50ff`

5. Click **Add secret**

## Workflows

### SonarCloud Analysis (`sonar-analysis.yml`)
- Runs on every push to `main` or `develop` branches
- Runs on pull requests
- Compiles Java code with Maven
- Performs SonarCloud analysis
- Results visible at: https://sonarcloud.io/project/overview?id=saad-g3_tp

### Build and Test (`build.yml`)
- Builds backend with Maven
- Runs backend tests
- Builds frontend with npm
- Runs frontend tests
- Builds Docker images

## Viewing Results

After pushing code:
1. Go to **Actions** tab in your GitHub repository
2. Click on the running workflow
3. View logs and results
4. Check SonarCloud dashboard for code quality metrics

## Badges for README

Add these to your README.md:

```markdown
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=saad-g3_tp&metric=alert_status)](https://sonarcloud.io/project/overview?id=saad-g3_tp)
[![Build Status](https://github.com/Saadkarz/Appointment-Scheduling-System/workflows/Build%20and%20Test/badge.svg)](https://github.com/Saadkarz/Appointment-Scheduling-System/actions)
```
