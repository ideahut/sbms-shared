package com.github.ideahut.sbms.shared.helper.impl;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;
import com.github.ideahut.sbms.shared.helper.MessageHelper;

public class MessageHelperImpl implements MessageHelper, InitializingBean {
	
	private MessageSource messageSource;
	
	private LocaleResolver localeResolver;
	
	private boolean checkArguments = false;
	
	// Untuk service exporter yang tidak memiliki HttpServletRequest seperti RMI
	private List<Locale> supportedLocales;	
	private Locale defaultLocale;
	
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}
	
	public void setCheckArguments(boolean checkArguments) {
		this.checkArguments = checkArguments;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		if (localeResolver != null) {
			Class<?> resolverClass = localeResolver.getClass();
			try {
				supportedLocales = (List<Locale>)resolverClass.getMethod("getSupportedLocales").invoke(localeResolver);
			} catch (Exception e) {}
			try {
				defaultLocale = (Locale)resolverClass.getMethod("getDefaultLocale").invoke(localeResolver);
			} catch (Exception e) {}			
		}
	}

	@Override
	public Locale getLocale() {
		HttpServletRequest request = getRequest();
		if (request != null) {
			return localeResolver.resolveLocale(getRequest());
		}
		Locale primaryLocale = this.defaultLocale;
		if (primaryLocale == null) {
			primaryLocale = Locale.getDefault();
		}
		String language = MessageHelper.LanguageHolder.get();
		if (language == null || language.isEmpty()) {
			return primaryLocale;
		}
		Locale locale = Locale.lookup(Locale.LanguageRange.parse(language), this.supportedLocales);
		return locale != null ? locale : primaryLocale;
	}
	
	@Override
	public String getMessage(String code, String... args) {
		Locale locale = getLocale();
		if (!checkArguments) {
			return messageSource.getMessage(code, args, locale);
		}
		String[] newArgs = new String[args.length];
		for (int i = 0; i < newArgs.length; i++) {
			newArgs[i] = messageSource.getMessage(args[i], null, locale);
		}
		return messageSource.getMessage(code, newArgs, locale);
	}

	@Override
	public CodeMessageDto getCodeMessage(String code, String... args) {
		String message = getMessage(code, args); 
		return new CodeMessageDto(code, message);
	}
	
	private HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			return ((ServletRequestAttributes) requestAttributes).getRequest();
		}
		return null;
	}	

}
