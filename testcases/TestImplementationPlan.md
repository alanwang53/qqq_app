# QQQ3X Strategy Android App Test Implementation Plan

## 1. Setup Test Environment

### 1.1 Dependencies
Add the following dependencies to the app's build.gradle file:

```gradle
// Testing dependencies
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:3.12.4'
testImplementation 'org.robolectric:robolectric:4.8'
testImplementation 'androidx.arch.core:core-testing:2.1.0'
testImplementation 'androidx.test:core:1.4.0'
testImplementation 'androidx.test.ext:junit:1.1.3'
testImplementation 'androidx.work:work-testing:2.7.1'

// Instrumented test dependencies
androidTestImplementation 'androidx.test.ext:junit:1.1.3'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
androidTestImplementation 'androidx.test:rules:1.4.0'
androidTestImplementation 'androidx.test:runner:1.4.0'
androidTestImplementation 'androidx.work:work-testing:2.7.1'
```

### 1.2 Test Directory Structure
Organize test files in the following structure:

```
app/
├── src/
│   ├── test/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── qqq3xstrategy/
│   │   │               ├── strategy/
│   │   │               ├── services/
│   │   │               ├── workers/
│   │   │               ├── util/
│   │   │               ├── data/
│   │   │               │   ├── models/
│   │   │               │   ├── database/
│   │   │               │   └── repository/
│   │   │               ├── ui/
│   │   │               └── receivers/
│   ├── androidTest/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── qqq3xstrategy/
│   │   │               ├── ui/
│   │   │               └── integration/
```

## 2. Implementation Phases

### Phase 1: Unit Tests (1 week)
1. Implement model tests (MarketDataTest, SignalHistoryTest)
2. Implement strategy tests (QQQ3XStrategyTest, QQQ3XStrategyAdditionalTest)
3. Implement view model tests (MainViewModelTest, PositionChangeViewModelTest)

### Phase 2: Integration Tests (1 week)
1. Implement service tests (StrategyCalculationServiceTest)
2. Implement worker tests (DataFetchWorkerTest, NotificationWorkerTest)
3. Implement utility tests (NotificationHelperTest)
4. Implement receiver tests (BootReceiverTest, NotificationActionReceiverTest)

### Phase 3: UI Tests (1 week)
1. Implement MainActivity UI tests
2. Implement PositionChangeDetailsActivity UI tests

### Phase 4: System Tests (1 week)
1. Implement scheduled execution tests
2. Implement network condition tests
3. Implement device state tests

## 3. Required Modifications to Production Code

### 3.1 Add Test Hooks
Add the following test hooks to enable easier testing:

1. In AppDatabase:
```java
private static AppDatabase testInstance;

public static void setTestInstance(AppDatabase instance) {
    testInstance = instance;
}

public static AppDatabase getInstance(Context context) {
    if (testInstance != null) {
        return testInstance;
    }
    // Existing implementation
}
```

2. In YahooFinanceRepository:
```java
private static YahooFinanceRepository testInstance;

public static void setTestInstance(YahooFinanceRepository instance) {
    testInstance = instance;
}

public static YahooFinanceRepository getInstance(Context context) {
    if (testInstance != null) {
        return testInstance;
    }
    // Existing implementation
}
```

3. In NotificationHelper:
```java
private static NotificationHelper testInstance;

public static void setTestInstance(NotificationHelper instance) {
    testInstance = instance;
}

public static NotificationHelper getInstance(Context context) {
    if (testInstance != null) {
        return testInstance;
    }
    return new NotificationHelper(context);
}
```

### 3.2 Add Dependency Injection
Refactor key classes to accept dependencies in constructors for easier testing:

1. Update workers to accept dependencies in constructors
2. Update services to accept dependencies in constructors
3. Update view models to accept dependencies in constructors

## 4. Test Data Management

### 4.1 Create Test Data Fixtures
Create reusable test data fixtures:

1. Create TestDataFactory class with methods to generate test data
2. Create JSON fixtures for network responses
3. Create database seed data for instrumented tests

### 4.2 Mock Web Server
Set up a mock web server for network tests:

1. Add OkHttp MockWebServer dependency
2. Create helper class to set up and tear down mock server
3. Create JSON response fixtures for different API endpoints

## 5. CI/CD Integration

### 5.1 GitHub Actions Setup
Set up GitHub Actions workflow:

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    - name: Run unit tests
      run: ./gradlew test
    - name: Run instrumented tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: 29
        script: ./gradlew connectedCheck
    - name: Generate test coverage report
      run: ./gradlew jacocoTestReport
    - name: Upload test coverage report
      uses: actions/upload-artifact@v2
      with:
        name: test-coverage-report
        path: app/build/reports/jacoco/jacocoTestReport
```

### 5.2 Test Coverage Reporting
Set up JaCoCo for test coverage reporting:

```gradle
apply plugin: 'jacoco'

jacoco {
    toolVersion = "0.8.7"
}

tasks.withType(Test) {
    jacoco.includeNoLocationClasses = true
    jacoco.excludes = ['jdk.internal.*']
}

task jacocoTestReport(type: JacocoReport, dependsOn: ['testDebugUnitTest', 'createDebugCoverageReport']) {
    reports {
        xml.enabled = true
        html.enabled = true
    }

    def fileFilter = ['**/R.class', '**/R$*.class', '**/BuildConfig.*', '**/Manifest*.*', '**/*Test*.*', 'android/**/*.*']
    def debugTree = fileTree(dir: "${buildDir}/intermediates/classes/debug", excludes: fileFilter)
    def mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.from = files([mainSrc])
    classDirectories.from = files([debugTree])
    executionData.from = fileTree(dir: "$buildDir", includes: [
            "jacoco/testDebugUnitTest.exec",
            "outputs/code-coverage/connected/*coverage.ec"
    ])
}
```

## 6. Test Execution Schedule

### 6.1 Local Development
- Run unit tests before each commit
- Run UI tests before submitting pull requests

### 6.2 CI/CD Pipeline
- Run unit tests on every commit
- Run UI tests on pull requests
- Run full test suite nightly

## 7. Documentation

### 7.1 Test Documentation
- Document test approach and strategy
- Document test fixtures and how to use them
- Document how to run tests locally

### 7.2 Test Reports
- Generate HTML test reports
- Generate coverage reports
- Track test metrics over time