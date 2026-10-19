# Selenium Test Engine

Automated UI tests for [Sauce Demo](https://www.saucedemo.com), a practice e-commerce site, written in Java with
Selenium 4 and JUnit. The project has two parts:

1. **Page-object test suite**: JUnit tests for login, product sorting, the cart and checkout *(in progress)*
2. **Keyword-driven engine**: test cases written as JSON steps, run through Selenium as JUnit tests *(planned)*

## Requirements

- JDK 21 or newer
- Chrome, Edge or Firefox. Selenium Manager downloads the matching driver (and Chrome, if it's missing) on first run.

Maven doesn't need to be installed. The Maven wrapper (`mvnw`) downloads it.

## Running the tests

```sh
./mvnw test                                  # headless Chrome
./mvnw test -Dbrowser=edge                   # chrome | edge | firefox
./mvnw test -Dheadless=false                 # watch the browser
./mvnw test -Dtest=SmokeTest                 # one test class
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

To run against a Selenium Grid or container instead of a local browser, set `SELENIUM_REMOTE_URL`
(for example `http://localhost:4444`).

### Running against a local copy of the site

Sauce Demo is open source ([saucelabs/sample-app-web](https://github.com/saucelabs/sample-app-web), MIT), so the
suite can run against a local copy instead of the public site:

```sh
git clone https://github.com/saucelabs/sample-app-web.git
cd sample-app-web
npm ci --ignore-scripts
npx vite                                     # serves http://localhost:3000
```

Then point the tests at it:

```sh
./mvnw test -DbaseUrl=http://localhost:3000
```

## Project layout

```
src/test/java/io/github/jroberts0907/engine/
├── SmokeTest.java                # browser starts and the site is up
├── LoginTest.java                # valid login and rejected logins
├── pages/                        # page objects: locators and actions, no assertions
│   ├── BasePage.java             # explicit-wait helpers shared by every page
│   ├── LoginPage.java
│   └── InventoryPage.java
└── support/
    ├── BaseTest.java             # fresh browser per test
    ├── ScreenshotOnFailure.java  # saves target/screenshots/<Test>_<name>.png on failure
    ├── TestUsers.java            # saucedemo.com test accounts
    ├── Config.java               # run settings from -D system properties
    └── DriverFactory.java        # local or remote WebDriver for the chosen browser
```

Tests only talk to page objects, never to locators, so a change to the site means fixing one page class rather
than every test. Waits are explicit (`WebDriverWait`); there is no `Thread.sleep` anywhere.
