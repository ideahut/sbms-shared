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
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;

public class MessageHelperImpl implements MessageHelper, InitializingBean {
	
	private MessageSource messageSource;
	
	private LocaleResolver localeResolver;
	
	// Untuk service exporter yang tidak memiliki HttpServletRequest seperti RMI
	private List<Locale> supportedLocales;	
	private Locale defaultLocale;
	
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
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
			return localeResolver.resolveLocale(request);
		}
		Locale primaryLocale = this.defaultLocale;
		if (primaryLocale == null) {
			primaryLocale = Locale.getDefault();
		}
		MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
		String language = momentAttributes != null ? momentAttributes.getLanguage() : null;
		if (language == null || language.isEmpty()) {
			return primaryLocale;
		}
		Locale locale = Locale.lookup(Locale.LanguageRange.parse(language), this.supportedLocales);
		return locale != null ? locale : primaryLocale;
	}
	
	@Override
	public String getMessage(String code, boolean checkArgs, String... args) {
		Locale locale = getLocale();
		if (!checkArgs) {
			return messageSource.getMessage(code, args, locale);
		}
		String[] newArgs = new String[args.length];
		for (int i = 0; i < newArgs.length; i++) {
			newArgs[i] = messageSource.getMessage(args[i], null, locale);
		}
		return messageSource.getMessage(code, newArgs, locale);
	}
	
	@Override
	public String getMessage(String code, String... args) {
		return getMessage(code, false, args);
	}
	
	@Override
	public CodeMessageDto getCodeMessage(String code, boolean checkArgs, String... args) {
		String message = getMessage(code, checkArgs, args); 
		return new CodeMessageDto(code, message);
	}

	@Override
	public CodeMessageDto getCodeMessage(String code, String... args) {
		return getCodeMessage(code, false, args);
	}
	
	
	private HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			return ((ServletRequestAttributes) requestAttributes).getRequest();
		}
		return null;
	}	

}
