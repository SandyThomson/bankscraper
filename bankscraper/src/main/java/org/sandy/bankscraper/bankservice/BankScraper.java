package org.sandy.bankscraper.bankservice;

import java.util.List;
import java.util.concurrent.Callable;

import org.sandy.bankscraper.account.Account;

public interface BankScraper extends Callable<List<Account>> {
		
	public List<Account> getAccounts();
	
	public Long getScrapeTimeMillis();
	
	public void scrape();
	
	default List<Account> call() throws Exception {
		scrape();
		return getAccounts();
	}
	
}
