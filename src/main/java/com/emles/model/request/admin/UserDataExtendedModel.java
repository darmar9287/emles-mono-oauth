package com.emles.model.request.admin;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.emles.model.request.UserDataRequestModel;
import com.emles.utils.ValidationErrorMessages;

/**
 * Extension of UserDataRequest. Should be used only by admin endpoints.
 * 
 * @author darglk
 *
 */
public class UserDataExtendedModel extends UserDataRequestModel {

	/**
	 * enabled - boolean field determining if user account should be
	 * enabled/disabled.
	 */
	@NotNull(message = ValidationErrorMessages.USER_ENABLED_NOT_EMPTY_MESSAGE)
	private Boolean enabled;

	/**
	 * authorityIds - IDs of authorities for a given user.
	 */
	@NotNull(message = ValidationErrorMessages.AUTHORITY_IDS_NOT_NULL_MESSAGE)
	private Set<Long> authorityIds;

	/**
	 * roleIds - IDs of roles for a given user.
	 */
	@NotNull(message = ValidationErrorMessages.ROLE_IDS_NOT_NULL_MESSAGE)
	private Set<Long> roleIds;

	public UserDataExtendedModel(String email, Boolean enabled, Set<Long> authorityIds,
			Set<Long> roleIds) {
		super(email);
		this.enabled = enabled;
		this.authorityIds = authorityIds;
		this.roleIds = roleIds;
	}

	public UserDataExtendedModel() {
		super();
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Set<Long> getAuthorityIds() {
		return authorityIds;
	}

	public void setAuthorityIds(Set<Long> authorityIds) {
		this.authorityIds = authorityIds;
	}

	public Set<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(Set<Long> roleIds) {
		this.roleIds = roleIds;
	}
}
