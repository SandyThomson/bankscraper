package org.sandy.bankscraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sandy.bankscraper.account.Account;
import org.sandy.bankscraper.account.AccountCompatator;
import org.sandy.bankscraper.bankservice.AvivaService;
import org.sandy.bankscraper.bankservice.BankScraper;
import org.sandy.bankscraper.bankservice.BosService;
import org.sandy.bankscraper.bankservice.HsbcService;
import org.sandy.bankscraper.bankservice.JupiterService;
import org.sandy.bankscraper.bankservice.LegalAndGeneralService;
import org.sandy.bankscraper.bankservice.SantanderService;
import org.sandy.bankscraper.bankservice.VirginMoneyService;
import org.sandy.bankscraper.service.LastpassService;
import org.sandy.bankscraper.service.WebDriverService;

public class Main {
	private static final String LASTPASS_USER = "FILLMEIN";
	private static BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	private static List<BankScraper> banks = new ArrayList<BankScraper>();

	public static void main(String... args) throws InterruptedException, IOException, ExecutionException {

		String lastpassPassword = getLastPassPassword(writer, reader);
		String lastpassOtp = getOtp("Lastpass", writer, reader);
		LastpassService lpService = new LastpassService(LASTPASS_USER, lastpassPassword, lastpassOtp);

		constructBanks(lpService);

		StringBuilder headerRow = new StringBuilder();
		StringBuilder dataRow = new StringBuilder();

		ExecutorService pool = Executors.newFixedThreadPool(WebDriverService.MAX_WEB_DRIVERS);
		List<Future<List<Account>>> list = new ArrayList<Future<List<Account>>>();
		for (BankScraper bank : banks) {
			Future<List<Account>> future = pool.submit(bank);
			list.add(future);
		}
		
		List<Account> accounts = new ArrayList<>();
		for (Future<List<Account>> future : list) {
			accounts.addAll(future.get());
		}
		
		Collections.sort(accounts, new AccountCompatator());
		
		for (Account a : accounts) {
			System.out.println("Name: " + a.getName() + ", Balance: " + a.getBalance());
			if (headerRow.length() > 0) {
				headerRow.append(",");
				dataRow.append(",");
			}
			headerRow.append(a.getName());
			dataRow.append(a.getBalance());
		}

		System.out.println(headerRow.toString());
		System.out.println(dataRow.toString());

		pool.shutdownNow();
		
		WebDriverService.closeAllDrivers();
		
	}

	private static String getOtp(String service, BufferedWriter writer, BufferedReader reader) throws IOException {
		writer.write(service + " otp:");
		writer.flush();
		String otp = reader.readLine();
		return otp;
	}

	private static String getLastPassPassword(BufferedWriter writer, BufferedReader reader) throws IOException {
		writer.write("Lastpass Pw:");
		writer.flush();
		String lpass = reader.readLine();
		return lpass;
	}

	private static void constructBanks(LastpassService lpService) throws IOException {
		String hsbcOtp = getOtp("HSBC", writer, reader);
		HsbcService hsbc = new HsbcService(lpService.getUserForDomain("hsbc.co.uk"),
				lpService.getPassForDomain("hsbc.co.uk"), hsbcOtp);
		BosService bos = new BosService(lpService.getUserForDomain("bankofscotland.co.uk"),
				lpService.getPassForDomain("bankofscotland.co.uk"), lpService.getPassForDomain("bos.com"));
		SantanderService santander = new SantanderService(lpService.getUserForDomain("retail.santander.co.uk"),
				lpService.getPassForDomain("retail.santander.co.uk"));
		JupiterService jupiter = new JupiterService(lpService.getUserForDomain("myaccountonline.co.uk"),
				lpService.getPassForDomain("myaccountonline.co.uk"));
		VirginMoneyService virgin = new VirginMoneyService(lpService.getUserForDomain("uk.virginmoney.com"),
				lpService.getPassForDomain("uk.virginmoney.com"),
				lpService.getPassForDomain("virginsecurityquestion.com"));
		AvivaService aviva = new AvivaService(lpService.getUserForDomain("direct.aviva.co.uk"),
				lpService.getPassForDomain("direct.aviva.co.uk"));
		LegalAndGeneralService landg = new LegalAndGeneralService(lpService.getUserForDomain("www10.landg.com"),
				lpService.getPassForDomain("www10.landg.com"));

		banks.add(hsbc);
		banks.add(bos);
		banks.add(santander);
		banks.add(jupiter);
		banks.add(virgin);
		banks.add(aviva);		
		banks.add(landg);
}

}
