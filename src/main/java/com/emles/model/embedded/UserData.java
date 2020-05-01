package com.emles.model.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for AppUser entity containing user specific data. Add more fields
 * when you want your users to contain more account data.
 * 
 * @author darglk
 *
 */
@Embeddable
@Data
@NoArgsConstructor
public class UserData {

	/**
	 * email - user email address (also used in this application as login).
	 */
	@Column(name = "email", unique = true, nullable = false)
	@NotNull(message = "email cannot be empty.")
	@Email(message = "invalid email address.")
	private String email;
}
