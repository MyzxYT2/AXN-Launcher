# Kurse Launcher — Android starter project

A dark violet, Kurse-branded Android home-screen launcher concept for the Samsung Galaxy S23/S23 Ultra.

## Included
- Selectable Android Home app.
- Clock/date header, violet styling, app search, simple All/Games/Social/Tools filters.
- Installed-app grid using each app's actual icon; tap to launch and long-press for Open/App info.
- Quick dock for Phone, Messages, Camera and Settings (uses common Samsung/Google package names where available).
- Kurse-themed wallpaper asset and a small control panel.
- GitHub Actions workflow to build a debug APK in the cloud.

## Build an APK with GitHub (no Android Studio needed)
1. Create a GitHub repository and upload the **contents** of this project folder (not the ZIP itself).
2. Make sure `.github/workflows/android.yml` is included at the repository root.
3. Open the repository's **Actions** tab. If prompted, enable Actions for the repository.
4. Select **Build Kurse Launcher APK** on the left, then tap **Run workflow** and confirm.
5. Wait for the run to finish with a green check mark. Open the completed run and find **Artifacts** near the bottom.
6. Download `Kurse-Launcher-debug-APK`, extract the downloaded artifact ZIP, and copy `app-debug.apk` to your Galaxy S23.
7. Open the APK on the phone and allow installation from that source if Android asks. Then select it under Settings > Apps > Choose default apps > Home app.

The workflow also runs automatically whenever you push a commit to the `main` branch. GitHub Actions usage and availability depend on your GitHub account and repository settings.

## Local build
Open this folder in Android Studio, let Gradle sync, and select Build > Build Bundle(s) / APK(s) > Build APK(s).

## Notes
This is starter source, not a prebuilt or device-tested APK. Android and Samsung system behavior can vary by One UI version. Some dock apps may use different package names on a particular device. It is not yet feature-equivalent to Nova/Smart Launcher: advanced gestures, resizable widgets, icon-pack support, backup/restore, adaptive layouts, and full category automation are not implemented.
