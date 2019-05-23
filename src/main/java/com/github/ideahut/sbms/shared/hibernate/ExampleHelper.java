package com.github.ideahut.sbms.shared.hibernate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

import org.hibernate.criterion.Example;

public class ExampleHelper {
	
	private static final Map<Class<?>, Set<String>> mapNotNullableString = new HashMap<Class<?>, Set<String>>(); 
	
	private ExampleHelper() {}
	
	public static void initializeEntityField(Class<?> entityClass) {
		if (mapNotNullableString.containsKey(entityClass)) {
			return;
		}
		Method[] mtd = entityClass.getMethods();
		for (Method m : mtd) {
			if (m.getReturnType().equals(String.class) && m.getName().startsWith("get")) {
				Column column = m.getAnnotation(Column.class);
				if (column == null) { // || column.nullable() == true) {
					continue;
				}				
				String name = m.getName().substring(3);
				if (!hasInputStringMethod(entityClass, "set" + name)) {
					continue;
				}
				Set<String> set = mapNotNullableString.get(entityClass);
				if (set == null) {
					set = new HashSet<String>();
					mapNotNullableString.put(entityClass, set);
				}				
				set.add(name);
			}
		}
	}
	
	public static Example excludeProperties(Example ex, String... excludedProperties) {
		if (excludedProperties != null) {
			for (String property : excludedProperties) {
				ex.excludeProperty(property);
			}
		}
		return ex;
	}

	public static Example createExample(Object example, String... excludedProperties) {
		checkNotNullableString(example);
		return excludeProperties(Example.create(example), excludedProperties);
	}

	public static String[] stripProperties(String root, String[] prop) {
		List<String> list = new LinkedList<String>();
		String path = root + ".";
		for (String p : prop) {
			int idx = p.indexOf(path);
			if (0 <= idx)
				list.add(p.substring(idx + path.length()));
		}
		return list.toArray(new String[] {});
	}
	
	private static boolean hasInputStringMethod(Class<?> entityClass, String name) {
		try {
			entityClass.getMethod(name, String.class);
			return true;
		} catch (Exception e) { }
		return false;
	}
	
	private static void checkNotNullableString(Object example) {
		Class<?> clazz = example.getClass();
		Set<String> set = mapNotNullableString.get(clazz);
		if (set == null) {
			return;
		}
		try {
			for (String name : set) {
				Method mGet = clazz.getMethod("get" + name);
				String str = (String)mGet.invoke(example);
				if (str != null && str.trim().length() == 0) {
					Method mSet = clazz.getMethod("set" + name, String.class);
					mSet.invoke(example, (String)null);
				}
			}
		} catch (Exception e) {
			
		}
	}
}
