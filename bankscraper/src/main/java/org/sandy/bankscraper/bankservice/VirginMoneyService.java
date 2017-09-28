package org.sandy.bankscraper.bankservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.sandy.bankscraper.account.Account;
import org.sandy.bankscraper.service.WebDriverService;
import org.sandy.bankscraper.tools.ScraperReliabilityHelper;

public class VirginMoneyService implements BankScraper {

	private String user;
	private String pass;
	private Map<String, String> securityAnswers = new HashMap<String, String>();
	private int tries = 0;

	private Long scrapeTimeMillis;
	private List<Account> accounts = new ArrayList<Account>();

	public VirginMoneyService(String user, String pass, String securityAnswers) {
		this.user = user;
		this.pass = pass;

		List<String> qAs = Arrays.asList(securityAnswers.split(","));

		for (String qA : qAs) {
			String[] split = qA.split("=");
			this.securityAnswers.put(split[0], split[1]);
		}
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

			webDriver.get("https://uk.virginmoney.com/virgin/service/?product=savings");

			// Username page
			ScraperReliabilityHelper.titleChecker(webDriver, "My Virgin Money | Virgin Money UK");

			WebElement usernameField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("alsoClientsChoice")));
			usernameField.sendKeys(user);
			usernameField.sendKeys(Keys.RETURN);

			// Password page
			ScraperReliabilityHelper.titleChecker(webDriver, "Virgin Money plc Online : Sign On : Input password");


			WebElement passwordForm = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("f")));
			List<WebElement> foundLabels = ScraperReliabilityHelper.locateWebElements(webDriver, driver -> driver.findElements(By.tagName("label")));

			for (int i = 0; i < 3; i++) {

				// Should return something like "Character 3"
				String rawText = foundLabels.get(i).getText();
				String characterNumber = rawText.replaceAll("[^\\d]", "");

				WebElement w = passwordForm.findElement(By.id("f:pwd-index-" + (i + 1)));
				Character c = pass.charAt(Integer.parseInt(characterNumber) - 1);

				w.sendKeys(c.toString());
			}

			WebElement next = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("f:btn-next")));
			next.click();

			// Security question page
			ScraperReliabilityHelper.titleChecker(webDriver, "Virgin Money plc Online : Sign on : Input security question");


			for (String securityQuestionFragment : securityAnswers.keySet()) {

				WebElement securityQuestionArea = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("f:squestion-school:pgrp")));
				Pattern pattern = Pattern.compile("(?i)" + securityQuestionFragment); // case
																						// insensitive
				String rawPageText = securityQuestionArea.getText();
				Matcher matcher = pattern.matcher(rawPageText);

				if (matcher.find()) {
					WebElement securityQuestionField = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("f:squestion-school:isc")));
					securityQuestionField.sendKeys(securityAnswers.get(securityQuestionFragment));
					securityQuestionField.sendKeys(Keys.RETURN);
					break;
				}
			}

			// Pointless security check page
			ScraperReliabilityHelper.titleChecker(webDriver, "Virgin Money plc Online : Sign on : Security check");

			next = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("f:btn-next")));
			next.click();

			// Logged in dashboard page
			ScraperReliabilityHelper.titleChecker(webDriver, "Virgin Money plc Online : Homepage");


			Account a = new Account("Virgin Money");
			
			
			String balance = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.className("balance-head"))).getText().replaceAll("[^\\d.]", "");
			String name = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.className("title-section"))).findElement(By.tagName("h1")).getText();
			a.setName(name);
			a.setBalance(balance);
			accounts.add(a);
			WebElement signOut = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("phc:phc:btn-sign-out")));
			signOut.click();

			WebElement confirm = ScraperReliabilityHelper.locateWebElement(webDriver, driver -> driver.findElement(By.id("p:btn-yes")));
			confirm.click();

			ScraperReliabilityHelper.titleChecker(webDriver, "My Virgin Money - Online service plus deals and discounts for Virgin Money customers | Virgin Money UK");


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

}
