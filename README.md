# Colourful Web Explorer

**Experiment 8: Implement Menus and WebView in an Android application.**

**Student Name:** Harshil Sheth
**USN:** 25MCAR0225

---

## Table of Contents
1. [Experiment Overview](#experiment-overview)
2. [Concept and Technology](#concept-and-technology)
3. [Scenario](#scenario)
4. [Features](#features)
5. [Project Structure](#project-structure)
6. [Getting Started](#getting-started)
7. [Output](#output)
8. [Test Cases](#test-cases)
9. [Known Limitations](#known-limitations)
10. [Learning Outcomes](#learning-outcomes)

---

## Experiment Overview

This experiment builds a small Android web browser that demonstrates three kinds of Android menus and an embedded web view. The app loads web pages inside the app, provides quick-access website chips, and offers color themes that change the toolbar and accent colors. Long-pressing a chip or the page opens a context menu, and the overflow button opens a popup menu.

## Concept and Technology

- **WebView** renders web pages inside the app. JavaScript and DOM storage are enabled, the viewport is set to fit the screen, and the app handles page start, finish, progress, title changes, and load errors through `WebViewClient` and `WebChromeClient`.
- **Options menu** is attached to the toolbar (`main_options_menu.xml`). It contains submenus for Quick Websites and Color Themes, plus page actions (reload, desktop view, copy URL, share URL, add bookmark, clear cache, and about).
- **Context menu** is registered on the WebView and on each quick-link chip (`quick_links_context_menu.xml`). A long press shows "Open Web Link," "Copy Link Address," "Share Link," or "Remove Bookmark."
- **Popup menu** is anchored to the overflow button in the URL bar (`popup_actions_menu.xml`) and offers Go Home, Reload, Desktop View, Share, and Clear Search History.
- **Desktop view** switches the WebView's user-agent string to a desktop browser string and reloads the page.
- **Smart URL handling:** input containing a dot and no spaces is treated as a web address (`https://` is added if missing). Anything else is sent to a Google search.

## Scenario

A user opens the browser, which loads Google by default. From there, the user can:

- Type a URL or search term into the address bar and tap **GO**
- Tap a quick chip (Google, Wikipedia, Android, GitHub, Kotlin, Tech News) to load that site
- Use the toolbar's Quick Websites menu or the overflow popup menu to navigate
- Switch color themes from the Color Themes submenu
- Toggle desktop view, copy or share the current page URL, and clear the cache or search history
- Long-press a chip or the page to open a context menu with link actions

## Features

- In-app WebView with JavaScript and zoom controls
- Address bar with clear button, GO action, and back, forward, and refresh buttons (back and forward dim when unavailable)
- Quick-access chips for six sites
- Options menu with Quick Websites and five color themes (Sunset Glow, Ocean Wave, Neon Cyberpunk, Emerald Garden, Berry Passion)
- Popup menu for common page actions
- Context menu on chips and the WebView
- Desktop view toggle
- Progress bar and page title shown in the toolbar subtitle
- Error screen with a "Try Again" button when a main page fails to load
- Back button navigates the web history before leaving the app
- Share and copy URL using the Android share sheet and clipboard

## Project Structure

```
androidapps_exp8/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/menusdemo/
│   │   │   │   └── MainActivity.kt          # WebView setup, menus, URL handling, themes
│   │   │   ├── res/
│   │   │   │   ├── layout/activity_main.xml # Toolbar, URL bar, quick chips, WebView, error view
│   │   │   │   ├── menu/
│   │   │   │   │   ├── main_options_menu.xml     # Toolbar options menu
│   │   │   │   │   ├── popup_actions_menu.xml    # Overflow popup menu
│   │   │   │   │   └── quick_links_context_menu.xml  # Long-press context menu
│   │   │   │   ├── drawable/                # Toolbar gradients, icons, button shapes
│   │   │   │   ├── values/                  # strings.xml, colors.xml, themes.xml
│   │   │   │   ├── values-night/themes.xml  # Dark theme
│   │   │   │   └── xml/                     # Backup and data extraction rules
│   │   │   └── AndroidManifest.xml          # INTERNET and network state permissions
│   │   ├── test/                # Unit test template
│   │   └── androidTest/         # Instrumented test template
│   └── build.gradle.kts
├── gradle/libs.versions.toml    # Dependency versions
├── build.gradle.kts
├── settings.gradle.kts
└── screenshots/                 # Output and test case images
```

## Getting Started

**Requirements:** Android Studio (latest stable), Android SDK, an emulator or device running Android 7.0 (API 24) or later, and an internet connection.

1. Clone the repository:
   ```bash
   git clone https://github.com/HarshilSheth888/AndroidApps_Exp8.git
   ```
2. Open the project folder in Android Studio.
3. Let Gradle sync.
4. Press **Run** on an emulator or connected device.

## Output



<img width="300" height="652" alt="1000199668" src="https://github.com/user-attachments/assets/21a2faac-0f3e-49f3-afc7-658225938aa4" />




The app opens with the quick-link chips and the address bar at the top and a web page in the main area.

## Test Cases

### Test Case 1: Quick-link chip loads a website
- **Input:** Tap the **📚 Wikipedia** chip.
- **Expected:** Wikipedia loads in the WebView, the address bar shows the Wikipedia URL, and the toolbar subtitle shows "Wikipedia."
- **Result:** Pass
- **Screenshot:** 

<img width="1080" height="2340" alt="1000199670" src="https://github.com/user-attachments/assets/080ca2e8-228a-41bd-aab3-c861cd961d4a" />




### Test Case 2: Options menu navigation
- **Input:** Tap the toolbar's overflow menu, choose **Quick Websites**, then **Android Developers**.
- **Expected:** The Android Developers site loads, and the toolbar subtitle shows the page title.
- **Result:** Pass
- **Screenshot:** 

<img width="1080" height="2340" alt="1000199672" src="https://github.com/user-attachments/assets/8c5cfe4e-a8a8-4e39-94fb-55776f9e950c" />



## Known Limitations

- **Add to Bookmarks** and **Remove Bookmark** show a confirmation message but do not save or delete anything. Bookmarks are not persisted.
- **Quick-link chips** are fixed in the layout and cannot be added or removed.
- **Color themes** reset when the app is closed.

## Learning Outcomes

- Creating options, popup, and context menus in Android
- Responding to menu selections and long presses
- Embedding and controlling a `WebView`, including JavaScript, navigation, and error handling
- Changing the user-agent to switch between mobile and desktop views
- Applying runtime theme changes to toolbar and UI colors
- Handling back-button navigation through web history

## Author

**Harshil Sheth**
USN: 25MCAR0225
GitHub: https://github.com/HarshilSheth888
