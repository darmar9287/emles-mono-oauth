package com.emles.controller.admin;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.emles.dto.UserDTO;
import com.emles.service.UserService;

@RestController
@RequestMapping("/api/admin/approvals")
public class ApprovalAdminController {
	@Autowired
	private JdbcClientDetailsService clientDetailsService;

	@Autowired
	private ApprovalStore approvalStore;

	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/list/{userId}", method = RequestMethod.GET)
	public ResponseEntity<List<Approval>> listUserApprovals(@PathVariable("userId") Long userId) {
		UserDTO userDto = userService.findUserById(userId);
		List<Approval> approvals = clientDetailsService
				.listClientDetails().stream().map(clientDetails -> approvalStore
						.getApprovals(userDto.getUserData().getEmail(), clientDetails.getClientId()))
				.flatMap(Collection::stream).collect(Collectors.toList());
		return ResponseEntity.ok().body(approvals);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<?> revokeApproval(@RequestBody Approval approval) {
		approvalStore.revokeApprovals(Arrays.asList(approval));
		tokenStore.findTokensByClientIdAndUserName(approval.getClientId(), approval.getUserId())
				.forEach(tokenStore::removeAccessToken);
		return ResponseEntity.noContent().build();
	}
}
