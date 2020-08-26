package com.example.dto;

import java.time.LocalDateTime;

public class ErrorDto {
	private LocalDateTime timestamp;
	private String requestInfo;
	private Object error;

	public ErrorDto(LocalDateTime timestamp, Object error, String requestInfo) {
		this.timestamp = timestamp;
		this.error = error;
		this.requestInfo = requestInfo;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getRequestInfo() {
		return requestInfo;
	}

	public Object getError() {
		return error;
	}
}
