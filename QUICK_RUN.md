# Quick Run Guide

## Option 1: Using Batch Script (Windows CMD)
```bash
run.bat
```

## Option 2: Using PowerShell Script
```powershell
.\run.ps1
```

## Option 3: Direct Maven Command (CMD)
```bash
mvn compile exec:java
```

## Option 4: Direct Maven Command (PowerShell)
```powershell
mvn compile exec:java
```

## Option 5: From Your IDE
1. Open `src/main/java/edu/univ/erp/App.java`
2. Right-click → Run As → Java Application

---

**Note:** The `exec-maven-plugin` is now configured in `pom.xml` with the main class, so you don't need to specify `-Dexec.mainClass` anymore.

