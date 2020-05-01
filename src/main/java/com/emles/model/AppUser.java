package com.emles.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.emles.model.embedded.UserData;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity class.
 * 
 * @author darglk
 *
 */
@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
public class AppUser {

	/**
	 * id - user ID in DB.
	 */
	@Id
	@Column(name = "app_user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * userData - user specific data wrapper.
	 */
	@Embedded
	private UserData userData;

	/**
	 * enabled - field used for checking if user account is enabled.
	 */
	@Column(name = "enabled", nullable = false)
	private Boolean enabled;

	/**
	 * password - field containing hashed password.
	 */
	@Column(name = "password", nullable = false)
	private String password;
	
	/**
	 * @Optional field used for storing last sign in IP address.
	 */
	@Column(name = "last_sign_in_ip", nullable = true)
	private String lastSignInIp;

	/**
	 * @Optional field used for storing last sign in date.
	 */
	@Column(name = "last_sign_in_date", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastSignInDate;

	/**
	 * Set containing user authorities. Check AuthorityName enum for authorities
	 * defined in this application.
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "app_user_authority", joinColumns = {
			@JoinColumn(name = "app_user_id", referencedColumnName = "app_user_id") }, inverseJoinColumns = {
					@JoinColumn(name = "authority_id", referencedColumnName = "authority_id") })
	private Set<Authority> authorities = new HashSet<>();

	/**
	 * Set containing user roles. Check RoleName enum for roles defined in this
	 * application.
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "app_user_roles", joinColumns = {
			@JoinColumn(name = "app_user_id", referencedColumnName = "app_user_id") }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", referencedColumnName = "role_id") })
	private Set<Role> roles = new HashSet<>();

	/**
	 * lastAccountUpdateDate - field containing last account update date. It is used
	 * to validate in JwtAuthorizationFilter to check JWT validity.
	 */
	@Column(name = "last_password_reset_date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastPasswordResetDate;

	public String getEmail() {
		return userData != null ? userData.getEmail() : null;
	}

	public void setEmail(String email) {
		if (this.userData == null) {
			this.userData = new UserData();
		}
		this.userData.setEmail(email);
	}

	/**
	 * Method used to fetch all authorities and roles for given instance and convert
	 * them to SimpleGrantedAuthority instances.
	 * 
	 * @return list of GrantedAuthorities for given instance.
	 */
	public List<SimpleGrantedAuthority> getAllAuthorities() {
		List<SimpleGrantedAuthority> auths = this.getRoles().stream()
				.map(r -> new SimpleGrantedAuthority(r.getAuthority())).collect(Collectors.toList());
		auths.addAll(this.getAuthorities().stream().map(a -> new SimpleGrantedAuthority(a.getAuthority()))
				.collect(Collectors.toList()));
		return auths;
	}

	/**
	 * Method used to fetch all authorities and roles for given instance and
	 * retrieving their names to String values.
	 * 
	 * @return list of names of roles and authorities for given instance.
	 */
	public List<String> getAllAuthoritiesAsStrings() {
		List<String> auths = this.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList());
		auths.addAll(this.getAuthorities().stream().map(a -> a.getName().name()).collect(Collectors.toList()));
		return auths;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppUser other = (AppUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}