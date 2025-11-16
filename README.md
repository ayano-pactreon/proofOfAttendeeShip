# SubZero

Android app for event attendee registration with Luma API integration, QR code scanning, and offline storage.

## Features

- Download attendee lists from Luma events
- QR code scanning for registration
- Offline access with Room database
- Material Design 3 UI

## Requirements

- Android 11 (API 30) or higher
- Luma Plus subscription for API access

## Quick Start

1. **Build & Install**
   ```bash
   ./gradlew installDebug
   ```

2. **Configure Luma Sync**
   - Get API key from [Luma Settings](https://lu.ma/settings/api)
   - Open Settings tab in app
   - Enter API key and Event ID
   - Click "Download Attendees"

3. **View Attendees**
   - Navigate to Users tab to see synced data

## Tech Stack

- Jetpack Compose
- Room Database
- Retrofit + OkHttp
- CameraX + ML Kit
- Kotlin Coroutines

## Build

```bash
./gradlew build          # Build project
./gradlew test           # Run tests
./gradlew lint           # Lint checks
```
#####
Project Video Link

```
https://drive.google.com/drive/folders/1dKv4vjnRAWc8SUgzrz5-Ih71f2VjxmMf?usp=sharing
```

###
Project Workflows

Registration Flow:

👉 [AttendeShip Documentation (PDF)](./docs/00_Sub0_App-Registration_flow.pdf)


Collecting points at activations with NFC tap:

👉 [AttendeShip Documentation (PDF)](./docs/01_Sub0_App-Earn_Points-NFC_Tap_Flow.pdf)


Collecting points at activations with QR scan:

👉 [AttendeShip Documentation (PDF)](./docs/02_Sub0_App-Earn_Points-QR_Scan_Flow.pdf)

Viewing balances:

👉 [AttendeShip Documentation (PDF)](./docs/03_Sub0_App-Check_Balance_Flow.pdf)


Paying for things with points:

👉 [AttendeShip Documentation (PDF)](./docs/04_Sub0_App_-_Merchandise_Redemption_Flow.pdf)