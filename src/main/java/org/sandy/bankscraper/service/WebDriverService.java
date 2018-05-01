package org.sandy.bankscraper.service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class WebDriverService {

	private static ConcurrentLinkedQueue<WebDriver> idleWebDriverInstances = new ConcurrentLinkedQueue<>();
	
	private static ConcurrentLinkedQueue<WebDriver> inUseWebDriverInstances = new ConcurrentLinkedQueue<>();
	
	private static AtomicInteger webDriversRunning = new AtomicInteger(0);
	
	public static final Integer MAX_WEB_DRIVERS = 3;
	
	public synchronized static WebDriver getDriver() {

		WebDriver wd = idleWebDriverInstances.poll();
		if(wd == null){
			int running = webDriversRunning.get();
			if( running < MAX_WEB_DRIVERS ){
				if( webDriversRunning.compareAndSet(running, running+1) ){
					wd = startWebDriver();
					inUseWebDriverInstances.add(wd);
				}
			} else {
				while (true) {
					wd = idleWebDriverInstances.poll();
					if (wd != null) {
						inUseWebDriverInstances.add(wd);
						break;
					}
				}
			}
		}
		return wd;
	}

	public synchronized static void returnDriver(WebDriver wd) {
		inUseWebDriverInstances.remove(wd);
		idleWebDriverInstances.add(wd);
	}

	public static void closeAllDrivers() {
		for( WebDriver wd : inUseWebDriverInstances ){
			wd.close();
			wd.quit();
		}
		for( WebDriver wd : idleWebDriverInstances ){
			wd.close();
			wd.quit();
		}
		
	}

	public static WebDriver startWebDriver() {
		System.setProperty("webdriver.chrome.driver", "chromedriver_2.37.exe");
		return new ChromeDriver();
	}

}
