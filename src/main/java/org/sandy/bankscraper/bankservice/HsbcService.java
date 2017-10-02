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

public class HsbcService implements BankScraper {

	private String user;
	private String pass;
	private String otp;
	private int tries = 0;

	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();

	public HsbcService(String user, String pass, String otp) {
		this.user = user;
		this.pass = pass;
		this.otp = otp;
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
		long timeInMillisStart = Calendar.getInstance().getTimeInMillis();
		


		webDriver.get("http://www.hsbc.co.uk/1/2/welcome-gsp?initialAccess=true&IDV_URL=hsbc.MyHSBC_pib");

		ScraperReliabilityHelper.titleChecker(webDriver, "Log on to Online Banking: Username | HSBC");

		WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("Username1")));
		usernameField.sendKeys(user);
		usernameField.sendKeys(Keys.RETURN);

		ScraperReliabilityHelper.titleChecker(webDriver, "Log on to Online Banking: with or without Secure Key Log on | HSBC");

		
		WebElement passwordField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("memorableAnswer")));
		passwordField.sendKeys(pass);

		WebElement otpField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("idv_OtpCredential")));
		otpField.sendKeys(otp);
		otpField.sendKeys(Keys.RETURN);
		
		ScraperReliabilityHelper.titleChecker(webDriver, "Transaction history | My banking | HSBC");

		// Authenticated
		
		List<WebElement> names = ScraperReliabilityHelper.locateWebElements(webDriver, driver -> driver.findElements(By.cssSelector("span.itemTitle")));
		
		while( names == null || names.size() < 2){
			try {
				Thread.sleep(1000); // zzz JS horror
				names = ScraperReliabilityHelper.locateWebElements(webDriver, driver -> driver.findElements(By.cssSelector("span.itemTitle")));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<Account> accountsTemp = new ArrayList<Account>();
		for (WebElement n : names) {
			Account a = new Account("HSBC");
			;
			a.setName(n.getText().replaceAll("View Recent transactions for: ", "").trim());
			accountsTemp.add(a);
		}

		int i = 0;
		List<WebElement> balances = ScraperReliabilityHelper.locateWebElements(webDriver, driver -> driver.findElements(By.cssSelector("span.fr span.itemValue")));
		for (WebElement b : balances) {
			Account a = accountsTemp.get(i);
			a.setBalance(b.getText().replaceAll("[^\\d.]", ""));
			accountsTemp.set(i, a);
			i++;
		}
		
		accounts.addAll(accountsTemp);

//		WebElement logOut = webDriver.findElement(By.partialLinkText("Log off"));
//		logOut.click();
//
//		Assert.assertEquals("Log off Successful - Customer", webDriver.getTitle());

		long timeInMillisEnd = Calendar.getInstance().getTimeInMillis();

		scrapeTimeMillis = timeInMillisEnd - timeInMillisStart;
		WebDriverService.returnDriver(webDriver);

		} catch (Exception e){
			System.out.println(e);
			WebDriverService.returnDriver(webDriver);
			tries ++;
			if( tries < 3 ){
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
