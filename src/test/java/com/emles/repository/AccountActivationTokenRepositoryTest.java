package com.emles.repository;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.emles.model.AccountActivationToken;
import com.emles.model.AppUser;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(locations = "classpath:application-repository-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountActivationTokenRepositoryTest {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AccountActivationTokenRepository accountActivationTokenRepository;
	
	private AppUser appUser;
	
	private AccountActivationToken accountActivationToken;
	
	@BeforeEach
	public void setUp() {
		appUser = new AppUser();
		appUser.setAuthorities(new HashSet<>());
		appUser.setRoles(new HashSet<>());
		String email = "test@test.com";
		appUser.setEmail(email );
		appUser.setEnabled(true);
		appUser.setLastPasswordResetDate(Date.from(Instant.now()));
		String password = "password";
		appUser.setPassword(password );
		appUser = userRepository.save(appUser);
		
		accountActivationToken = new AccountActivationToken();
		accountActivationToken.setToken("activationToken");
		accountActivationToken.setUser(appUser);
	}
	
	@Test
	public void testSaveActivationToken() {
		AccountActivationToken savedToken = accountActivationTokenRepository.save(accountActivationToken);
		assertNotNull(savedToken.getId());
		assertEquals(accountActivationToken.getToken(), savedToken.getToken());
		assertEquals(accountActivationToken.getUser(), savedToken.getUser());
		
		AccountActivationToken found = accountActivationTokenRepository.findByToken(accountActivationToken.getToken());
		assertEquals(savedToken, found);
		found = accountActivationTokenRepository.findByUser(appUser);
		assertEquals(savedToken, found);
	}
	
	@Test
	public void testSaveActivationTokenWithNullUserShouldThrowException() {
		accountActivationToken.setUser(null);
		assertThrows(ConstraintViolationException.class, () -> {
			accountActivationTokenRepository.save(accountActivationToken);
		});
	}
	
	@Test
	public void testSaveActivationTokenWithNullTokenStringShouldThrowException() {
		accountActivationToken.setToken(null);
		assertThrows(ConstraintViolationException.class, () -> {
			accountActivationTokenRepository.save(accountActivationToken);
		});
	}
}
