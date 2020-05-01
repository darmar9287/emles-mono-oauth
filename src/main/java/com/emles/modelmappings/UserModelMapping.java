package com.emles.modelmappings;

import com.emles.dto.UserDTO;
import com.emles.dto.UserDataDTO;
import com.emles.model.AppUser;
import com.emles.model.response.UserDataResponse;

public class UserModelMapping {

	public static UserDTO of(AppUser user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setUserId(user.getId());
		UserDataDTO userDataDTO = new UserDataDTO();
		userDataDTO.setEmail(user.getEmail());
		userDTO.setUserData(userDataDTO);
		return userDTO;
	}

	public static UserDataResponse res(UserDTO user) {
		UserDataResponse response = new UserDataResponse();
		response.setEmail(user.getUserData().getEmail());
		return response;
	}
}
