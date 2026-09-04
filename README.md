# CyberShield Community

> **An Educational Cyber Safety Ecosystem**

## 🛡️ About the Project
**CyberShield Community** is a native Android application developed as a Final Year B.Tech Major Project. The platform is designed to bridge the gap between cybersecurity awareness and real-world application. It provides an all-in-one ecosystem where users can learn about digital threats, analyze suspicious URLs, generate secure passwords, report cyber incidents, and engage with a community of safety-conscious users.

## 🎯 Problem Statement
As digital transformation accelerates, everyday smartphone users are increasingly targeted by sophisticated cyber threats such as phishing, social engineering, and malicious links. Most users lack accessible cybersecurity education, proactive tools to detect simple scams, and a centralized platform to report incidents and warn their community.

## 💡 Solution
CyberShield Community addresses this vulnerability by combining education, detection, and community reporting into a single Android application. By providing actionable tools (like URL checkers and password analyzers) alongside interactive learning modules, the application empowers users to transition from vulnerable targets to proactive digital defenders.

---

## 🚀 Features & Implementation Status

### Authentication
*   ✅ **Splash Screen:** Routes users dynamically based on Firebase Auth session state.
*   ✅ **Registration:** Secure account creation with full name, email, password, and input validation.
*   ✅ **Login:** Email/password authentication with "Forgot Password" reset functionality.

### Dashboard & Navigation
*   ✅ **Dashboard:** Personalized greeting, recent threat alerts, cyber news, and daily security tips.
*   ✅ **Bottom Navigation:** Seamless routing between Learn, Detect, Protect, Report, and Community modules.

### Learn
*   ✅ **Cybersecurity Topics:** 6 structured learning modules.
*   ✅ **Progress Tracking:** Real-time completion counter synced with Firebase.
*   🔵 **Quizzes & Digital Certificates:** Planned for future enhancement.

### Detect
*   ✅ **URL Checker:** Analyzes and validates inputted URLs for potential risks.
*   🔵 **QR Code / Screenshot / AI Scam Scanner:** Planned for future integration with AI APIs.

### Protect
*   ✅ **Password Analyzer:** Evaluates the strength and complexity of user-provided passwords.
*   ✅ **Password Generator:** Creates highly secure, randomized passwords on demand.
*   🔵 **Password Vault & Emergency SOS:** Planned for future enhancement.

### Report
*   ✅ **Incident Reporting:** Form-based submission allowing users to categorize and describe cyber incidents.
*   ✅ **Report History:** Users can view a log of their previously submitted reports retrieved from Firebase.

### Community
*   ✅ **Shared Feed:** A live community board where users can create posts.
*   ✅ **Categorization & Engagement:** Support for category tags and Like/Unlike functionality.

### Profile
*   ✅ **User Stats:** Displays name, email, completed topics, submitted reports, community posts, and current Security Score.
*   ✅ **Session Management:** Secure logout functionality.

---

## 🧠 Security Score

The **Dashboard Security Score** is a dynamic, gamified metric representing a user's cybersecurity awareness and platform engagement. 

**Calculation Logic:**
The score is calculated locally based on the user's authenticated activities stored in the Firebase Realtime Database. It dynamically scales based on:
1.  **Completed Learning Topics** (Positive weight per topic finished)
2.  **Submitted Reports** (Positive weight per community incident reported)

*(Note: The exact integer weights are defined dynamically in the Java logic to cap at a maximum score of 100).*

---

## 🔄 User Journey

```text
[ Install App ]
      ↓
[ Register / Login via Firebase Auth ]
      ↓
[ Dashboard ] (Views Security Score & News)
      ↓
[ Choose Module ]
  ├── 📖 Learn (Read topics -> Increases Score)
  ├── 🔍 Detect (Scan URLs for safety)
  ├── 🔐 Protect (Analyze/Generate Passwords)
  ├── 📝 Report (Log an incident -> Increases Score)
  └── 🌐 Community (Share and read threat posts)
      ↓
[ Profile Updates ] (Score increases, History saved)
      ↓
[ Cyber-Aware User ]

//PROJECT STRUCTURE
CyberShield-Community/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cybershield/
│   │   │   │   ├── activities/     # Splash, Login, Register, MainActivity
│   │   │   │   ├── fragments/      # Learn, Detect, Protect, Report, Community, Profile
│   │   │   │   ├── models/         # User, Post, Report data classes
│   │   │   │   └── adapters/       # RecyclerView adapters for Community
│   │   │   └── res/
│   │   │       ├── layout/         # XML UI files
│   │   │       ├── menu/           # Bottom navigation menu
│   │   │       └── values/         # Colors, strings, themes
│   └── build.gradle                # App-level dependencies
│
├── build.gradle                    # Project-level dependencies
├── README.md
└── .gitignore

Disclaimer
CyberShield Community is developed strictly as an educational project for a B.Tech university curriculum.
The tools provided (such as the URL checker and password analyzer) rely on basic validation rules and should not be treated as guaranteed, professional-grade security analysis.
 Always use standard security software and common sense when navigating the web.
