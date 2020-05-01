package com.emles.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emles.model.Authority;
import com.emles.model.AuthorityName;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
	public Authority findByName(AuthorityName authorityName);
}
