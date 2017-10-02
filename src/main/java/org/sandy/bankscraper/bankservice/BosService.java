package org.sandy.bankscraper.bankservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sandy.bankscraper.account.Account;
import org.sandy.bankscraper.service.WebDriverService;
import org.sandy.bankscraper.tools.ScraperReliabilityHelper;

public class BosService implements BankScraper {

	private String user;
	private String pass;
	private String memorable;
	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();
	private int tries = 0;

	public BosService(String user, String pass, String memorable) {
		this.user = user;
		this.pass = pass;
		this.memorable = memorable;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public Long getScrapeTimeMillis() {
		return scrapeTimeMillis;
	}

	public void scrape() {

		WebDriver webDriver = WebDriverService.getDriver();

		try {

			long timeInMillisStart = Calendar.getInstance().getTimeInMillis(); //

			webDriver.get("https://online.bankofscotland.co.uk/personal/logon/login.jsp");

			ScraperReliabilityHelper.titleChecker(webDriver, "Bank of Scotland - Welcome to internet banking");

			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("frmLogin:strCustomerLogin_userID")));
			usernameField.sendKeys(user);

			WebElement passwordField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("frmLogin:strCustomerLogin_pwd")));
			passwordField.sendKeys(pass);

			WebElement submitButton = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("frmLogin:btnLogin2")));
			submitButton.submit();

			Thread.sleep(3000);

			ScraperReliabilityHelper.titleChecker(webDriver, "Bank of Scotland - Enter Memorable Information");

			WebElement memorableInfoForm = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("frmentermemorableinformation1")));
			List<WebElement> foundLabels = memorableInfoForm.findElements(By.tagName("label"));

			for (int i = 0; i < 3; i++) {

				// Should return something like "Character 3"
				String rawText = foundLabels.get(i).getText();
				String characterNumber = rawText.replaceAll("[^\\d]", "");

				WebElement w = memorableInfoForm.findElement(
						By.id("frmentermemorableinformation1:strEnterMemorableInformation_memInfo" + (i + 1)));
				Character c = memorable.charAt(Integer.parseInt(characterNumber) - 1);

				w.sendKeys(c.toString());
			}

			WebElement continueButton = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("frmentermemorableinformation1:btnContinue")));
			continueButton.click();

			// Bullshit marketing page that sometimes appears
			if (webDriver.getTitle().equals("Bank of Scotland - Interstitial page")) {
				continueButton = ScraperReliabilityHelper.locateWebElement(webDriver,
						driver -> driver.findElement(By.id("frmMdlSAN:continueBtnSAN")));
				continueButton.click();
			}

			// Now authenticated!
			ScraperReliabilityHelper.titleChecker(webDriver, "Bank of Scotland - Personal Account Overview");

			int oldMaxWaitTime = ScraperReliabilityHelper.MAX_WAIT_TIME;
			ScraperReliabilityHelper.MAX_WAIT_TIME = 10; // seconds
			for (int i = 1; true; i++) {
				final int count = i;
				Account a = new Account("Bank of Scotland");

				WebElement accountInfo = ScraperReliabilityHelper.locateWebElement(webDriver,
						driver -> driver.findElement(By.id("des-m-sat-xx-" + count)));
				if (accountInfo == null)
					break; // No more accounts to scrape!
				a.setName(accountInfo.findElement(By.id("lnkAccName_des-m-sat-xx-" + count)).getText());

				String scrapedBalance = accountInfo.findElement(By.className("balance")).findElement(By.tagName("span"))
						.getText().replaceAll("[^\\d.]", "");

				/*
				 * Zero amount shows up as "Nil" on site
				 */
				String balance = (scrapedBalance == null || scrapedBalance.equals("")) ? "0" : scrapedBalance;

				a.setBalance(balance);
				accounts.add(a);
			}
			
			ScraperReliabilityHelper.MAX_WAIT_TIME = oldMaxWaitTime; 

			WebElement logoutLink = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("ifCommercial:ifCustomerBar:ifMobLO:outputLinkLogOut")));
			logoutLink.click();

			ScraperReliabilityHelper.titleChecker(webDriver, "Bank of Scotland - Logged Off");

			long timeInMillisEnd = Calendar.getInstance().getTimeInMillis();

			scrapeTimeMillis = timeInMillisEnd - timeInMillisStart;
			WebDriverService.returnDriver(webDriver);

		} catch (Exception e) {
			WebDriverService.returnDriver(webDriver);
			tries++;
			if (tries < 3) {
				scrape();
			}
		}

	}

	@Override
	public List<Account> call() throws Exception {
		scrape();
		return accounts;
	}

}
