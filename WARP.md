# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is an Android game development simulation app called "Yjcy" (游创纪元 - Game Development Era). It's a business simulation game where players create and manage a game development company, hire employees, develop games, and compete in the market.

## Development Commands

### Build & Run
```powershell
# Build debug version
.\gradlew assembleDebug

# Build and install debug APK
.\gradlew installDebug

# Run tests
.\gradlew test

# Run connected tests (requires device/emulator)
.\gradlew connectedAndroidTest

# Clean build
.\gradlew clean

# Check dependencies for updates
.\gradlew dependencyUpdates
```

### Development Tools
```powershell
# Run lint checks
.\gradlew lint

# Generate test coverage report
.\gradlew jacocoTestReport

# Run KSP annotation processing
.\gradlew kspDebugKotlin

# Check code style with ktlint (if configured)
.\gradlew ktlintCheck
```

### Testing
```powershell
# Run unit tests
.\gradlew testDebugUnitTest

# Run specific test class
.\gradlew test --tests "com.example.yjcy.ExampleUnitTest"

# Run instrumentation tests
.\gradlew connectedDebugAndroidTest

# Run tests with coverage
.\gradlew testDebugUnitTest jacocoTestDebugUnitTestReport
```

## Architecture Overview

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Architecture Pattern**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **Database**: Room (SQLite)
- **State Management**: Compose State + ViewModels
- **Data Serialization**: Gson
- **Build System**: Gradle with Kotlin DSL

### Project Structure

```
app/src/main/java/com/example/yjcy/
├── MainActivity.kt                    # Main entry point with navigation setup
├── YjcyApplication.kt                # Application class with Hilt setup
├── config/                           # Configuration constants
├── data/                            # Data models and business logic
│   ├── Employee.kt                  # Employee data class
│   ├── Game.kt                      # Game project data class
│   ├── SaveData.kt                  # Save game data structure
│   ├── FounderProfession.kt         # Enum for founder careers
│   ├── GameRatingCalculator.kt      # Game quality rating logic
│   ├── RevenueCalculator.kt         # Revenue generation system
│   └── TalentCandidate.kt          # HR candidate data model
├── database/                        # Room database setup
│   └── migrations/                  # Database migration files
├── di/                             # Hilt dependency injection modules
├── service/                        # Business service classes
│   ├── RecruitmentService.kt       # Employee recruitment logic
│   └── TalentMarketService.kt      # HR talent market system
├── ui/                             # UI components and screens
│   ├── components/                 # Reusable UI components
│   ├── screens/                    # Screen compositions
│   ├── theme/                      # App theme definitions
│   ├── EmployeeManagementContent.kt # Employee management UI
│   └── ProjectManagementWrapper.kt # Game project UI
└── utils/                          # Utility functions
```

### Core Game Systems

#### 1. Company Management System
- **Company Creation**: Players set up company name, logo, and founder details
- **Financial Management**: Track company funds, revenue, and expenses
- **Time Progression**: Day/month/year system with configurable game speed
- **Save System**: 3-slot save system using SharedPreferences + Gson

#### 2. Employee Management System
- **Founder System**: Special employee with career-specific skills (Programmer, Designer, Artist, Sound Engineer, Customer Service)
- **Employee Hierarchy**: Different positions with skill levels (Development, Design, Art, Music, Service)
- **Training System**: Skill progression through training investments
- **Talent Market**: Procedural candidate generation with filtering and recruitment

#### 3. Game Development System
- **Project Creation**: Multi-step game creation with theme selection
- **Development Progress**: Real-time progress tracking based on assigned employees
- **Quality Rating**: Multi-factor rating system (development, design, art, music, service quality)
- **Release System**: Price setting, market release, and revenue generation
- **Genre & Theme**: Various game genres and themes affecting market performance

#### 4. Revenue & Market System
- **Dynamic Pricing**: Price recommendation engine based on game quality
- **Market Performance**: Day-by-day sales tracking with rating influence
- **Long-term Revenue**: Sustained income from released games
- **Fan System**: Player base growth affecting future sales

### Key Architectural Patterns

#### State Management
- **Compose State**: Local UI state management with `remember` and `mutableStateOf`
- **State Hoisting**: Parent components manage child state for data flow
- **Global State**: Game state passed through navigation and component hierarchy

#### Navigation Pattern
```kotlin
// Screen navigation using Navigation Compose
NavHost(navController, startDestination = "main_menu") {
    composable("main_menu") { MainMenuScreen(navController) }
    composable("game_setup") { GameSetupScreen(navController) }
    composable("game/{companyName}/{founderName}/{selectedLogo}/{founderProfession}") { 
        backStackEntry -> GameScreen(/* parameters */) 
    }
}
```

#### Data Flow Pattern
```kotlin
// Typical data flow in game screens
GameScreen -> CompanyOverviewContent/EmployeeManagement/ProjectManagement
    ↓
State updates flow up through callbacks
    ↓  
Parent component manages state and persistence
```

### Important Implementation Details

#### Save System
- Uses Android SharedPreferences for persistence
- JSON serialization via Gson
- 3-slot save system with metadata (company name, time, funds)
- Backward compatibility handling for data model evolution

#### Game Time System
- Configurable speed: 1x (2s/day), 2x (1s/day), 3x (0.5s/day)
- Pause functionality
- Real-time game development progress updates
- Revenue generation tied to time progression

#### Employee Skills & Training
- 5-level skill system (1-5) across 5 categories
- Founder has level 5 in specialty, level 1-2 in others
- Training costs scale with employee salary
- Skill levels affect game development speed and quality

#### Game Development Pipeline
1. **Project Setup**: Name, genre, theme, platform selection
2. **Employee Assignment**: Assign team members to project
3. **Development Phase**: Progress based on team skill total
4. **Completion & Rating**: Automated quality calculation
5. **Release Setup**: Price setting with recommendation engine
6. **Market Performance**: Revenue tracking and fan growth

### Privacy & Compliance Features
- **Privacy Policy Dialog**: Mandatory first-launch privacy agreement
- **Data Storage**: Local-only save data, no external data transmission
- **Permission Handling**: Minimal permissions (Internet for future features)

### UI/UX Architecture

#### Theme System
- **Modern Design**: Gradient backgrounds, glass-morphism effects
- **Responsive Layout**: Adaptive layouts for different screen sizes
- **Animation System**: Smooth transitions and micro-interactions
- **Color Scheme**: Purple-blue gradients with orange accents

#### Component Architecture
- **Reusable Components**: ModularButton, DataCard, SkillBar components
- **Dialog System**: Consistent dialog patterns for confirmations and forms
- **Navigation**: Bottom tab navigation with smooth animations
- **State Animations**: Loading states, progress indicators, and transitions

## Development Guidelines

### Code Organization
- Group related functionality in packages (data, ui, service)
- Use clear naming conventions: `*Content.kt` for screen compositions
- Separate business logic from UI code
- Implement proper error handling and loading states

### Performance Considerations
- Use `remember` for expensive calculations in Compose
- Implement proper state management to avoid recomposition issues
- Optimize navigation argument passing
- Consider lazy loading for large data sets

### Testing Strategy
- Unit tests for business logic (GameRatingCalculator, RevenueCalculator)
- UI tests for critical user flows (game creation, save/load)
- Integration tests for data persistence
- Performance testing for real-time game updates

### Future Architecture Considerations
- Room database migration for complex data relationships
- ViewModel integration for better state management
- Repository pattern for data access abstraction
- Coroutines for background processing (time system, auto-save)

This architecture supports a complex business simulation game with multiple interconnected systems while maintaining code clarity and extensibility.