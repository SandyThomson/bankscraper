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

public class JupiterService implements BankScraper {

	private String user;
	private String pass;
	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();
	private int tries = 0;

	public JupiterService(String user, String pass) {
		this.user = user;
		this.pass = pass;
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

			webDriver.get("https://www.myaccountonline.co.uk/jupiter/");

			ScraperReliabilityHelper.titleChecker(webDriver, "Sign In - Jupiter Unit Trust Managers Limited");

			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("signInInputUserName")));
			usernameField.sendKeys(user);

			WebElement passwordField = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("signInInputPassword")));
			passwordField.sendKeys(pass);
			passwordField.sendKeys(Keys.RETURN);

			WebElement confirmButton = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("warningCorfirm"))); // Typo on the site.
			confirmButton.click();

			// Now authenticated!

			WebElement viewInvestmentsButton = ScraperReliabilityHelper.locateWebElement(webDriver,
					driver -> driver.findElement(By.id("dashboardButtonView")));

			ScraperReliabilityHelper.titleChecker(webDriver, "Home Page - Jupiter Unit Trust Managers Limited");
			viewInvestmentsButton.click();
				
			ScraperReliabilityHelper.titleChecker(webDriver, "Investments - Jupiter Unit Trust Managers Limited");

			// Investments view page

			List<WebElement> investments = 
					ScraperReliabilityHelper.locateWebElements(webDriver,
							driver -> driver.findElements(By.className("bdr-l-r-b")));
					
			for (WebElement investment : investments) {
				Account a = new Account("Jupiter");
				a.setName(investment.findElement(By.className("tdDiv01")).getText());
				a.setBalance(investment.findElement(By.className("tdDiv05")).getText().replaceAll("[^\\d.]", ""));
				accounts.add(a);
			}

			WebElement logoutLink =
					ScraperReliabilityHelper.locateWebElement(webDriver,
							driver -> driver.findElement(By.id("siteHeadLinkSignOut")));
			logoutLink.click();

			ScraperReliabilityHelper.titleChecker(webDriver, "Sign In - Jupiter Unit Trust Managers Limited");

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
