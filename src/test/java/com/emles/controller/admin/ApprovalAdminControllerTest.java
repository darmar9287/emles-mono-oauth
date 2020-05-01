package com.emles.controller.admin;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.emles.dto.UserDTO;
import com.emles.service.UserService;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ApprovalAdminControllerTest {

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
	private ApprovalAdminController approvalAdminController;
	
	@Mock
	private UserService userService;
	
	private String email = "test@test.com";
	
	private long userId = 1L;
	
	@Mock
	private UserDTO userDto;
	
	@BeforeEach
	public void setUp() {
		List<ClientDetails> clientDetailsList = Collections.emptyList();
		lenient().when(clientDetails.getClientId()).thenReturn(clientId);
		lenient().when(clientDetailsService.listClientDetails()).thenReturn(clientDetailsList);
		lenient().when(tokenStore.findTokensByClientIdAndUserName(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		lenient().when(accessToken.getRefreshToken()).thenReturn(refreshToken);
		lenient().when(approvalStore.getApprovals(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		
		lenient().when(userService.findUserById(userId)).thenReturn(userDto);
	}
	
	@Test
	public void testListUserApprovals() {
		ResponseEntity<List<Approval>> response = approvalAdminController.listUserApprovals(userId);
		assertTrue(response.getBody().isEmpty());
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		verify(userService, times(1)).findUserById(userId);
		verify(clientDetailsService, times(1)).listClientDetails();
	}
	
	@Test
	public void testRevokeApproval() {
		when(approval.getClientId()).thenReturn(clientId);
		when(approval.getUserId()).thenReturn(email);
		
		ResponseEntity<?> response = approvalAdminController.revokeApproval(approval);
		
		assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
		verify(approvalStore, times(1)).revokeApprovals(Mockito.any());
		verify(tokenStore, times(1)).findTokensByClientIdAndUserName(clientId, email);
		verify(approval, times(1)).getClientId();
		verify(approval, times(1)).getUserId();
	}
}
