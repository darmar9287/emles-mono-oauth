
package com.emles.controller;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ApprovalControllerTest {

	@Mock
	private SecurityContext context;

	@Mock
	private Authentication authentication;
	
	@Mock
	private User userMock;
	
	@Mock
	private JdbcClientDetailsService clientDetailsService;
	
	@Mock
	private ApprovalStore approvalStore;
	
	@Mock
	private TokenStore tokenStore;
	
	@Mock
	private ClientDetails clientDetails;
	
	@Mock
	private OAuth2AccessToken accessToken;

	private String clientId = "client_id";

	@Mock
	private OAuth2RefreshToken refreshToken;
	
	@Mock
	private Approval approval;
	
	@InjectMocks
	private ApprovalController approvalController;
	
	private String email = "test@test.com";
	
	@BeforeEach
	public void setUp() {
		lenient().when(context.getAuthentication()).thenReturn(authentication);
		lenient().when(authentication.getName()).thenReturn(email);
		lenient().when(authentication.getPrincipal()).thenReturn(userMock);
		SecurityContextHolder.setContext(context);
		
		List<ClientDetails> clientDetailsList = Arrays.asList(clientDetails);
		lenient().when(clientDetails.getClientId()).thenReturn(clientId);
		lenient().when(clientDetailsService.listClientDetails()).thenReturn(clientDetailsList);
		lenient().when(tokenStore.findTokensByClientIdAndUserName(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		lenient().when(accessToken.getRefreshToken()).thenReturn(refreshToken);
		lenient().when(approvalStore.getApprovals(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
	}
	
	@Test
	public void testFetchListOfApprovals() {
		when(approvalStore.getApprovals(email, clientId)).thenReturn(Arrays.asList(approval));
		
		ResponseEntity<List<Approval>> response = approvalController.myApprovals();
		assertEquals(response.getBody().size(), 1);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		
		verify(context, times(1)).getAuthentication();
		verify(authentication, times(1)).getName();
		verify(clientDetailsService, times(1)).listClientDetails();
		verify(approvalStore, times(1)).getApprovals(email, clientId);
	}
	
	@Test
	public void testRevokeMyApprovalShouldReturnNoContentWhenApprovalBelongsToUser() {
		when(approval.getUserId()).thenReturn(email);
		when(approval.getClientId()).thenReturn(clientId);
		ResponseEntity<?> response = approvalController.revokeMyApproval(approval);
		
		assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
		verify(context, times(1)).getAuthentication();
		verify(authentication, times(1)).getName();
		verify(approval, times(2)).getUserId();
		verify(approvalStore, times(1)).revokeApprovals(Mockito.any());
		verify(tokenStore, times(1)).findTokensByClientIdAndUserName(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void testRevokeMyApprovalShouldReturnNotFoundWhenApprovalBelongsToOtherUser() {
		when(approval.getUserId()).thenReturn("other");
		
		ResponseEntity<?> response = approvalController.revokeMyApproval(approval);
		
		assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
		verify(context, times(1)).getAuthentication();
		verify(authentication, times(1)).getName();
		verify(approval, times(1)).getUserId();
		verify(approvalStore, times(0)).revokeApprovals(Mockito.any());
		verify(tokenStore, times(0)).findTokensByClientIdAndUserName(Mockito.anyString(), Mockito.anyString());
	}
}
