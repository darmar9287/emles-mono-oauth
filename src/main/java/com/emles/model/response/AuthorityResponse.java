package com.emles.model.response;

import com.emles.model.AuthorityName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthorityResponse {
	private Long id;
	private AuthorityName name;
}
