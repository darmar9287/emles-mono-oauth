package com.emles.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing account activation token. It is stored in DB when user signs up to the page.
 * @author Dariusz Kulig
 *
 */
@Entity
@Data
@NoArgsConstructor
public class AccountActivationToken {

	/**
	 * id - token id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_activation_token_id")
	private Long id;
	
	/**
	 * token - generated random string value used to reset the password.
	 */
	@Column(name = "token", nullable = false)
	@NotNull(message = "token cannot be empty.")
	private String token;

	/**
	 * user - user which signed up to the page.
	 */
	@OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "app_user_id")
	@NotNull(message = "app_user_id cannot be empty.")
	private AppUser user;
	
	@Override
	public String toString() {
		return "token: " + token;
	}
}
