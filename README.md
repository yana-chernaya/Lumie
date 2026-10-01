[Русский](./README.ru.md)

# Lumie 
**Lumie** is an Android app designed to support mental health, boost motivation, and train positive thinking.

The app generates personalized affirmations with beautiful dynamic backgrounds, supports background execution, notifies users about new affirmations via local notifications, and allows users to save their favorite cards.

## Demo
<table>
  <tr>
    <td align="center"><b>Content & Network</b></td>
    <td align="center"><b>Feed & Custom Gestures</b></td>
  </tr>
  <tr>
    <td align="center" width="250">
      <video src="https://github.com/user-attachments/assets/da45e946-3472-41a7-b325-d2bd1fac2d8a" width="250"></video>
    </td>
    <td align="center" width="250">
      <video src="https://github.com/user-attachments/assets/9c5b62ce-4d59-43d3-b1df-f82fd78e50f9" width=250"></video>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td align="center"><b>Favorites</b></td>
    <td align="center"><b>Settings & UI States</b></td>
  </tr>
  <tr>
    <td align="center" width="250">
      <video src="https://github.com/user-attachments/assets/83fc1e82-ba77-4e18-9c24-fc4e343b871e" width="250"></video>
    </td>
    <td align="center" width="250">
      <video src="https://github.com/user-attachments/assets/de10a423-4494-4fa8-ba1a-1fc161e4e0bc" width=250"></video>
    </td>
  </tr>
</table>

## Key Features
* **Smart content generation:** loading affirmations via AI and fetching thematic backgrounds using the Unsplash API, with a reliable fallback to local data (JSON + drawable).
* **Interactive feed:** the main screen features an affirmation feed using VerticalPager – one affirmation per screen. Tapping the 🏠 button in the BottomBar again automatically scrolls the feed back to the top.
* **Favorites:** adding to “Favorites” is available both by clicking the ❤️ icon and double-tapping the screen, which triggers a floating heart animation. Category filtering is available. Tapping the ⭐ button in the BottomBar again automatically scrolls the feed to the top.
* **Background customization:** the ability to change the background of a specific affirmation or set a persistent static/dynamic background from the settings.
* **Export and sharing:** save an affirmation (text + background) as a single image to the gallery or share it with other apps.
* **Background work:** automatic background refresh of affirmations with configurable intervals (6/12/24 hours) and smart Wi‑Fi‑only loading (via WorkManager).
* **Notifications:** local notifications when a new affirmation is automatically loaded.
* **Flexible settings:** choose category, theme (Light/Dark/System), refresh interval, Wi‑Fi‑only mode, etc.
* **Animations:** Lottie animations for error and empty states.
* **First launch:** to avoid an empty screen on first open, the app automatically loads one initial affirmation.

## Data Loading Logic
The app is designed so that the user is never left without content. The process of assembling an Affirmation entity looks like this:

                          Affirmation text request (AI API)
                                        │
               ┌────────────────────────┴────────────────────────┐
            Success                                            Error
               │                                                 │ 
               ▼                                                 ▼
     AI-generated text and                        Random text from JSON (Assets) and
      tags for background                               default category tags
               │                                                 │
               └────────────────────────┬────────────────────────┘
                                        ▼
                          Background request (Unsplash API) 
                                        │
               ┌────────────────────────┴────────────────────────┐
            Success                                            Error
               │                                                 │ 
               ▼                                                 ▼
     Image URL from Unsplash                       Random background from Drawable 
               │                                                 │
               └────────────────────────┬────────────────────────┘
                                        ▼
                       Entity assembly -> Save to Room DB

## Tech Stack
| Category | Library / Tool |
|:----------|:-----------|
| UI | Jetpack Compose (Material 3), Lottie |
| Architectural Pattern | Clean Architecture, MVVM |
| Navigation | Navigation Compose | 
| DI | Hilt | 
| Asynchronous Programming | Kotlin Coroutines, Flow | 
| Networking | Retrofit, OkHttp, Logging Interceptor | 
| Serialization | Kotlinx Serialization | 
| Local Database | Room | 
| Preferences Storage | DataStore Preferences | 
| Background Tasks | WorkManager | 
| Image Loading | Coil 3 | 
| Splash Screen | Core SplashScreen |

## Project Roadmap 
* [ ] **Storage Optimization:** migrate from storing system drawable IDs as strings to file name–based strings (e.g. "bg_city_lights") with UI mapping to the actual `R.drawable.*`. This will eliminate ID instability.
* [ ] **Data Management:** add the ability to delete selected affirmations from the local database.
* [ ] **Room DB:** implement database migration.
* [ ] **UX/UI:** add onboarding screens to introduce key features on the first launch.
* [ ] **System Integration:** develop a widget.
* [ ] **New Features:** add music for meditation and breathing exercises with step‑by‑step phase changes (inhale / hold / exhale).

## APK Download
Requirement: minimum API 26 (Android 8.0 Oreo)

[![Release](https://img.shields.io/github/v/release/yana-chernaya/Lumie)](https://github.com/yana-chernaya/Lumie/releases/latest)

## Author
Yana Chernaya – [**GitHub profile**](https://github.com/yana-chernaya)

***

Made with ❤️ for an Android developer portfolio
