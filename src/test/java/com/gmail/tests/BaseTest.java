package com.gmail.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Base Test Class containing common setup and teardown methods
 */
public class BaseTest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Properties config;
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    /**
     * Loads configuration from properties file
     */
    protected void loadConfiguration() {
        try {
            config = new Properties();
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            config.load(fis);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load configuration file", e);
            throw new RuntimeException("Configuration file not found");
        }
    }
    
    /**
     * Setup method executed before each test
     * Initializes WebDriver and navigates to Gmail
     */
    @BeforeMethod
    public void setUp() {
        logger.info("Setting up test environment...");
        
        // Load configuration
        loadConfiguration();
        
        // Setup ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        
        // Add arguments for stability and compatibility
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        // Disable automation flags to avoid detection
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        
        // Optional: Run in headless mode
        if (Boolean.parseBoolean(config.getProperty("headless"))) {
            options.addArguments("--headless");
            logger.info("Running in headless mode");
        }
        
        // Initialize driver
        driver = new ChromeDriver(options);
        
        // Set timeouts
        int implicitWait = Integer.parseInt(config.getProperty("implicit.wait", "10"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        
        // Initialize explicit wait
        int explicitWait = Integer.parseInt(config.getProperty("explicit.wait", "20"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        
        logger.info("WebDriver initialized successfully");
    }
    
    /**
     * Teardown method executed after each test
     * Closes browser and cleans up resources
     */
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            logger.info("Closing browser...");
            driver.quit();
            logger.info("Test execution completed");
        }
    }
    
    /**
     * Utility method to pause execution
     * @param seconds Number of seconds to wait
     */
    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            logger.error("Wait interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}