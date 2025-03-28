# AI-Powered Attendance Marking & Parent Notification App

Welcome to the **Hackverse 2025** project by **The Debugging Squad**! This is an innovative EduTech solution designed to ensure secure, proxy-proof attendance tracking with real-time notifications for parents and faculty. Built during the **CSI 5 WIET Hackathon**, this app leverages AI, QR codes, face recognition, and geofencing to modernize attendance systems.

---

## Project Overview

The **AI-Powered Attendance Marking & Parent Notification App** eliminates manual attendance efforts, prevents proxy attendance, and provides actionable insights through AI-driven analytics. It uses triple-layered authentication (QR Code, Face Recognition, and Geofencing) and integrates WhatsApp for instant alerts to parents and faculty.

### Team: The Debugging Squad
- **Nitesh Ray**
- **Shreya Deore**
- **Varshada Kothawade**
- **Rahul Ahir**

### Domain
EduTech

---

## Key Features

- **Triple-Layered Secure Attendance**:
  - Dynamic QR Code (refreshes every 10 seconds).
  - Face Recognition using FaceNet for identity verification.
  - Geofencing to ensure on-campus attendance.
- **AI-Based Attendance Analytics**:
  - Detects absentee patterns, tracks latecomers, and predicts trends.
- **Anti-Spoofing**:
  - Face Liveness Detection (blink and head movement tracking) to prevent photo/video fraud.
- **AI-Powered Warning System**:
  - Predicts low attendance risks and sends WhatsApp alerts if attendance falls below 75%.
- **Instant WhatsApp Alerts**:
  - Real-time absence notifications to parents and faculty with reply functionality.
- **Automated Reports**:
  - Faculty can request attendance reports via WhatsApp.

---

## Problem Solved

- **Eliminates Proxy Attendance**: Prevents students from marking attendance for absent peers.
- **Reduces Manual Effort**: Automates attendance for faculty.
- **Ensures Location Authenticity**: Blocks remote check-ins.
- **Real-Time Insights**: Tracks absentee patterns and notifies stakeholders instantly.

---

## How It Works

1. **Attendance Session Start**: Faculty initiates a session, generating a dynamic QR code.
2. **Student Verification**: Students scan the QR code, followed by face recognition and geolocation checks.
3. **Pass/Fail**:
   - **Pass**: Attendance marked in Firebase.
   - **Fail**: Attendance denied if any check fails.
4. **Notifications**: Parents are alerted via WhatsApp if a student is absent after 15 minutes.
5. **Analytics**: Real-time data stored for future insights.

---

## Technologies Used

### Programming Languages
- **Kotlin**: Core app development.
- **XML**: UI design.

### Frameworks & Libraries
- **TensorFlow Lite (FaceNet)**: Face recognition.
- **Google ML Kit**: QR code scanning.
- **Google Play Services**: Geofencing.
- **Twilio API**: WhatsApp notifications.

### Backend & Database
- **Firebase Authentication**: User login and verification.
- **Firebase Cloud Messaging**: Push notifications.
- **Firebase Firestore**: Real-time attendance storage.

### Location & Security
- **GPS & Geofencing**: Location verification.
- **Anti-Spoofing**: Liveness detection via FaceNet.

### Development Tools
- **Android Studio**: App development.
- **Firebase Console**: Backend management.
- **GitHub**: Version control.

---

## Installation

### Prerequisites
- Android Studio (latest version)
- Firebase account
- Twilio account (for WhatsApp API)
- Android smartphone (API 21+)

### Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/[your-username]/ai-attendance-app.git
   cd ai-attendance-app
