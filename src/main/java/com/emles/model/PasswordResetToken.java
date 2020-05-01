package com.emles.model;
import java.util.Date;

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
 * Model representing password reset token. It is stored in DB when user sends forgot_password request to the endpoint.
 * @author Dariusz Kulig
 *
 */
@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

	/**
	 * EXPIRATION - token expiration in minutes.
	 */
	private static final int EXPIRATION = 60 * 24;

	/**
	 * id - token id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "password_reset_token_id")
	private Long id;
	
	/**
	 * token - generated random string value used to reset the password.
	 */
	@Column(name = "token", nullable = false)
	@NotNull(message = "token cannot be empty.")
	private String token;

	/**
	 * user - user which requested password reset token.
	 */
	@OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "app_user_id")
	@NotNull(message = "app_user_id cannot be empty.")
	private AppUser user;

	/**
	 * expiryDate - date of reset token expiration.
	 */
	@Column(name = "expiry_date", nullable = false)
	@NotNull(message = "expiry_date cannot be empty.")
	private Date expiryDate;
	
	/**
	 * Getter for EXPIRATION constant.
	 * @return EXPIRATION value.
	 */
	public static int getExpiration() {
		return EXPIRATION;
	}
	
	@Override
	public String toString() {
		return "token: " + token + " expiryDate: " + expiryDate;
	}
}