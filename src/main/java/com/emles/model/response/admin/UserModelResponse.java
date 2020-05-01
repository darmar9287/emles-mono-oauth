package com.emles.model.response.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.emles.dto.UserDataDTO;
import com.emles.model.response.AuthorityResponse;
import com.emles.model.response.RoleResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModelResponse {
	private Long userId;

	private UserDataDTO userData;

	private Boolean enabled;

	private Date lastAccountUpdateDate;

	private Set<AuthorityResponse> authorities = new HashSet<>();

	private Set<RoleResponse> roles = new HashSet<>();
}
