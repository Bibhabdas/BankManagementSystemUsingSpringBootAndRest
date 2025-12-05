package com.bank.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomerDetailsExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException invalidData) {
		Map<String, String> invalidmsg=new HashMap<String, String>();
		invalidData.getBindingResult().getFieldErrors().forEach(error->{
			invalidmsg.put(error.getField(), error.getDefaultMessage());
		});
		
		return new ResponseEntity<>(invalidmsg,HttpStatus.BAD_REQUEST);
	}
}
