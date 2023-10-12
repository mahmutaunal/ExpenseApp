# Shared Income and Expense Tracking App

This project aims to create a mobile application where two users can track their expenses and be informed of each other's spending in real-time.

This application is developed to provide control and tracking of expenses. Firebase and other technologies support functionalities such as live tracking and notification sending. The project is prepared in compliance with Kotlin, MVVM architecture, DataBinding, and other modern Android development principles.

## Technologies and Tools Used
- Kotlin: The programming language used for app development.
- Firebase: A cloud-based platform used for functions such as user authentication, matching, and live tracking.
- Firebase Cloud Messaging (FCM): A tool used for sending notifications.
- MVVM Architecture: A design pattern used to organize the application's structure.
- DataBinding: A library used to bind data to the user interface.
- Kotlin Coroutines: A Kotlin library used to simplify and manage asynchronous tasks.
- Retrofit: A library used to manage HTTP requests.
- Google Maps API: Integrated service for displaying expenses on a map.

## App Screens and Features
### Login & Register Screen
The application is used with email and password.

### Main Screen
This screen represents the main screen where expenses are listed using CardView.

### Expense Adding Screen
This screen allows for the addition of new expenses. It includes category selection (mandatory), amount input (mandatory), and optional location addition.

### Matching Screen
This screen or dialog allows users to match with each other. Matching can only occur with one person.

### Live Tracking Screen
This screen shows the movements of the person being tracked in real-time, provided the other person has given permission.

## Setup

### Firebase
- Setup Authentication and use the Sign-in method 'Email/Password'.
- Setup Realtime Database.
- Setup Storage.
- Replace the file google-services.json.
- Note: Download the google-services.json file after the Firebase services are set up to automatically include the services in the json file.
- Note: When updating the google-services.json file then make sure to invalidate the caches as well as doing a clean + rebuild.

### Project
1. Download and open the project in Android Studio.
2. Connect your Android phone or use the emulator to start the application.


## License
This application is released under BSD-3-Clause license (see [LICENSE](LICENSE)).
