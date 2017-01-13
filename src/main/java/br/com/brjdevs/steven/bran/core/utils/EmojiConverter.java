package br.com.brjdevs.steven.bran.core.utils;

public class EmojiConverter {
	public static String toUnicode(String emoji) {
		StringBuilder builder = new StringBuilder();
		emoji.codePoints().forEachOrdered(code -> {
			char[] chrs = Character.toChars(code);
			String hex = Integer.toHexString(code);
			while (hex.length() < 4)
				hex = "0" + hex;
			builder.append("\n`\\u").append(hex).append("`   ");
			if(chrs.length>1)
			{
				String hex1 = Integer.toHexString(chrs[0]).toUpperCase();
				String hex2 = Integer.toHexString(chrs[1]).toUpperCase();
				while(hex1.length()<4)
					hex1 = "0" + hex1;
				while(hex2.length()<4)
					hex2 = "0" + hex2;
				builder.append("[`\\u").append(hex1).append("\\u").append(hex2).append("`]   ");
			}
			builder.append(String.valueOf(chrs)).append("   _").append(Character.getName(code)).append("_");
			
		});
		return builder.toString();
	}
	public static String toSimpleUnicode(String emoji) {
		StringBuilder builder = new StringBuilder();
		emoji.codePoints().forEachOrdered(code -> {
			char[] chrs = Character.toChars(code);
			if(chrs.length>1)
			{
				String hex1 = Integer.toHexString(chrs[0]).toUpperCase();
				String hex2 = Integer.toHexString(chrs[1]).toUpperCase();
				while (hex1.length() < 4)
					hex1 = "0" + hex1;
				while (hex2.length() < 4)
					hex2 = "0" + hex2;
				builder.append("\\u").append(hex1).append("\\u").append(hex2);
			} else {
				String hex = Integer.toHexString(code);
				while (hex.length() < 4)
					hex = "0" + hex;
				builder.append("\\u").append(hex);
			}
			//builder.append(String.valueOf(chrs));
			
		});
		return builder.toString();
	}
	
}
