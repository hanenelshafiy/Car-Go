CarGo – Car Reservation Android App

CarGo is a comprehensive Android application designed to streamline the car reservation process. Built with Kotlin and following modern Android development practices, the app provides a seamless experience for browsing, selecting, and managing vehicle reservations.


 Features

Secure Authentication: Integrated Firebase Authentication for user accounts, including sign-up, login, and profile management.


Car Catalog: A dynamic Home screen to browse available vehicles with detailed Car Details views.


Reservation System: A functional Cart system allowing users to manage their selections before finalizing reservations.



Local & Cloud Data: Utilizes Room Database with DAO for local reservation management and Firebase Database for cloud-based user and profile data.


User Profiles: Dedicated User Profile section to view and manage personal account information.

 Architecture & Tech Stack
The project is architected using the MVVM (Model-View-ViewModel) pattern to ensure a clean separation of concerns and maintainability.



Language: Kotlin.


Local Database: Room Database with Data Access Objects (DAO).


Backend/Auth: Firebase Authentication and Realtime Database/Firestore.


Design Patterns: Repository pattern for data abstraction.


Reactive UI: LiveData for lifecycle-aware data handling and UI updates.


Testing: Unit tests written in JUnit to validate business logic and ensure code reliability.

🛠️ Installation
Clone the repository:

Bash

git clone https://github.com/hanenelshafiy/Car-Go.git
Open in Android Studio:

Ensure you have the latest version of Android Studio installed.

Firebase Setup:

Create a project in the Firebase Console.

Add your google-services.json file to the app/ directory.

Build & Run:

Sync Gradle and run the application on an emulator or physical device.
