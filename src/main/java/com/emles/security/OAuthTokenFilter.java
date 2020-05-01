package com.emles.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;

public class OAuthTokenFilter extends OAuth2AuthenticationProcessingFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("Here goes token processing filter");
		super.doFilter(req, res, chain);
	}
	
//	public OAuthTokenFilter() {
//		super();
//	}
}
