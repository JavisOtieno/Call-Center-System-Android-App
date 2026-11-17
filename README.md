# Call Center System – Android App

A native Android application for call center agents to handle voice calls, video calls, messages, WhatsApp, emails, and customer interactions using Twilio.

[![Android](https://img.shields.io/badge/Android-Java-3DDC84?style=flat&logo=android)](https://developer.android.com)
[![Twilio](https://img.shields.io/badge/Twilio-Voice%20%26%20Video-F22F46?style=flat&logo=twilio)](https://www.twilio.com)
[![minSdk](https://img.shields.io/badge/minSdk-27-blue)](https://developer.android.com)

## Overview

**Call Center System Android App** (SCG Calls) is a mobile tool designed for call center agents. It enables agents to make and receive voice and video calls, manage conversations across multiple channels (SMS/messages, WhatsApp, email), track callers, and stay connected via real-time notifications.

Built with **Twilio Programmable Voice & Video**, Firebase Cloud Messaging, and a custom backend API.

**Package:** `com.scg.scgcalls`

## Key Features

- **Voice Calls** – Make and receive calls via Twilio Voice SDK
- **Video Calls** – Join or create video rooms using Twilio Video
- **Incoming & Outgoing Call Management**
- **Dial Pad** – Manual dialing support
- **Messaging** – In-app messages and message threads
- **WhatsApp Integration** – Send and view WhatsApp messages
- **Email** – View and manage emails
- **Caller Management** – List and view caller details
- **Push Notifications** – Firebase Cloud Messaging for incoming calls and alerts
- **Real-time Updates** – Pusher integration
- **Login & Dashboard**
- Secure local storage

## Tech Stack

| Component              | Technology                              |
|------------------------|-----------------------------------------|
| Language               | Java                                    |
| UI                     | AndroidX + Material Design              |
| Voice Calls            | Twilio Voice Android SDK 6.9.0          |
| Video Calls            | Twilio Video Android SDK 7.8.0          |
| Networking             | Retrofit 2 + OkHttp                     |
| Push Notifications     | Firebase Cloud Messaging                |
| Real-time              | Pusher                                  |
| Analytics & Crash      | Firebase Analytics + Crashlytics        |
| Security               | AndroidX Security Crypto                |
| Min SDK                | 27                                      |
| Target / Compile SDK   | 35                                      |

## Prerequisites

- Android Studio (Ladybug or newer)
- JDK 11+
- Android SDK 35
- Twilio account (Voice & Video enabled)
- Firebase project (with `google-services.json`)
- Git

## Getting Started

```bash
# Clone the repository
git clone https://github.com/JavisOtieno/Call-Center-System-Android-App.git
cd Call-Center-System-Android-App

# Open in Android Studio
# File → Open → select the project folder

# Place google-services.json in app/
# Configure Twilio credentials / backend URLs as needed

# Sync Gradle and run
# Build → Make Project
# Run on emulator or physical device
