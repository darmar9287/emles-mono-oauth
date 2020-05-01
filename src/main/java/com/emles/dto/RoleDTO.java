package com.emles.dto;

import com.emles.model.RoleName;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role model Data Transfer Object.
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
public class RoleDTO {
	/**
	 * id - role ID
	 */
	private Long id;
	/**
	 * name - role name
	 */
	private RoleName name;
}
