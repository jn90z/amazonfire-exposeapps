# Amazon Fire Expose Apps

Small open-source Android launcher for Amazon Fire tablets, designed to let a parent open an already installed app (initially **Daylio**) from an Amazon Kids profile through a simple interface.

## Why a replacement instead of patching AppStart?

The original AppStart APK was signed by another developer. Patching it and resigning it would conflict with the installed signature. This application has a unique package ID (`com.jn90z.fireexposeapps`), so AppStart can remain installed while this is tested. This repo contains **new code**, not proprietary AppStart binaries.

## Features

- One-tap **Open Daylio** using package `net.daylio` with direct-activity fallback
- Optional list of launchable apps available to the current Android user
- `QUERY_ALL_PACKAGES` declaration to support discovering installed apps on Android 11+
- No network permissions, root, file access, ads, telemetry, or install/uninstall operations
- Doesn't change parental controls or enable applications for another user

## Build

GitHub **Actions → Build Android APK → Run workflow**, or push changes. Download the `amazonfire-exposeapps-debug` artifact and extract `app-debug.apk`.

For a local build with JDK 17, Android SDK 35 and Gradle 8.9:

```sh
gradle :app:assembleDebug
```

## Install and test (PowerShell with adb)

Make sure the debug APK is unzipped into the current directory, then:

```powershell
.\adb install .\app-debug.apk
.\adb shell cmd package install-existing --user 10 com.jn90z.fireexposeapps
.\adb shell pm list packages --user 10 -e | findstr fireexposeapps
.\adb shell am start --user 10 -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p com.jn90z.fireexposeapps
```

Replace user `10` with the correct child user ID from `adb shell pm list users`. Installing and launching via ADB **does not guarantee the icon is exposed on the Amazon Kids home screen**. Check the parent's **Add Content** menu. If this app is not shareable there, another parent-authorized entry point is required. Do not remove the original AppStart until the new app is accessible without ADB.

## Limitations

Fire OS/Amazon Kids may restrict what a child can see or launch irrespective of Android package visibility. The complete list of apps is not a guarantee of Amazon Kids authorization. Use this only for apps the parent has approved. Debug builds are for testing; keep the signing key stable for future upgrades and do not publish private keystores to GitHub.
