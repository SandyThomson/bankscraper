package org.sandy.bankscraper.service;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import com.nhinds.lastpass.GoogleAuthenticatorRequired;
import com.nhinds.lastpass.LastPass;
import com.nhinds.lastpass.PasswordInfo;
import com.nhinds.lastpass.LastPass.PasswordStoreBuilder;
import com.nhinds.lastpass.PasswordStore;
import com.nhinds.lastpass.impl.LastPassImpl;
import com.nhinds.lastpass.impl.NullCacheProvider;

public class LastpassService {

	private PasswordStore store;

	public LastpassService(String user, String pass, String otp) {
		PasswordStoreBuilder builder = lastPass.getPasswordStoreBuilder(user, pass, "device");
		try {
			store = builder.getPasswordStore(null);
		} catch (GoogleAuthenticatorRequired req) {
			// Prompt user for one-time password
			store = builder.getPasswordStore(otp, "label", null);
		}
	}

	LastPass lastPass = new LastPassImpl(new NullCacheProvider());

	public String getPassForDomain(String domain) {
		Collection<PasswordInfo> passwordsByHostname = store.getPasswordsByHostname(domain);
		assertEquals(1, passwordsByHostname.size());
		String pass = null;
		for (PasswordInfo pw : passwordsByHostname) {
			pass = pw.getPassword();
		}

		return pass;

	}

	public String getUserForDomain(String domain) {
		Collection<PasswordInfo> passwordsByHostname = store.getPasswordsByHostname(domain);
		assertEquals(1, passwordsByHostname.size());
		String user = null;
		for (PasswordInfo pw : passwordsByHostname) {
			user = pw.getUsername();
		}

		return user;
	}

}
