package br.net.brjdevs.steven.bran.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Argument {
	
	private static final Pattern ARG_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");
	private final String name;
	private final Class<?> type;
	private Object result;
	private boolean isOptional;
	
	public Argument(String name, Class<?> type, boolean isOptional) {
		this.name = name;
		this.type = type;
		this.isOptional = isOptional;
		this.result = null;
	}
	
	public Argument(String name, Class<?> type) {
		this(name, type, false);
	}
	
	public static String[] split(String input, int size) {
		input = input.trim().replaceAll("  ", " ");
		List<String> results = new ArrayList<>();
		Matcher matcher = ARG_PATTERN.matcher(input);
		while (matcher.find()) {
			if (results.size() >= size) {
				String s = input.substring(String.join(" ", results).length());
				if (s.length() > 3 && results.get(results.size() - 1).contains(" ")) s = s.substring(3);
				results.add(s);
				break;
			}
			if (matcher.group(1) != null) {
				results.add(matcher.group(1).trim());
			} else if (matcher.group(2) != null) {
				results.add(matcher.group(2).trim());
			} else {
				results.add(matcher.group().trim());
			}
		}
		return results.toArray(new String[0]);
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void parse(String input) throws ArgumentParsingException {
		if (input == null || input.isEmpty()) {
			result = null;
			return;
		}
		Object obj;
		switch (type.getSimpleName()) {
			case "String":
				obj = input;
				break;
			case "Integer":
				if (!input.matches("^(\\+|-)?[0-9]+$"))
					throw new ArgumentParsingException(type.getSimpleName(), input);
				obj = Integer.parseInt(input);
				break;
			case "Boolean":
				if (!input.matches("^(t|true|y|yes|f|false|n|no)"))
					throw new ArgumentParsingException(type.getSimpleName(), input);
				obj = input.matches("^(t|true|y|yes)$");
				break;
			default:
				throw new ArgumentParsingException(type);
		}
		result = type.cast(obj);
	}
	
	public Object get() {
		return result;
	}
	
	public Object getOrDefault(Object d) {
		return result == null ? d : result;
	}
	
	Argument copy() {
		return new Argument(name, type, isOptional);
	}
	
	public boolean isPresent() {
		return result != null;
	}
}