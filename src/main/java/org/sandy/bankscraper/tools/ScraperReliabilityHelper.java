package org.sandy.bankscraper.tools;

import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ScraperReliabilityHelper {
	
	public static int MAX_WAIT_TIME = 90;

	public static void titleChecker(WebDriver driver, String expectedTitle) {

		for (int i = 0; i < MAX_WAIT_TIME; i++) {
			if (driver.getTitle().equals(expectedTitle)) {
				break;
			}
			try {
				System.out.println(i + ": Waiting for a second...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (!driver.getTitle().equals(expectedTitle)) {
			System.out.println("Still incorrect after " + MAX_WAIT_TIME + " seconds ... reloading");

			driver.get(driver.getCurrentUrl());
			for (int i = 0; i < MAX_WAIT_TIME; i++) {
				if (driver.getTitle().equals(expectedTitle)) {
					break;
				}
				try {
					System.out.println(i + ": Waiting for a second...");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		

		Assert.assertEquals(expectedTitle, driver.getTitle());
	}
	
	public static void h1Checker(WebDriver webdriver, String expectedText) {
		
		h1Checker(webdriver, expectedText, 1);
		
		WebElement element = locateWebElement(webdriver, driver -> driver.findElement(By.tagName("h1")));
		Assert.assertEquals(expectedText, element.getText());
	}
	
	private static void h1Checker(WebDriver webdriver, String expectedText, int attempt){
		if(attempt > 2){
			return;
		}
		
		for (int i = 0; i < MAX_WAIT_TIME; i++) {
			WebElement element = locateWebElement(webdriver, driver -> driver.findElement(By.tagName("h1")));
			if (element.getText().equals(expectedText)){
				return;
			}
			try {
				System.out.println(i + ": Waiting for a second...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		h1Checker(webdriver, expectedText, ++attempt);
	}

	public static WebElement locateWebElement( WebDriver driver, Function<WebDriver, WebElement> function){

		for (int i = 0; i < MAX_WAIT_TIME; i++) {
			try {
				return function.apply(driver);
			} catch (NoSuchElementException e) {
				try {
					System.out.println(i + ": Waiting for a second...");
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static List<WebElement> locateWebElements( WebDriver driver, Function<WebDriver, List<WebElement>> function){

		for (int i = 0; i < MAX_WAIT_TIME; i++) {
			try {
				return function.apply(driver);
			} catch (NoSuchElementException e) {
				try {
					System.out.println(i + ": Waiting for a second...");
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
}
