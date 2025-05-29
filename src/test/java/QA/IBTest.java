package QA;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class IBTest {

    WebDriver driver;
    WebDriverWait wait;
    ExtentReports extent;
    ExtentTest test;

    @BeforeSuite
    public void setupReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        driver.get("https://ndcdyn.interactivebrokers.com/sso/Login?RL=1&locale=en_US");
    }

    @Test(priority = 1)
    public void login() {
        test = extent.createTest("Login Test");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("xyz-field-username"))).sendKeys("testah000");
            driver.findElement(By.id("xyz-field-password")).sendKeys("tester12");
            driver.findElement(By.cssSelector("button.btn.btn-primary[type='submit']")).click();

            // Wait for search box (indicates post-login success)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));

            test.pass("Login successful and dashboard loaded.");
        } catch (Exception e) {
            test.fail("Login failed: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void validateSearchResult() {
        test = extent.createTest("Validate Search Result Test");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox"))).sendKeys("Citibank");

            // Wait for result sections to be rendered
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("otherSection")));

            List<WebElement> otherResults = driver.findElements(By.xpath("//div[@id='otherSection']//span[contains(text(), 'Costco Wholesale Credit Card')]"));
            List<WebElement> bestMatchResults = driver.findElements(By.xpath("//div[@id='bestMatchSection']//span[contains(text(), 'Costco Wholesale Credit Card')]"));

            Assert.assertTrue(otherResults.size() > 0, "'Costco' not found under 'Other'");
            Assert.assertEquals(bestMatchResults.size(), 0, "'Costco' incorrectly under 'Best Match'");
            driver.findElement(By.xpath("//button[text()='X']")).click();

            test.pass("Search result validated successfully.");
        } catch (Exception e) {
            test.fail("Search validation failed: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void addOfflineAccounts() {
        test = extent.createTest("Add Offline Accounts Test");
        try {
            String[] accountTypes = {"Brokerage", "Credit Card", "Other Asset", "Real Estate", "Savings"};

            for (int i = 0; i < accountTypes.length; i++) {
                if (i == 0) {
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Offline Account')]"))).click();
                } else {
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Configuration']"))).click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'+') and contains(@aria-label, 'External Accounts')]"))).click();
                }

                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Continue']"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[contains(text(), '" + accountTypes[i] + "')]"))).click();
                driver.findElement(By.xpath("//button[text()='Next']")).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountName"))).sendKeys(accountTypes[i] + " Test Account");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("value"))).sendKeys("1000");
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Save']"))).click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//button[text()='Save']")));
            }

            test.pass("All offline accounts added successfully.");
        } catch (Exception e) {
            test.fail("Adding offline accounts failed: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    @AfterSuite
    public void flushReport() {
        extent.flush();
    }
}
