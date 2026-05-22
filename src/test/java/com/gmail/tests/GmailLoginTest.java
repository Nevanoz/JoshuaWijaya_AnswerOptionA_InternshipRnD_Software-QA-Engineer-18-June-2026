package com.gmail.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Gmail Login and Email Reading Test
 * Tests login functionality and retrieves last unread email title
 */
public class GmailLoginTest extends BaseTest {
    
    /**
     * Test to login to Gmail and log the last unread email title
     */
    @Test(priority = 1, description = "Login to Gmail and retrieve last unread email")
    public void testGmailLoginAndReadUnreadEmail() {
        logger.info("=== Starting Gmail Login Test ===");
        
        try {
            // Step 1: Navigate to Gmail
            logger.info("Step 1: Navigating to Gmail...");
            driver.get(config.getProperty("gmail.url"));
            waitFor(2);
            
            // Step 2: Enter email address
            logger.info("Step 2: Entering email address...");
            WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("identifierId"))
            );
            emailInput.clear();
            emailInput.sendKeys(config.getProperty("gmail.email"));
            logger.info("Email entered: " + config.getProperty("gmail.email"));
            
            // Click Next button
            WebElement nextButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Next']"))
            );
            nextButton.click();
            waitFor(3);
            
            // Step 3: Enter password
            logger.info("Step 3: Entering password...");
            WebElement passwordInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@name='Passwd' or @type='password']")
                )
            );
            passwordInput.clear();
            passwordInput.sendKeys(config.getProperty("gmail.password"));
            logger.info("Password entered");
            
            // Click Next button
            WebElement passwordNextButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Next']"))
            );
            passwordNextButton.click();
            waitFor(5);
            
            // Step 4: Handle 2FA if prompted
            logger.info("Step 4: Checking for 2FA prompts...");
            handleTwoFactorAuthentication();
            
            // Step 5: Wait for inbox to load
            logger.info("Step 5: Waiting for inbox to load...");
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='main']")
            ));
            waitFor(3);
            
            // Verify login successful
            Assert.assertTrue(driver.getCurrentUrl().contains("mail.google.com/mail"),
                "Login failed - not redirected to inbox");
            logger.info("✓ Successfully logged into Gmail");
            
            // Step 6: Get last unread email title
            logger.info("Step 6: Retrieving last unread email...");
            String lastUnreadEmailTitle = getLastUnreadEmailTitle();
            
            // Step 7: Log the result
            if (lastUnreadEmailTitle != null && !lastUnreadEmailTitle.isEmpty()) {
                logger.info("===========================================");
                logger.info("LAST UNREAD EMAIL TITLE: " + lastUnreadEmailTitle);
                logger.info("===========================================");
                
                // Also print to console
                System.out.println("\n========================================");
                System.out.println("LAST UNREAD EMAIL SUBJECT:");
                System.out.println(lastUnreadEmailTitle);
                System.out.println("========================================\n");
            } else {
                logger.warn("No unread emails found in inbox");
                System.out.println("\nNo unread emails found in inbox\n");
            }
            
            logger.info("=== Test Completed Successfully ===");
            
        } catch (Exception e) {
            logger.error("Test failed with exception", e);
            Assert.fail("Test execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Handles Two-Factor Authentication prompts
     * Provides manual intervention time if 2FA is required
     */
    private void handleTwoFactorAuthentication() {
        try {
            // Check if 2FA verification page is displayed
            List<WebElement> verificationElements = driver.findElements(
                By.xpath("//*[contains(text(), 'Verify') or contains(text(), 'confirm') " +
                        "or contains(text(), '2-Step')]")
            );
            
            if (!verificationElements.isEmpty()) {
                logger.warn("2FA verification detected!");
                System.out.println("\n" + "=".repeat(60));
                System.out.println("⚠️  TWO-FACTOR AUTHENTICATION REQUIRED");
                System.out.println("=".repeat(60));
                System.out.println("Please complete the verification manually.");
                System.out.println("The test will wait for 60 seconds...");
                System.out.println("=".repeat(60) + "\n");
                
                // Wait for manual intervention (60 seconds)
                waitFor(60);
                
                // Additional wait for page to load after 2FA
                waitFor(5);
                
                logger.info("Proceeding after 2FA wait period");
            } else {
                logger.info("No 2FA prompt detected, proceeding...");
            }
            
        } catch (Exception e) {
            logger.warn("Error while checking for 2FA: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves the title/subject of the last unread email
     * @return Email subject or null if no unread emails
     */
    private String getLastUnreadEmailTitle() {
        try {
            // Wait for email list to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='main']")
            ));
            
            // Find all unread emails (they have a specific class or styling)
            // Gmail marks unread emails with specific attributes
            List<WebElement> unreadEmails = driver.findElements(
                By.xpath("//tr[contains(@class, 'zE')]//span[@class='bog']")
            );
            
            // Alternative selector if above doesn't work
            if (unreadEmails.isEmpty()) {
                unreadEmails = driver.findElements(
                    By.xpath("//tr[contains(@class, 'zA')]//span[@class='bog']")
                );
            }
            
            // Another alternative - look for emails in bold (unread indicator)
            if (unreadEmails.isEmpty()) {
                unreadEmails = driver.findElements(
                    By.xpath("//span[@class='bog']/span[contains(@style, 'font-weight')]")
                );
            }
            
            if (!unreadEmails.isEmpty()) {
                // Get the last unread email (most recent)
                WebElement lastUnreadEmail = unreadEmails.get(unreadEmails.size() - 1);
                String emailTitle = lastUnreadEmail.getText();
                
                logger.info("Found " + unreadEmails.size() + " unread email(s)");
                return emailTitle;
            } else {
                logger.warn("No unread emails found using any selector");
                
                // Fallback: Get any email title as example
                List<WebElement> anyEmails = driver.findElements(
                    By.xpath("//span[@class='bog']")
                );
                
                if (!anyEmails.isEmpty()) {
                    logger.info("Returning first available email as fallback");
                    return anyEmails.get(0).getText() + " (may be read)";
                }
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving unread email title", e);
        }
        
        return null;
    }
}