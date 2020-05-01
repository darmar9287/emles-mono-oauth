package com.emles.model.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ModelValidationError {
	private Map<String, List<String>> errors = new HashMap<>();
}
