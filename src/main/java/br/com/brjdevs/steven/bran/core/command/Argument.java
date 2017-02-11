package br.com.brjdevs.steven.bran.core.command;

import java.util.regex.Pattern;

public class Argument<T> {
	
	private static final Pattern ARG_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");
	private final String name;
	private final Class<T> type;
	private T result;
	private boolean isOptional;
	
	public Argument(String name, Class<T> type, boolean isOptional) {
		this.name = name;
		this.type = type;
		this.isOptional = isOptional;
		this.result = null;
	}
	
	public Argument(String name, Class<T> type) {
		this(name, type, false);
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<T> getType() {
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
				if (!input.matches("^-?[0-9]+$"))
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
	
	public T get() {
		return result;
	}
	
	Argument copy() {
		return new Argument<>(name, type, isOptional);
	}
	
	public boolean isPresent() {
		return result != null;
	}
}