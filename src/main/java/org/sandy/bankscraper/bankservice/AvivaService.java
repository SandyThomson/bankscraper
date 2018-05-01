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

public class AvivaService implements BankScraper {

	private String user;
	private String pass;
	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();
	private int tries = 0;

	public AvivaService(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public void scrape() {

		WebDriver webDriver = WebDriverService.getDriver();

		try {

			long timeInMillisStart = Calendar.getInstance().getTimeInMillis(); //

			webDriver.get("https://www.direct.aviva.co.uk/MyAccount/login");
			ScraperReliabilityHelper.titleChecker(webDriver, "Welcome to MyAviva");
			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("username")));
			usernameField.sendKeys(user);
			WebElement passwordField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("password")));
			passwordField.sendKeys(pass);
			usernameField.sendKeys(Keys.RETURN);

			// Authenticated
			ScraperReliabilityHelper.titleChecker(webDriver, "MyAviva");

			Account a = getSubPageAccountInfo("2", webDriver);
			accounts.add(a);

			Account b = getSubPageAccountInfo("1", webDriver); // Rather
																// annoyingly
																// this can
																// shift about
																// on the
																// MyAviva page
			if (b.getBalance().equals(a.getBalance())) {
				// Sigh, retry with different id
				b = getSubPageAccountInfo("2", webDriver);
				if (b.getBalance().equals(a.getBalance())) {
					System.out.println("Aviva issues!");
				}
			}
			accounts.add(b);

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

	private Account getSubPageAccountInfo(String id, WebDriver webDriver) {

		Account a = null;
		WebElement link = ScraperReliabilityHelper.locateWebElement(webDriver,
				driver -> driver.findElement(By.id(id)).findElement(By.tagName("a")));
		link.click();

		int oldMaxWaitTime = ScraperReliabilityHelper.MAX_WAIT_TIME;
		ScraperReliabilityHelper.MAX_WAIT_TIME = 10; // seconds
		WebElement detailsButton = ScraperReliabilityHelper.locateWebElement(webDriver,
				driver -> driver.findElement(By.linkText("Details")));
		;
		ScraperReliabilityHelper.MAX_WAIT_TIME = oldMaxWaitTime;
		if(detailsButton == null){
			link.click();
			detailsButton = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.linkText("Details")));
		}
		
		detailsButton.click();

		a = new Account("Aviva");
		a.setName(ScraperReliabilityHelper
				.locateWebElement(webDriver, driver -> driver.findElement(By.cssSelector("span.a-breadcrumb__current"))).getText()
				.trim().replace("\n", " ").replace("\r", " "));
		a.setBalance(ScraperReliabilityHelper
				.locateWebElement(webDriver,
						driver -> driver.findElement((By.cssSelector("p.a-heading--1"))))
				.getText().replaceAll("[^\\d.]", ""));
		
		webDriver.get("https://www.direct.aviva.co.uk/MyPortfolio/");

		ScraperReliabilityHelper.titleChecker(webDriver, "MyAviva");

		try {
			Thread.sleep(2000); // Unreliable JS
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		return a;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public Long getScrapeTimeMillis() {
		return scrapeTimeMillis;
	}

}
