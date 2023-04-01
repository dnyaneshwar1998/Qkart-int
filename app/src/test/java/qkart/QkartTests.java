package qkart;



import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import qkart.pages.*;

public class QkartTests {
    static RemoteWebDriver driver;
    public static String lastGeneratedUserName;
    public static WebDriverWait wait;
    public static Wait fluentWait;

    @BeforeSuite(alwaysRun = true)
    public static void createDriver() throws MalformedURLException {
        // Launch Browser using Zalenium
        //final DesiredCapabilities capabilities = new DesiredCapabilities();
        //capabilities.setBrowserName(BrowserType.CHROME);
        //driver = new RemoteWebDriver(new URL("http://127.0.0.1:4444/wd/hub"), capabilities);
        ChromeOptions co = new ChromeOptions();
        co.addArguments("--remote-allow-origins=*");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(co);
        driver.manage().window().maximize();
        System.out.println("createDriver()");
    }

    /*
     * Testcase01: Verify a new user can successfully register
     */
    @Test(description  = "Verify registration happens correctly", priority = 1, groups = {"Sanity_test"})
    @Parameters ({"TC1_Username", "TC1_Password"})
    public void TestCase01(@Optional("testuserOpn")String  username,@Optional("Opn@123")String password) throws InterruptedException {
        Boolean status;

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(username, password, true);
        assertTrue(status, "Failed to register new user");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the login page and login with the previuosly registered user
        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, password);
        assertTrue(status, "Failed to login with registered user");

        // Visit the home page and log out the logged in user
        Home home = new Home(driver);
        status = home.PerformLogout();
        assertTrue(status,"Test Case 1: Verify user Registration : FAIL");
        Actions act = new Actions(driver);



    }

    /*
     * Verify that an existing user is not allowed to re-register on QKart
     */
    @Test(description = "Verify re-registering an already registered user fails", priority = 2, groups = {"Sanity_test"})
    @Parameters ({"TC2_Username", "TC2_Password"})
    public void TestCase02(@Optional("testuserOpn")String  username,@Optional("Opn@123")String password) throws InterruptedException {

        Boolean status;

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(username, password, true);
        assertTrue(status , "Test Case 2: Verify user Registration :"+(status ? "PASS" : "FAIL"));


        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the Registration page and try to register using the previously
        // registered user's credentials
        registration.navigateToRegisterPage();
        status = registration.registerUser(lastGeneratedUserName, password, false);


        assertFalse(status ,"Test Case 2: Verify user Registration : "+(status ? "FAIL" : "PASS") );
        System.out.println("Testcase02 ended !!!!!!!");
    }

    /*
     * Verify the functinality of the search text box
     */
    @Test(description = "Verify the functionality of search text box", priority = 3, groups = {"Sanity_test"})
    @Parameters ("TC3_ProductNameToSearchFor")
    public void TestCase03(String productToSearch) throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();
        boolean status;

        // Visit the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // SLEEP_STMT_01 : Wait for Page to Load
        //Thread.sleep(5000); // Not required as driver.get statement waits till all elements loads

        // Search for the "yonex" product
        status = homePage.searchForProduct(productToSearch);
        //Thread.sleep(5000);// Find by me SLEEP_STMT_05 // Removing this as unwanted

        softAssert.assertTrue(status);

        // Fetch the search results
        List<WebElement> searchResults = homePage.getSearchResults();

        //System.out.println("List of results :" + searchResults.size());

        // Verify the search results are available
        assertFalse(searchResults.size() == 0,"Test Case Failure. There were no results for the given search string,FAIL");


        for (WebElement webElement : searchResults) {
            // Create a SearchResult object from the parent element
            SearchResult resultelement = new SearchResult(webElement);

            // Verify that all results contain the searched text
            String elementText = resultelement.getTitleofResult();
            assertTrue(elementText.toUpperCase().contains("YONEX"),"Test Case Failure. Test Results contains un-expected values: " + elementText +
                    "FAIL" );

        }

        // SLEEP_STMT_02
        //Thread.sleep(2000); // Unwanted wait statement

        // Search for product
        status = homePage.searchForProduct("Gesundheit");
        assertTrue(status,"Test Case Failure. Unable to search for given product FAIL" );


        // Verify no search results are found
        searchResults = homePage.getSearchResults();
        assertTrue(searchResults.size() == 0,"Test Case Fail. Expected: no results , actual: Results were available FAIL" );
        softAssert.assertAll();

    }

    /*
     * Verify the presence of size chart and check if the size chart content is as
     * expected
     */
    @Test(description = "Verify the existence of size chart for certain items and validate contents of size chart", priority = 4, groups = {"Regression_Test"})
    @Parameters ("TC4_ProductNameToSearchFor")
    public void TestCase04(@Optional("Roadster")String productNameToSearchFor) throws InterruptedException {
        boolean status = false;
        SoftAssert softAssert = new SoftAssert();

        // Visit home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Search for product and get card content element of search results
        status = homePage.searchForProduct(productNameToSearchFor);
        List<WebElement> searchResults = homePage.getSearchResults();

        // Create expected values
        List<String> expectedTableHeaders = Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
        List<List<String>> expectedTableBody = Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                Arrays.asList("7", "7", "41", "10.2"), Arrays.asList("8", "8", "42", "10.6"),
                Arrays.asList("9", "9", "43", "11"), Arrays.asList("10", "10", "44", "11.5"),
                Arrays.asList("11", "11", "45", "12.2"), Arrays.asList("12", "12", "46", "12.6"));

        // Verify size chart presence and content matching for each search result
        for (WebElement webElement : searchResults) {
            SearchResult result = new SearchResult(webElement);

            // Verify if the size chart exists for the search result
            if (result.verifySizeChartExists()) {
                //logStatus("Step Success", "Successfully validated presence of Size Chart Link", "PASS");

                // Verify if size dropdown exists
                status = result.verifyExistenceofSizeDropdown(driver);
                //logStatus("Step Success", "Validated presence of drop down", status ? "PASS" : "FAIL");
                assertTrue(status,"Validated presence of drop down FAIL" );

                status = false;
                // Open the size chart
                if (result.openSizechart()) {
                    // Verify if the size chart contents matches the expected values
                    assertTrue(result.validateSizeChartContents(expectedTableHeaders, expectedTableBody, driver),"Step Failure Failure while validating contents of Size Chart Link FAIL");


                    // Close the size chart modal
                    status = result.closeSizeChart(driver);

                }
                softAssert.assertTrue(status,"TestCase 4 Test Case Fail. Failure to open Size Chart FAIL");


            }
            softAssert.assertTrue(result.verifySizeChartExists(), "TestCase 4 Test Case Fail. Size Chart Link does not exist FAIL");

        }
        //logStatus("TestCase 4", "End Test Case: Validated Size Chart Details", status ? "PASS" : "FAIL");
        softAssert.assertAll();
    }

    /*
     * Verify the complete flow of checking out and placing order for products is
     * working correctly
     */
    @Test(description = "Verify that a new user can add multiple products in to the cart and Checkout", priority = 5, groups = {"Sanity_test"})
    @Parameters({"TC5_ProductNameToSearchFor","TC5_ProductNameToSearchFor2","TC5_AddressDetails"})
    public void TestCase05(String productNameToSearchFor, String productNameToSearchFor2, String addressDetails ) throws InterruptedException {
        Boolean status;
        // Go to the Register page
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();

        // Register a new user
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");

        // Save the username of the newly registered user
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Go to the login page
        Login login = new Login(driver);
        login.navigateToLoginPage();

        // Login with the newly registered user's credentials
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        // Go to the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Find required products by searching and add them to the user's cart
        status = homePage.searchForProduct(productNameToSearchFor);
        homePage.addProductToCart(productNameToSearchFor);
        status = homePage.searchForProduct(productNameToSearchFor2);
        homePage.addProductToCart(productNameToSearchFor2);

        // Click on the checkout button
        homePage.clickCheckout();

        // Add a new address on the Checkout page and select it
        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(addressDetails);
        checkoutPage.selectAddress(addressDetails);

        // Place the order
        checkoutPage.placeOrder();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

        // Check if placing order redirected to the Thansk page
        status = driver.getCurrentUrl().endsWith("/thanks");

        // Go to the home page
        homePage.navigateToHome();

        // Log out the user
        homePage.PerformLogout();

        assertTrue(status,"Test Case 5: Happy Flow Test Completed : FAIL" );


    }

    @Test(description = "Verify that the contents of the cart can be edited", priority = 6, groups = {"Regression_Test"})
    @Parameters({"TC6_ProductNameToSearch1","TC6_ProductNameToSearch2"})
    public void TestCase06(String productNameToSearch, String productNameToSearch2) throws InterruptedException {
        Boolean status;
        Home homePage = new Home(driver);
        Register registration = new Register(driver);
        Login login = new Login(driver);

        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        homePage.navigateToHome();
        status = homePage.searchForProduct(productNameToSearch);
        homePage.addProductToCart(productNameToSearch);

        status = homePage.searchForProduct(productNameToSearch2);
        homePage.addProductToCart(productNameToSearch2);

        // update watch quantity to 2
        homePage.changeProductQuantityinCart(productNameToSearch, 2);

        // update table lamp quantity to 0
        homePage.changeProductQuantityinCart(productNameToSearch2, 0);

        // update watch quantity again to 1
        homePage.changeProductQuantityinCart(productNameToSearch, 1);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
        } catch (TimeoutException e) {
            System.out.println("Error while placing order in: " + e.getMessage());
        }

        status = driver.getCurrentUrl().endsWith("/thanks");

        homePage.navigateToHome();
        homePage.PerformLogout();

        assertTrue(status,"Test Case 6: Verify that cart can be edited: ");


    }

    /*
     * Verify that the cart contents are persisted after logout
     */
//     "String: TC7_ListOfProductsToAddToCart
// (use a string separated by "";"")"
    @Test(description = "Verify that the contents made to the cart are saved against the user's login details", priority = 7, groups = {"Regression_Test"}, dataProvider = "TC7_ListOfProductsToAddToCart")
    //@Parameters ("TC7_ListOfProductsToAddToCart")
    public void TestCase07(String listOfProductsToAddToCart1, String listOfProductsToAddToCart2) throws InterruptedException {
        Boolean status = false;
        SoftAssert softAssert = new SoftAssert();
        List<String> expectedResult = Arrays.asList("Stylecon 9 Seater RHS Sofa Set ",
                "Xtend Smart Watch");


        Register registration = new Register(driver);
        Login login = new Login(driver);
        Home homePage = new Home(driver);

        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        homePage.navigateToHome();
        status = homePage.searchForProduct(listOfProductsToAddToCart1);
        homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set");

        status = homePage.searchForProduct(listOfProductsToAddToCart2);
        homePage.addProductToCart("Xtend Smart Watch");

        homePage.PerformLogout();

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");

        WebElement checkoutBtn = driver.findElement(By.className("checkout-btn"));

        status = checkoutBtn.isDisplayed();

        softAssert.assertTrue(status, "Checkout button is not displayed");

        status = homePage.verifyCartContents(expectedResult);

        assertTrue(status, "Test Case 7: Verify that cart contents are persisted after logout: FAIL ");

        homePage.PerformLogout();
        softAssert.assertAll();

    }

    @DataProvider(name = "TC7_ListOfProductsToAddToCart")
    public static Object[][] listOfProductsToAddToCart() {
        String[][] subjects = {{"Stylecon 9 Seater RHS Sofa Set","Xtend Smart Watch"}};
        return subjects;
    }

    /* Verify insufficiant balance */
    @Test(description = "Verify that insufficient balance error is thrown when the wallet balance is not enough", priority = 8, groups = {"Sanity_test"})
    @Parameters({"TC8_ProductName","TC8_Qty"})
    public void TestCase08(String productName, int quantity ) throws InterruptedException {
        Boolean status;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        Home homePage = new Home(driver);
        homePage.navigateToHome();
        status = homePage.searchForProduct(productName);
        homePage.addProductToCart(productName);

        homePage.changeProductQuantityinCart(productName, quantity);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();
        Thread.sleep(3000);

        status = checkoutPage.verifyInsufficientBalanceMessage();

        assertTrue(status, "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough: FAIL");
    }

    /*Verify that product added to cart is available when a new tab is opened*/

    @Test(description = "Verify that a product added to a cart is available when a new tab is added", priority = 9, groups = {"Regression_Test"})
    public void TestCase09() throws InterruptedException {
        Boolean status = false;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct("YONEX");
        homePage.addProductToCart("YONEX Smash Badminton Racquet");

        String currentURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

        driver.get(currentURL);
        Thread.sleep(2000);

        List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
        status = homePage.verifyCartContents(expectedResult);

        driver.close();

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        assertTrue(status,"Verify that product added to cart is available when a new tab is openedFAIL" );



    }

    /*Verify that the Privacy Policy, About Us are displayed correctly */
    @Test(description = "Verify that privacy policy and about us links are working fine", priority = 10, groups = {"Regression_Test"})
    public void TestCase10() throws InterruptedException {

        Boolean status = false;
        SoftAssert softAssert = new SoftAssert();
        String qkartUrl ="https://crio-qkart-frontend-qa.vercel.app/";
        driver.get(qkartUrl);
        String parentWindowHandle = driver.getWindowHandle();

        //locating Privacy Policy link and clicking on it
        WebElement privacyPolicyElement = driver.findElement(By.linkText("Privacy policy"));
        privacyPolicyElement.click();

        //String parentWindowHandle2 = driver.getWindowHandle();
        //System.out.println(parentWindowHandle.equals(parentWindowHandle2));

        // Verifying parent tab url should not cahnges i.e. should remaed as Qkart home page link
        status = driver.getCurrentUrl().equals(qkartUrl);
        softAssert.assertTrue(status,"url-changedTo-failed");

        //Getting window handles of opened tabs, starts from 0th index
        ArrayList<String> tabs2 = new ArrayList<String> (driver.getWindowHandles());
        driver.switchTo().window(tabs2.get(1));
        Thread.sleep(5000);

        WebElement privacyPolicyTitleElement = driver.findElement(By.xpath("//h2[text()='Privacy Policy']"));
        status = privacyPolicyTitleElement.getText().equalsIgnoreCase("Privacy Policy");
        softAssert.assertTrue(status,"Privacy policy text is not correct");

        driver.switchTo().window(tabs2.get(0));
        System.out.println(driver.getCurrentUrl());

        //locating Terms Of Service link and clicking on it
        WebElement termsOfServiceElement = driver.findElement(By.linkText("Terms of Service"));
        termsOfServiceElement.click();

        // Verifying parent tab url should not cahnges i.e. should remaed as Qkart home page link
        status = driver.getCurrentUrl().equals(qkartUrl);
        softAssert.assertTrue(status,"url-changedTo-failed");

        //Getting window handles of opened tabs, starts from 0th index
        List<String> tabs3 = new ArrayList<String> (driver.getWindowHandles());
        driver.switchTo().window(tabs3.get(2));
        System.out.println(driver.getCurrentUrl());

        WebElement termsOfServiceTitleElement = driver.findElement(By.xpath("//h2['Terms of Service']"));
        status = termsOfServiceTitleElement.getText().equalsIgnoreCase("Terms of Service");
        softAssert.assertTrue(status,"Terms of Service text is not correct");


        // Traversing through list and closing tabs other than parent
        for (String tab : tabs3){

            if(!tab.equals(parentWindowHandle)){
                driver.switchTo().window(tab);
                Thread.sleep(3000);
                //tabs3.remove(tab);
                driver.close();
                driver.switchTo().window(parentWindowHandle);

            }
        }
        // As of now we closed othe tabs other than oarent window hence size should be 1
        tabs3 = new ArrayList<String> (driver.getWindowHandles());
        assertFalse(tabs3.size()>1,"closing-all-tabs-failed");
        softAssert.assertAll();


    }

    /* Verify that contact us option is working correctly */
    @Test(description = "Verify that the contact us dialog works fine", priority = 11, groups = {"Regression_Test"})
    @Parameters ({"TC11_ContactusUserName","TC11_ContactUsEmail","TC11_QueryContent"})
    public void TestCase11(String contactusUserName, String contactusEmail, String queryContent) throws InterruptedException {
        Boolean status;
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        driver.findElement(By.xpath("//*[text()='Contact us']")).click();

        WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
        name.sendKeys(contactusUserName);
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys(contactusEmail);
        WebElement message = driver.findElement(By.xpath("//input[@placeholder='Message']"));
        message.sendKeys(queryContent);

        WebElement contactUs = driver.findElement(
                By.xpath("/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

        contactUs.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        status = wait.until(ExpectedConditions.invisibilityOf(contactUs));

        assertTrue(status , "Contact us details not updated successfully...");
    }


    @Test(description = "Ensure that the Advertisement Links on the QKART page are clickable", priority = 12, groups = {"Sanity_test"})
    @Parameters({"TC12_ProductNameToSearch","TC12_AddresstoAdd"})
    public void TestCase12(String productNameToSearch, String addresstoAdd ) throws InterruptedException {
        Boolean status = false;
        // TODO: CRIO_TASK_MODULE_SYNCHRONISATION -


        // Go to the Register page
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();

        // Register a new user
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Failed to register new user");

        // Save the username of the newly registered user
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Go to the login page
        Login login = new Login(driver);
        login.navigateToLoginPage();

        // Login with the newly registered user's credentials
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to login with registered user");

        // Go to the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Find required products by searching and add them to the user's cart
        status = homePage.searchForProduct(productNameToSearch);
        homePage.addProductToCart(productNameToSearch);

        // Click on the checkout button
        homePage.clickCheckout();

        // Add a new address on the Checkout page and select it
        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(addresstoAdd);
        checkoutPage.selectAddress(addresstoAdd);

        // Place the order
        checkoutPage.placeOrder();

        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.urlContains("https://crio-qkart-frontend-qa.vercel.app/thanks"));

        String parentPageHandle = driver.getWindowHandle();

        //Located all iframes and stored in list
        List <WebElement> totalIframeElemenents = driver.findElements(By.tagName("iframe"));
        // if(totalIframeElemenents.size()==3){
        //Switching to 1st iframe
        WebElement firstFrame = driver.findElement(By.xpath("/html/body/div/div/div[2]/div/iframe[1]"));
        driver.switchTo().frame(firstFrame);

        // Located QKART ad button and checked if they are enabled
        WebElement firstAdViewCartElement = driver.findElement(By.xpath("//button[normalize-space()='View Cart']"));
        WebElement firstAdBuyNowElement = driver.findElement(By.xpath("//button[normalize-space()='Buy Now']"));
        status = firstAdViewCartElement.isEnabled() && firstAdBuyNowElement.isEnabled();
        //focus will directly move to main webpage
        firstAdBuyNowElement.click();
        driver.navigate().back();
        driver.switchTo().parentFrame();

        WebElement secondFrame = driver.findElement(By.xpath("/html/body/div/div/div[2]/div/iframe[2]"));
        driver.switchTo().frame(secondFrame);

        WebElement secondAdViewCartElement = driver.findElement(By.xpath("//button[normalize-space()='View Cart']"));
        WebElement secondAdBuyNowElement = driver.findElement(By.xpath("//button[normalize-space()='Buy Now']"));
        status = secondAdViewCartElement.isEnabled() && secondAdBuyNowElement.isEnabled();

        secondAdBuyNowElement.click();
        driver.navigate().back();
        driver.switchTo().parentFrame();
        driver.switchTo().frame(2);
        driver.switchTo().parentFrame();
        // }
        // else {
        //     logStatus("Step Failure", "3 ads are not present ", status ? "PASS" : "FAIL");
        //     logStatus("End TestCase", "Test Case 12: Advirtisement check failed : ", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "3-ads-check-failed", "TestCase12");
        // }


        // Go to the home page
        homePage.navigateToHome();
        // Thread.sleep(3000); // Find by me SLEEP_STMT_06 // Not required as driver.get statement waits till all elements loads

        // Log out the user
        homePage.PerformLogout();

        assertTrue(status, "Test Case 12: Advirtisement check : FAIL" );
    }




    @AfterSuite
    public static void quitDriver() {
        System.out.println("quit()");
        driver.quit();
    }

    public static void logStatus(String type, String message, String status) {

        System.out.println(String.format("%s |  %s  |  %s | %s",
                String.valueOf(java.time.LocalDateTime.now()), type, message, status));
    }

    public static void takeScreenshot(WebDriver driver, String screenshotType, String description) {
        try {
            File theDir = new File("/screenshots");
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            String timestamp = String.valueOf(java.time.LocalDate.now());
            String timestamp2 = String.valueOf(java.time.LocalTime.now());
            System.out.println(timestamp2);
            String fileName = String.format("screenshot_%s_%s_%s.png",timestamp, screenshotType, description);
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
            File DestFile = new File("screenshots/"  + fileName);
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
