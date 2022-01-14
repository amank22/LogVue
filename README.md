<!-- LOGO -->
<br />
<h1>
<p align="center">
<img src="https://raw.githubusercontent.com/amank22/LogVue/main/logo_land_full.png" alt="Logo">
</p>
</h1>
<p align="center">
    Monitor, analyse, export local analytics from Android device and use SQL-Like query to filter logs on desktop!
    <br />
</p>
<hr>

<p align="center">
    <a href="https://github.com/JetBrains/compose-jb"><img alt="compose" src="https://img.shields.io/badge/Made%20with-Compose--Jb-blueviolet?logo=data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSItMC43MDkgLTExLjU1NSAxNDEuNzMyIDE0MS43MzIiIHhtbDpzcGFjZT0icHJlc2VydmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTE0MC4zMTQgMzcuNjU0QzE0MC4zMTQgMTYuODU4IDEyMy40MDIgMCAxMDIuNTM3IDBjLTEzLjc0NCAwLTI1Ljc3IDcuMzE3LTMyLjM3OSAxOC4yNTVDNjMuNTQ5IDcuMzE3IDUxLjUyMSAwIDM3Ljc3NyAwIDE2LjkxMiAwIDAgMTYuODU4IDAgMzcuNjU0YzAgMTAuODIxIDQuNTg4IDIwLjU3IDExLjkyMiAyNy40MzhoLS4wMWw1NC4wODQgNTEuNTg0YTUuMzg5IDUuMzg5IDAgMCAwIDguMDY4LjI0OWw1NC4zNDYtNTEuODMzaC0uMDE2YzcuMzM1LTYuODY3IDExLjkyLTE2LjYxNiAxMS45Mi0yNy40MzgiIGZpbGw9IiNlZDcxNzEiIGNsYXNzPSJmaWxsLTAwMDAwMCI+PC9wYXRoPjwvc3ZnPg=="/></a>
    <a href="https://github.com/amank22/LogVue/releases/latest"><img alt="release" src="https://img.shields.io/github/v/release/amank22/logvue?color=brightgreen&label=latest%20release"/></a>
    <a href="https://github.com/amank22/LogVue/issues"><img alt="issues" src="https://img.shields.io/github/issues/amank22/LogVue"/></a>
    <a href="https://github.com/amank22/LogVue/blob/main/LICENSE"><img alt="License" src="https://img.shields.io/github/license/amank22/logvue"/></a>
    <a href="#"><img alt="License" src="https://img.shields.io/badge/platform-windows%20%7C%20mac%20%7C%20linux-blue?cacheSeconds=maxAge"/></a>
</p>

## Features

- Native desktop apps to view analytics
- Capture analytics directly from connected device
- Create multiple sessions for different features or apps
- Filter logs using SQL query like `where eventName = 'home'`
- Filter nested objects like `where event.user.name = 'Aman'`
- Export logs in json or pretty yaml format or copy single log
- Basic classification of common types of events like load, view, search, click etc.
- Enable dark mode for night

## Supported Analytics

- [Firebase](https://firebase.google.com/docs/analytics)
- [More to come](#future-goals)

## Prerequisites

- Install [ADB tools](https://www.xda-developers.com/install-adb-windows-macos-linux/) on your system

## Install

- Download installer package from [Latest Release](https://github.com/amank22/LogVue/releases/latest) for your
  respective OS.
- If there is any issue with the installer package, download and run the jar file with `jar -jar filename.jar`

## Basic operations

### Filtering analytics

- Use SQL query to filter your data.
- Not all commands can be used but should be sufficient for this app use-case.

### Exporting analytics

- You can export session data using the export button in the UI.
- Following formats are provided:
  - Json with pretty print
  - Compact Json
  - Plain Yaml
- Copy single analytics data in details section

## Contribute

Do you see any improvements or want to implement a missing feature? Contributions are very welcome!

- Is your contribution relatively small? Make your changes, run the code checks, open a PR and make sure the CI is
  green!
- Are the changes big and do they make a lot of impact? Please open an
  issue [here](https://github.com/amank22/LogVue/issues?q=is%3Aissue) or reach out and let's discuss.

Take into account that changes and requests can be rejected if they don't align with the **purpose of the application**.
To not waste any time you can always open an issue [here](https://github.com/amank22/LogVue/issues?q=is%3Aissue) to talk
before you start making any changes.

## Report an issue

- Did you find an issue and want to fix it yourself? See [Contribute](#contribute) for more information
- Want to report an issue? You can do that [here](https://github.com/amank22/LogVue/issues?q=is%3Aissue). By adding as
  much details when reporting the issue and steps to reproduce you improve the change it will be solved quickly.

## Future goals

- [ ] Plugin system to support more logs and allows for in-house
      customisations [#27](https://github.com/amank22/LogVue/issues/27)
- [ ] Import event logs directly [#28](https://github.com/amank22/LogVue/issues/28)
- [ ] A framework to validate logs directly with some set of rules directly in GUI
- [ ] Create [feature request](https://github.com/amank22/LogVue/issues/new) and we can discuss
