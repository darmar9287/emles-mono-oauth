package com.emles.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emles.model.Role;
import com.emles.model.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {
	public Role findByName(RoleName roleName);
}
