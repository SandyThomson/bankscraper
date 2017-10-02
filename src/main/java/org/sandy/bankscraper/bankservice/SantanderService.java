package org.sandy.bankscraper.bankservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sandy.bankscraper.account.Account;
import org.sandy.bankscraper.service.WebDriverService;
import org.sandy.bankscraper.tools.ScraperReliabilityHelper;

public class SantanderService implements BankScraper {

	private String user;
	private String pass;
	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();
	private int tries = 0;

	public SantanderService(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public void scrape() {

		WebDriver webDriver = WebDriverService.getDriver();

		try {

			long timeInMillisStart = Calendar.getInstance().getTimeInMillis(); //

			// Username page
			webDriver.get(
					"https://retail.santander.co.uk/LOGSUK_NS_ENS/BtoChannelDriver.ssobto?dse_operationName=LOGON");
			
			/*
			 * Title doesn't ever change, so use h1
			 */
			
			ScraperReliabilityHelper.h1Checker(webDriver, "Log on to Online Banking");
			
			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("infoLDAP_E.customerID")));
			usernameField.sendKeys(user);
			usernameField.sendKeys(Keys.RETURN);
			
			// Pin page
			ScraperReliabilityHelper.h1Checker(webDriver, "Recognise this image and phrase?");
			WebElement pinField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("authentication.CustomerPIN")));
			pinField.sendKeys(pass);
			pinField.sendKeys(Keys.RETURN);

			// Bank account dashboard

			ScraperReliabilityHelper.h1Checker(webDriver, "My accounts");

			List<WebElement> accountList = ScraperReliabilityHelper.locateWebElements(webDriver, driver -> driver.findElement(By.className("accountlist")).findElements(By.tagName("li")));
			for (WebElement e : accountList) {
				Account a = new Account("Santander");

				a.setName(e.findElement(By.className("info")).findElement(By.className("name")).getText());
				a.setBalance(e.findElement(By.className("balance")).findElement(By.className("amount")).getText()
						.replaceAll("[^\\d.]", ""));
				accounts.add(a);
			}

			// Horror of javascript logout. The below doesn't work - Santander
			// does at least timeout sessions

			// webDriver.findElement(By.linkText("Log off")).click();
			// webDriver.findElement(By.id("main_botones.boton2_enlace")).click();
			// Assert.assertEquals("You have successfully logged out of Online
			// Banking", webDriver.findElement(By.tagName("h1")).getText());

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

	public List<Account> getAccounts() {
		return accounts;
	}

	public Long getScrapeTimeMillis() {
		return scrapeTimeMillis;
	}

}
