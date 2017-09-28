package org.sandy.bankscraper.bankservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sandy.bankscraper.account.Account;
import org.sandy.bankscraper.service.WebDriverService;
import org.sandy.bankscraper.tools.ScraperReliabilityHelper;

public class LegalAndGeneralService implements BankScraper {

	private String user;
	private String pass;
	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();
	private int tries = 0;

	public LegalAndGeneralService(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public void scrape() {

		WebDriver webDriver = WebDriverService.getDriver();

		try {

			long timeInMillisStart = Calendar.getInstance().getTimeInMillis(); //

			webDriver.get("https://www10.landg.com/SAuthGateWeb/login.html");
			ScraperReliabilityHelper.titleChecker(webDriver, "Legal & General - Log in");

			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("username")));
			usernameField.sendKeys(user);
			WebElement passwordField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("password")));
			passwordField.sendKeys(pass);
			usernameField.sendKeys(Keys.RETURN);

			// Authenticated
			ScraperReliabilityHelper.titleChecker(webDriver, "Legal & General - Employee Portfolio View");

			// Stupid L&G have 2 versions of the same site - go to the one with
			// sensible id's etc on the page
			webDriver.get("https://everydaymatters.landg.com/portalserver/my-l-and-g/index");

			ScraperReliabilityHelper.titleChecker(webDriver, "Legal & General - My Account");

			// Bank account dashboard

			List<WebElement> accountList = ScraperReliabilityHelper.locateWebElements(webDriver,
					driver -> {
						List<WebElement> results = driver.findElements(By.cssSelector("h3.ng-binding"));
						if (results.size() != 1) {
							throw new NoSuchElementException("Number of elements returned didn't equal 1");
						}
						return results;
					});

			accountList.addAll(ScraperReliabilityHelper.locateWebElements(webDriver,
					driver -> driver.findElements(By.cssSelector("span.h3"))));

			Account a = new Account("Legal and General");

			for (WebElement e : accountList) {

				if (e.getText().contains("£")) {
					a.setBalance(e.getText().replaceAll("[^\\d.]", ""));
				} else {
					a.setName(e.getText().trim());
				}
			}

			accounts.add(a);

			WebElement logout = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("logout-link")));
			logout.click();

			long timeInMillisEnd = Calendar.getInstance().getTimeInMillis();
			scrapeTimeMillis = timeInMillisEnd - timeInMillisStart;
			WebDriverService.returnDriver(webDriver);

		} catch (	Exception e) {
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

	@Override
	public List<Account> call() throws Exception {
		scrape();
		return accounts;
	}
}
