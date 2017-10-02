package org.sandy.bankscraper.account;

import java.util.Comparator;

public class AccountCompatator implements Comparator<Account> {

	@Override
	public int compare(Account o1, Account o2) {
		if(o1 == null && o2 != null) return 1;
		if(o2 == null && o1 != null) return -1;
		if(o1 == null && o2 == null) return 0;
		
		// CBA with nullchecks here
		int outcome = o1.getBank().compareTo(o2.getBank());
		if(outcome != 0) return outcome;
		
		return o1.getName().compareTo(o2.getName());
	}

}
