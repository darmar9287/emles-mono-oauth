package com.emles.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.emles.model.Authority;
import com.emles.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AppUser Data Transfer Object
 * 
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
public class UserDTO {

	/**
	 * userId - user ID
	 */
	private Long userId;

	/**
	 * userData - user account specific data
	 */
	private UserDataDTO userData;

	/**
	 * enabled - field which determines if user account is enabled.
	 */
	private Boolean enabled;

	/**
	 * password - user password
	 */
	private String password;

	/**
	 * getLastAccountUpdateDate - date of last account update date.
	 */
	private Date lastAccountUpdateDate;

	/**
	 * authorities - set of authorities for given account.
	 */
	private Set<Authority> authorities = new HashSet<>();

	/**
	 * roles - set of roles for given account.
	 */
	private Set<Role> roles = new HashSet<>();
}
