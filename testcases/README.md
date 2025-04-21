# QQQ3X Strategy Android App Test Cases

## Overview

This directory contains test cases for the QQQ3X Strategy Android app. The test cases cover various aspects of the application, including strategy logic, data models, services, workers, and UI components.

## Test Files

1. **Strategy Tests**
   - `QQQ3XStrategyTest.java` - Tests for the core strategy logic under various market conditions
   - `QQQ3XStrategyAdditionalTest.java` - Additional tests focusing on the SMA calculation functionality

2. **Service Tests**
   - `StrategyCalculationServiceTest.java` - Tests for the service that calculates strategy signals

3. **Worker Tests**
   - `DataFetchWorkerTest.java` - Tests for the worker that fetches market data
   - `NotificationWorkerTest.java` - Tests for the worker that sends notifications

4. **Utility Tests**
   - `NotificationHelperTest.java` - Tests for the notification system

5. **Receiver Tests**
   - `BootReceiverTest.java` - Tests for the broadcast receiver that reschedules tasks after device reboot
   - `NotificationActionReceiverTest.java` - Tests for the broadcast receiver that handles notification actions

6. **Model Tests**
   - `MarketDataTest.java` - Tests for the MarketData model
   - `SignalHistoryTest.java` - Tests for the SignalHistory model

7. **ViewModel Tests**
   - `MainViewModelTest.java` - Tests for the main activity's view model
   - `PositionChangeViewModelTest.java` - Tests for the position change details activity's view model

## Documentation

- `TestCasesSummary.md` - Summary of all test cases and their coverage
- `TestImplementationPlan.md` - Plan for implementing the tests in the project

## Running the Tests

To run the tests:

1. **Unit Tests**:
   ```
   ./gradlew test
   ```

2. **Instrumented Tests**:
   ```
   ./gradlew connectedAndroidTest
   ```

3. **All Tests with Coverage Report**:
   ```
   ./gradlew jacocoTestReport
   ```

## Test Coverage

These test cases aim to provide comprehensive coverage of the application's functionality:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test interactions between components
- **UI Tests**: Test user interface components and interactions
- **System Tests**: Test the application under real-world conditions

## Implementation Notes

1. Some tests require modifications to the production code to enable testing, such as adding test hooks.
2. Many tests use mocking to isolate the component being tested.
3. LiveData testing requires special handling to make updates synchronous.
4. UI tests require Espresso and should be implemented in the androidTest directory.

## Next Steps

1. Implement the tests according to the implementation plan
2. Set up CI/CD integration to run tests automatically
3. Generate and monitor test coverage reports
4. Expand test coverage as new features are added