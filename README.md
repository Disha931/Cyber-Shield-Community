# 🛡️ CyberShield Community

**Learn • Detect • Protect • Report**

An Android application built to make cybersecurity awareness accessible, practical, and community-driven. CyberShield Community combines real-time threat detection tools, hands-on security utilities, and a shared reporting network into a single app — helping users build safer digital habits and respond quickly when something goes wrong.

---

## 📱 About the Project

Cybercrime awareness is often scattered — one app for password checking, another for scam reporting, another for education. CyberShield Community brings these into one platform, structured around four core pillars:

- **Learn** — build cyber-hygiene knowledge through structured awareness topics
- **Detect** — analyze links, QR codes, messages, and screenshots for scam indicators
- **Protect** — strengthen and manage your digital identity
- **Report** — file cyber incidents and see community-wide threat trends

This was built as a B.Tech Major Project, with every feature backed by real functionality — not static mockups.

---

## ✨ Features

### 🎓 Learn
- 6 structured cybersecurity awareness topics (Phishing, Passwords, Privacy, Malware, Web Security, and more)
- Persistent progress tracking synced to Firebase
- **Digital Certificate** awarded on completing all topics

### 🔍 Detect
- **URL Checker** — heuristic risk analysis (HTTPS check, IP-based URLs, shorteners, suspicious TLDs, phishing patterns)
- **QR Code Scanner** — real-time camera scanning (ZXing) with built-in link safety verdict
- **AI Scam Detector** — paste any suspicious message for pattern-based scam analysis
- **SMS Phishing Detector** — scans the device inbox for phishing indicators
- **Screenshot Scanner** — on-device OCR (ML Kit) extracts text from images and runs the same scam analysis
- **Threat Heatmap** — live, community-wide breakdown of reported incident types

### 🔒 Protect
- **Password Analyzer** — real-time strength scoring with actionable suggestions
- **Password Generator** — customizable length and character sets, powered by `SecureRandom`
- **Password Vault** — encrypted, on-device storage (`EncryptedSharedPreferences`) — vault data never touches the cloud
- **Emergency SOS** — one-tap access to India's Cyber Crime Helpline (1930), the official reporting portal, and a saved personal emergency contact

### 📢 Report
- Guided incident reporting form with type + description
- Personal report history with status tracking
- Anonymized data feeds the community Threat Heatmap

### 👥 Community
- Live, shared discussion feed (real-time Firebase sync)
- Categorized posts (Scam Alert, Question, Tip)
- Like/unlike with per-user tracking

### 👤 Profile & Dashboard
- Firebase-authenticated user profile with live activity stats
- Dynamic Security Score, calculated from real Learn + Report activity
- Dynamic Cyber News feed (admin-managed via Firebase Console)
- Secure logout with full session/back-stack clearing

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI | XML, Material Components |
| Architecture | Single-Activity + Fragments |
| Auth & Database | Firebase Authentication, Firebase Realtime Database |
| Local Secure Storage | Jetpack Security (`EncryptedSharedPreferences`) |
| QR Scanning | ZXing (`zxing-android-embedded`) |
| OCR / Text Recognition | Google ML Kit Text Recognition |
| Design | Custom dark cybersecurity theme, Material CardViews |

---

## 🏗️ Architecture

The app follows a **single-Activity, multi-Fragment** architecture:

```
Dashboard (Activity)
 ├── fragment_learn
 ├── fragment_Detect
 │     ├── Fragment_UrlChecker
 │     ├── Fragment_QrScanner
 │     ├── Fragment_ScamDetector
 │     ├── Fragment_SmsDetector
 │     ├── Fragment_ScreenshotScanner
 │     └── Fragment_ThreatHeatmap
 ├── Fragment_Protect
 │     ├── Fragment_PasswordAnalyzer
 │     ├── Fragment_PasswordGenerator
 │     ├── Fragment_PasswordVault
 │     └── Fragment_EmergencySos
 ├── fragment_Report
 ├── Fragment_Community
 └── fragment_Profile
       └── Fragment_Certificate
```

All fragments swap into a shared `FrameLayout` container, with the bottom navigation and top bar persisting across screens — avoiding full-screen transitions for every action.

### Firebase Data Structure

```
Users/
 └── {uid}/
      ├── name, email
      ├── learningProgress/ {topicKey: true/false}
      ├── myReports/ {autoId: {type, description, status, timestamp}}
      └── certificate/ {earned, dateEarned}

CommunityPosts/
 └── {autoId}/ {authorUid, authorName, category, text, timestamp, likeCount, likedBy}

ThreatReports/          (anonymized — no user ID, no description)
 └── {autoId}/ {type, timestamp}

CyberNews/               (admin-managed via Firebase Console)
 └── {autoId}/ {title, description, date}
```

---

## 🔐 Security & Privacy Notes

- **Password Vault data is never sent to Firebase** — it's encrypted and stored only on-device via the Android Keystore, specifically because it holds the user's actual passwords to other accounts.
- **Threat Heatmap and Community data are intentionally anonymized/public** — no personal report descriptions or user identifiers are exposed in shared nodes.
- SMS and camera permissions are requested at runtime and only used for their stated feature (SMS Phishing Detector, QR Scanner) — no background access.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project with:
  - Authentication (Email/Password) enabled
  - Realtime Database enabled
- Your own `google-services.json` placed in `app/`

### Setup
1. Clone the repository
   ```bash
   git clone https://github.com/<Disha931>/cybershield-community.git
   ```
2. Open in Android Studio
3. Add your `google-services.json` to the `app/` directory
4. Sync Gradle
5. Run on an emulator or physical device (a real device is recommended for QR Scanning, SMS Detection, and Screenshot Scanner testing)

---

## 📸 Screenshots

*(Add your app screenshots here — Dashboard, Learn, Detect tools, Protect tools, Community, Profile)*

---

## 🗺️ Roadmap

- [ ] Leaderboard (rank users by Security Score)
- [ ] Dynamic Recent Threat Alerts on Dashboard
- [ ] Push Notifications
- [ ] Full comment threads on Community posts

---


---

## 🙋 Author

Built by **[Disha Tomar]** as a B.Tech Major Project.

*Learn. Detect. Protect. Report.*
