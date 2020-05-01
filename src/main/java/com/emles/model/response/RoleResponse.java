package com.emles.model.response;

import com.emles.model.RoleName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
	private Long id;
	private RoleName name;
}
