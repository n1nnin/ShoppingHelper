# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ShoppingHelper is a **Kotlin Multiplatform (KMP)** shopping list application with location-based features. The app helps users manage shopping lists and provides geofencing notifications when near stores with pending items.

**Key Technologies:**
- Kotlin Multiplatform (v2.1.0)
- Jetpack Compose with Material 3
- SQLDelight (local database)
- Google Maps & Location Services
- Navigation Compose
- Koin (dependency injection)

## Common Commands

### Build & Development
```bash
# Build the project
./gradlew build

# Clean build artifacts
./gradlew clean

# Assemble debug APK
./gradlew assemble

# Build for specific targets
./gradlew compileDebugSources          # Android debug
./gradlew compileReleaseSources        # Android release
./gradlew compileKotlinIosArm64        # iOS ARM64
./gradlew compileKotlinIosSimulatorArm64 # iOS Simulator
```

### Testing & Verification
```bash
# Run all tests
./gradlew test

# Run unit tests
./gradlew testDebugUnitTest

# Run iOS tests
./gradlew iosX64Test
./gradlew iosSimulatorArm64Test

# Run all checks (lint + tests)
./gradlew check

# Android lint
./gradlew lint
./gradlew lintFix                      # Apply safe lint fixes

# Connected device tests
./gradlew connectedAndroidTest
```

### Database & SQLDelight
```bash
# Generate SQLDelight interfaces
./gradlew generateSqlDelightInterface

# Verify database migrations
./gradlew verifyCommonMainShoppingDatabaseMigration

# Build debug APK with Database Inspector support
./gradlew :composeApp:assembleDebug

# Run the app and use Android Studio's Database Inspector to:
# - View database schema and data
# - Run live queries on shopping.db
# - Monitor real-time data changes
```

### iOS Framework
```bash
# Build iOS framework
./gradlew linkDebugFrameworkIosArm64
./gradlew linkReleaseFrameworkIosArm64

# Embed framework for Xcode
./gradlew embedAndSignAppleFrameworkForXcode
```

## Architecture

### Module Structure
- **composeApp/**: Android application with shared UI code
  - `commonMain/`: Shared Compose UI components and screens
  - `androidMain/`: Android-specific implementations (Maps, Geofencing, Notifications)
- **shared/**: Shared business logic module
  - `commonMain/`: Platform-agnostic domain models
  - `androidMain/`: Android-specific shared code
  - `iosMain/`: iOS-specific shared code

### Key Components

**Navigation Structure:** Bottom navigation with three main screens
- `ShoppingListScreen` (shopping lists management)
- `ListItemsScreen` (items for selected list)
- `ShopsScreen` (shops and map view)
- `MapScreen` (Google Maps integration)

**Domain Models:**
- `ShoppingList`: Multiple shopping lists with active state management
- `ShoppingItem`: Items with priority, category, and shop association
- `Shop`: Store information with location and category
- `ItemTemplate`: Frequently used items for quick addition
- `Geofence`: Location-based notifications (100m radius default)

**Data Layer Architecture:**
- **Repository Pattern**: `ShoppingRepository` interface with `SqlDelightShoppingRepository` implementation
- **Local Database**: SQLDelight with `shopping.db` containing 4 tables (shopping_list, shopping_item, shop, item_template)
- **Data Migration**: `DataMigrationHelper` for automatic migration from SharedPreferences to SQLDelight
- **Reactive Data**: Kotlin Flow for real-time UI updates with automatic SQL-to-Flow conversion
- **Type Safety**: Custom ColumnAdapters for enum types (Priority, ItemCategory, ShopCategory)
- **Dependency Injection**: Koin modules for database, repository, and platform-specific drivers

**UI Architecture:**
- Clean separation with UI models (`ShopUi`, `ShoppingItemUi`) extending domain models
- Reusable components: `AddItemDialog`, `EditItemDialog`, `AddShopDialog`, `ShoppingItemCart`
- Enhanced dialogs with shop selection for flexible item management
- Platform-specific: `AndroidMapView` for Google Maps integration

### Configuration
- **Min SDK**: Android 29 (Android 10)
- **Target SDK**: Android 34
- **Maps API Key**: Configured in `gradle.properties` as `MAPS_API_KEY`
- **Namespace**: `xyz.moroku0519.shoppinghelper`

## Development Notes

### Dependencies
- **SQLDelight**: Fully implemented for local database operations with automatic migration
  - Android: `android-driver` with SQLite
  - iOS: `native-driver` with platform SQLite
  - Schema: 4 tables with foreign key constraints and optimized indexes
- **Koin**: Actively used for dependency injection across Android/iOS platforms
  - Database drivers, repository, and enum adapters configuration
- **Google Play Services**: Required for Maps and Location features
- **Kotlin Serialization**: For JSON serialization and data migration
- **Navigation Compose**: For screen navigation and bottom tab management

### Platform-Specific Features
- **Android**: Full Google Maps integration, geofencing, background location, push notifications
- **iOS**: Basic setup with SwiftUI, shared business logic via Kotlin/Native framework

### Location & Permissions
The app requires location permissions for core functionality. Use `LocationPermissionHandler` component for permission management.

**Required Permissions:**
- `ACCESS_FINE_LOCATION`: For precise location access
- `ACCESS_BACKGROUND_LOCATION`: For geofence functionality while app is in background
- `POST_NOTIFICATIONS`: For geofence notifications
- `FOREGROUND_SERVICE_LOCATION`: For location-based services

### Map Integration
Google Maps is integrated via Maps Compose library. API key must be configured in `gradle.properties` for Maps functionality to work.

### Geofence Functionality
Complete geofence system that sends notifications when users enter shop areas:

**Key Components:**
- `GeofenceManager`: Manages geofence registration and removal
- `GeofenceBroadcastReceiver`: Handles geofence events and triggers notifications
- `ShoppingNotificationManager`: Creates and displays notifications
- `GeofenceIntegration`: Helper functions for easy integration

**Usage:**
```kotlin
// Setup geofences for shops with pending items
val geofenceManager = GeofenceManager(context)
geofenceManager.setupGeofencesForShops(shopsWithPendingItems)

// Remove all geofences
geofenceManager.removeAllGeofences()

// Test notification
val notificationManager = ShoppingNotificationManager(context)
notificationManager.showTestNotification()
```

**Testing:**
Use `GeofenceDemo.kt` components to test geofence functionality in development. Default geofence radius is 100 meters with 24-hour expiration.