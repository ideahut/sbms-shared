package com.github.ideahut.sbms.shared.helper;

import java.util.Locale;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;

public interface MessageHelper {

	public Locale getLocale();
	
	public String getMessage(String code, String...args);
	
	public CodeMessageDto getCodeMessage(String code, String...args);
	
	
	
	public static class LanguageHolder {
		private static final ThreadLocal<String> holder = new ThreadLocal<String>();
		private static final ThreadLocal<String> inheritableHolder = new InheritableThreadLocal<String>();
		
		public static void remove() {
			holder.remove();
			inheritableHolder.remove();
		}
		
		public static void set(String language, boolean inheritable) {
			if (language == null) {
				remove();
			} else {
				if (inheritable) {
					inheritableHolder.set(language);
					holder.remove();
				} else {
					holder.set(language);
					inheritableHolder.remove();
				}
			}
		}
		
		public static void set(String language) {
			set(language, false);
		}
		
		public static String get() {
			String language = holder.get();
			if (language == null) {
				language = inheritableHolder.get();
			}
			return language;
		}		
	}
	
}
