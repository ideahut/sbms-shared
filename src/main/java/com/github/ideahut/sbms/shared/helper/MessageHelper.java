package com.github.ideahut.sbms.shared.helper;

import java.util.Locale;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;

public interface MessageHelper {

	public Locale getLocale();
	
	public String getMessage(String code, boolean checkArgs, String...args);
	
	public String getMessage(String code, String...args);
	
	public CodeMessageDto getCodeMessage(String code, boolean checkArgs, String...args);
	
	public CodeMessageDto getCodeMessage(String code, String...args);	
	
}
