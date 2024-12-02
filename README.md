# ArtChart


<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#through-android-studio">Through Android Studio</a></li>
        <li><a href="#through-apk">Through APK</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributors">Contributors</a></li>
    <li>
      <a href="#acknowledgments">Acknowledgments</a>
    </li>
  </ol>
</details>

## About The Project

<img src="art_chart_app_icon.png" alt="ArtChart logo" width="200" style="margin-bottom: 10px;" />
<br>
ArtChart is an online art database where you can find public art close to you, review art, and submit new public art pieces to the app.

### Built With
[![Kotlin][Kotlin.com]][Kotlin-url]


## Getting Started

### Through Android Studio

#### Prerequisites

* Android Studio IDE is installed
* The project is cloned or forked

#### Run

1. Open ArtChart in Android Studio
2. Connect your Android device via USB
3. Launch the app using Shift+F10. Alternatively, run from command line:
   ```sh
   ./gradlew installDebug; adb shell am start -n artchart/artchart.ui.MainActivity 
   ```

### Through APK
Download the APK file from <a href="https://qq-zhong.github.io/artChart_site/">our website</a> directly to your android device and run.

## Usage

* Home Page: View art near you on the map.
* Search Page: Search and filter all art.
* View art details by clicking an art piece from the Home or Search pages.
* Write reviews for art and see reviews from other users.
* Add Art Page: Submit new public art pieces on the app.
* Profile Page: Sign in and configure your account.
* Submit art pieces to ArtChart with precise map location.

## Contributors

* Alicia LeClercq
* Ana Premovic
* Nicholas Turenne
* Peter Zhong


## Acknowledgments

* <a href="https://github.com/othneildrew/Best-README-Template">README Template</a>

[Kotlin.com]: https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org/
