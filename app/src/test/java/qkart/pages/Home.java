package qkart.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Home {
    RemoteWebDriver driver;
    public String url = "https://crio-qkart-frontend-qa.vercel.app";

    public Home(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public void navigateToHome() {
        if (!this.driver.getCurrentUrl().equals(this.url)) {
            this.driver.get(this.url);
        }
    }

    public Boolean PerformLogout() throws InterruptedException {
        try {
            // Find and click on the Logout Button
            WebElement logout_button = driver.findElement(By.className("MuiButton-text"));
            logout_button.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.invisibilityOfElementWithText(By.className("css-1urhf6j"), "Logout"));

            return true;
        } catch (Exception e) {
            // Error while logout
            return false;
        }
    }

    /*
     * Returns Boolean if searching for the given product name occurs without any
     * errors
     */
    public Boolean searchForProduct(String product) {
        try {
            // Clear the contents of the search box and Enter the product name in the search
            // box
            WebElement searchBox = driver.findElement(By.xpath("//input[@name='search'][1]"));
            searchBox.clear();
            searchBox.sendKeys(product);

            //WebElement element = driver.findElement(By.xpath("//p[contains(@class,'css-yg30e6')]"));
            //Thread.sleep(3000);

            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(30));
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.className("css-yg30e6")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[normalize-space()='No products found']"))));
            Thread.sleep(3000);
            return true;
        } catch (Exception e) {
            System.out.println("Error while searching for a product: " + e.getMessage());
            return false;
        }
    }

    /*
     * Returns Array of Web Elements that are search results and return the same
     */
    public List<WebElement> getSearchResults() {
        List<WebElement> searchResults = new ArrayList<WebElement>() {
        };
        try {
            // Find all webelements corresponding to the card content section of each of
            // search results
            searchResults = driver.findElements(By.className("css-1qw96cp"));
            return searchResults;
        } catch (Exception e) {
            System.out.println("There were no search results: " + e.getMessage());
            return searchResults;

        }
    }

    /*
     * Returns Boolean based on if the "No products found" text is displayed
     */
    public Boolean isNoResultFound() {
        Boolean status = false;
        try {
            status = driver.findElement(By.xpath("//h4[text()=' No products found ']")).isDisplayed();
            return status;
        } catch (Exception e) {
            return status;
        }
    }

    /*
     * Return Boolean if add product to cart is successful
     */
    public Boolean addProductToCart(String productName) {
        try {
            /*
             * Iterate through each product on the page to find the WebElement corresponding
             * to the matching productName
             *
             * Click on the "ADD TO CART" button for that element
             *
             * Return true if these operations succeeds
             */
            List<WebElement> gridContent = driver.findElements(By.className("css-sycj1h"));
            for (WebElement cell : gridContent) {
                if (productName.contains(cell.findElement(By.className("css-yg30e6")).getText())) {
                    cell.findElement(By.tagName("button")).click();

                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            String.format("//*[@class='MuiBox-root css-1gjj37g']/div[1][normalize-space()='%s']", productName))));
                    return true;
                }
            }
            System.out.println("Unable to find the given product");
            return false;
        } catch (Exception e) {
            System.out.println("Exception while performing add to cart: " + e.getMessage());
            return false;
        }
    }

    /*
     * Return Boolean denoting the status of clicking on the checkout button
     */
    public Boolean clickCheckout() {
        Boolean status = false;
        try {
            // Find and click on the the Checkout button
            WebElement checkoutBtn = driver.findElement(By.className("checkout-btn"));
            checkoutBtn.click();

            status = true;
            return status;
        } catch (Exception e) {
            System.out.println("Exception while clicking on Checkout: " + e.getMessage());
            return status;
        }
    }

    /*
     * Return Boolean denoting the status of change quantity of product in cart
     * operation
     */
    public Boolean changeProductQuantityinCart(String productName, int quantity) {
        try {
            // TODO: CRIO_TASK_MODULE_TEST_AUTOMATION - TEST CASE 06: MILESTONE 5

            // Find the item on the cart with the matching productName
            WebElement productNameElement = driver.findElement(By.xpath("//div[normalize-space()='"+productName+"']"));

            WebElement currentProductQuantity =
                    productNameElement.findElement(By.xpath(".//following-sibling::div/div/div"));

            WebElement increaseQuantity = productNameElement.findElement(By.xpath(".//following-sibling::div/div/button[2]"));

            WebElement decreaseQuantity = productNameElement.findElement(By.xpath(".//following-sibling::div/div/button[1]"));

            // Increment or decrement the quantity of the matching product until the current
            // quantity is reached (Note: Keep a look out when then input quantity is 0,
            // here we need to remove the item completely from the cart)

            //Following code is added to get current product quantity

            String quantityText = currentProductQuantity.getText();
            int initialProductCount = Integer.parseInt(quantityText);
            //System.out.println(initialProductCount);

            if ((productNameElement.getText()).equals(productName)){
                while (initialProductCount != quantity) {
                    if (initialProductCount < quantity) {
                        Thread.sleep(2000);
                        increaseQuantity.click();
                        initialProductCount++;
                    } else {
                        decreaseQuantity.click();
                        initialProductCount--;
                        //System.out.println(initialProductCount);
                    }
                }
            }


            return false;
        } catch (Exception e) {
            if (quantity == 0)
                return true;
            System.out.println("exception occurred when updating cart: " + e.getMessage());
            return false;
        }
    }

    /*
     * Return Boolean denoting if the cart contains items as expected
     */
    public Boolean verifyCartContents(List<String> expectedCartContents) {
        try {
            List<WebElement> cartContents = driver.findElements(By.xpath(
                    "//div[@class='MuiGrid-root MuiGrid-item MuiGrid-grid-xs-12 MuiGrid-grid-md-3 css-1q5pok1']//div[@class='MuiBox-root css-1gjj37g']/div[not(@class)]"));
            ArrayList<String> actualCartContents = new ArrayList<String>() {

            };
            for (WebElement cartItem : cartContents) {
                actualCartContents.add(cartItem.getText());
            }

            for (String expected : expectedCartContents) {
                // To trim as getText() trims cart item title
                if (!actualCartContents.contains(expected.trim())) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            System.out.println("Exception while verifying cart contents: " + e.getMessage());
            return false;
        }
    }

}
