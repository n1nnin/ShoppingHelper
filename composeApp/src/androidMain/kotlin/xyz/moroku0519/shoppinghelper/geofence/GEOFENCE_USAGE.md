# Geofence Integration Usage Guide

This document explains how the geofence functionality is now integrated into the ShoppingHelper app.

## ✅ What's Been Implemented

### 1. Automatic Geofence Setup
- **ShopsScreen**: Automatically sets up geofences for all shops with pending items when the screen loads
- **MapScreen**: Also sets up geofences when viewing the map with shops
- **Real-time Updates**: Geofences are updated whenever the shop list changes

### 2. Platform-Specific Implementation
- **Common**: `GeofenceHandler` expect/actual pattern for cross-platform compatibility
- **Android**: Full geofence implementation with Google Play Services
- **iOS**: Placeholder (can be implemented later with CoreLocation)

### 3. Smart Filtering
- Only shops with `pendingItemsCount > 0` get geofences
- Automatic cleanup when shops no longer have pending items
- 100-meter radius with 24-hour expiration per geofence

## 🚀 How It Works

### Automatic Integration
```kotlin
// In ShopsScreen.kt and MapScreen.kt
GeofenceHandler(shops) // Automatically called when shops list changes
```

### Manual Control (Available via GeofenceIntegration)
```kotlin
// Setup geofences for specific shops
GeofenceIntegration.setupGeofences(context, shopsWithPendingItems)

// Remove all geofences
GeofenceIntegration.removeAllGeofences(context)

// Test notifications
GeofenceIntegration.testNotification(context)
```

## 📱 User Experience

1. **Shop Management**: When users add items to shops, geofences are automatically created
2. **Location Detection**: When users enter a shop area (100m radius), they get a notification
3. **Smart Notifications**: Shows shop name and number of pending items
4. **Tap to Open**: Notification opens the app to the shopping list

## 🔧 Testing

### Test Notification Button
- Use `GeofenceTestButton` component to test notifications
- Can be added to any screen for development/testing

### Sample Shop Data
- Both ShopsScreen and MapScreen have predefined shops with pending items
- Shop1 (イオン): 3 pending items → Will trigger geofence
- Shop2 (ツルハドラッグ): 1 pending item → Will trigger geofence  
- Shop3 (セブンイレブン): 0 pending items → No geofence

## 🎯 Key Features

- **Background Processing**: Works even when app is closed
- **Permission Handling**: Automatic checks for location and notification permissions
- **Error Handling**: Graceful fallbacks if permissions are denied
- **Logging**: Comprehensive logging for debugging
- **Battery Efficient**: Only active geofences for shops with pending items

## 📋 Required Permissions

The app automatically requests these permissions:
- `ACCESS_FINE_LOCATION`: For precise location
- `ACCESS_BACKGROUND_LOCATION`: For geofence monitoring while app is closed
- `POST_NOTIFICATIONS`: For showing geofence notifications

## 🔄 Automatic Lifecycle

1. **Setup**: Geofences created when shops with pending items are loaded
2. **Update**: Geofences refreshed when shop data changes
3. **Trigger**: Notifications sent when user enters shop area
4. **Cleanup**: Geofences removed when no longer needed

The integration is now complete and works seamlessly with the existing app architecture!