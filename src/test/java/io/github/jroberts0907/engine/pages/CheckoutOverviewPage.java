package io.github.jroberts0907.engine.pages;

import java.math.BigDecimal;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Checkout step two: the order summary with item total, tax and total. */
public class CheckoutOverviewPage extends BasePage {

    private static final By FINISH_BUTTON = By.cssSelector("[data-test='finish']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SUBTOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL = By.cssSelector("[data-test='total-label']");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutOverviewPage waitUntilLoaded() {
        visible(FINISH_BUTTON);
        return this;
    }

    public List<BigDecimal> itemPrices() {
        return textsOf(ITEM_PRICES).stream().map(CheckoutOverviewPage::amount).toList();
    }

    /** "Item total: $39.98" */
    public BigDecimal itemTotal() {
        return amount(textOf(SUBTOTAL));
    }

    /** "Tax: $3.20" */
    public BigDecimal tax() {
        return amount(textOf(TAX));
    }

    /** "Total: $43.18" */
    public BigDecimal total() {
        return amount(textOf(TOTAL));
    }

    public CheckoutCompletePage finish() {
        click(FINISH_BUTTON);
        return new CheckoutCompletePage(driver).waitUntilLoaded();
    }

    // BigDecimal, not double, so money adds up exactly (0.1 + 0.2 is not 0.3 in double)
    private static BigDecimal amount(String label) {
        return new BigDecimal(label.substring(label.indexOf('$') + 1));
    }
}
