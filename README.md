# Selenium Test Engine

Automated UI tests for [Sauce Demo](https://www.saucedemo.com), a practice e-commerce site, written in Java with
Selenium 4 and JUnit. The project has two parts:

1. **Page-object test suite**: 17 JUnit tests covering login, product sorting, the cart and checkout
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
./mvnw test -DtestUser=problem_user          # sign in as a different test account
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

## Known defects

Sauce Demo ships test accounts with bugs built in. Running the suite as each of them
(`./mvnw test -DtestUser=problem_user`) found the defects below. `KnownDefectsTest` asserts the correct behavior
for each one, so those tests fail until the bug is fixed. They're tagged `known-defect` and left out of the default
run so the main suite stays green; run them on their own with:

```sh
./mvnw test -Pknown-defects
```

| Account | Defect | What the test sees | Caught by |
|---|---|---|---|
| `problem_user` | Sorting does nothing | Prices stay in default order after choosing "Price (low to high)" | `ProblemUser.sortingWorks`, `InventoryTest` |
| `problem_user` | Half the products can't be added to the cart | Adding all 6 products leaves the badge at 3 | `ProblemUser.everyProductCanBeAdded` |
| `problem_user` | Every product shows the same broken image | All 6 image URLs are `sl-404.jpg` | `ProblemUser.productImagesAreDistinct` |
| `problem_user` | Typing a last name overwrites the first name instead | Checkout never gets past step one; the wrong field errors are shown | `ProblemUser.checkoutAcceptsValidDetails`, `CheckoutTest` |
| `error_user` | Sorting fails with an alert | "Sorting is broken! This error has been reported to Backtrace." | `ErrorUser.sortingWorks`, `InventoryTest` |
| `error_user` | Half the products can't be added to the cart | Adding all 6 products leaves the badge at 3 | `ErrorUser.everyProductCanBeAdded` |
| `error_user` | A missing last name isn't rejected | Checkout continues with no error | `ErrorUser.missingLastNameIsRejected`, `CheckoutTest` |
| `error_user` | Finish doesn't complete the order | The confirmation page never appears | `ErrorUser.finishCompletesOrder`, `CheckoutTest` |
| `visual_user` | Prices on the product list are wrong | Backpack shows a random price instead of $29.99 | `VisualUser.pricesMatchCatalog`, `InventoryTest` |
| `performance_glitch_user` | Login is slow | About 6 s to reach the product list (budget: 3 s) | `PerformanceGlitchUser.loginIsFast` |

`performance_glitch_user` passes the rest of the suite: the explicit waits (10 s) absorb the delay, which is why
the slow login needs its own timing check. `visual_user` also has layout bugs (a misplaced cart icon, for one) that
a functional test can't see; catching those takes visual regression testing.

## Project layout

```
src/test/java/io/github/jroberts0907/engine/
├── SmokeTest.java                # browser starts and the site is up
├── LoginTest.java                # valid login and rejected logins
├── InventoryTest.java            # sorting and the cart badge
├── CartTest.java                 # adding, removing, continue shopping
├── CheckoutTest.java             # full purchase, totals, form validation
├── KnownDefectsTest.java         # one test per bug in the buggy test accounts (tag: known-defect)
├── pages/                        # page objects: locators and actions, no assertions
│   ├── BasePage.java             # explicit-wait helpers and the shared header
│   ├── LoginPage.java
│   ├── InventoryPage.java
│   ├── CartPage.java
│   ├── CheckoutInfoPage.java
│   ├── CheckoutOverviewPage.java
│   └── CheckoutCompletePage.java
└── support/
    ├── BaseTest.java             # fresh browser per test
    ├── ScreenshotOnFailure.java  # saves target/screenshots/<Test>_<name>.png on failure
    ├── TestUsers.java            # saucedemo.com test accounts
    ├── Config.java               # run settings from -D system properties
    └── DriverFactory.java        # local or remote WebDriver for the chosen browser
```

Tests only talk to page objects, never to locators, so a change to the site means fixing one page class rather
than every test. Waits are explicit (`WebDriverWait`); there is no `Thread.sleep` anywhere.
