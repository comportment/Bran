package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.utils.EmojiConverter;

public class EmojiInfoCommand {
	
	@Command
	private static ICommand emojiInfo() {
		return new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("emoji", "character")
				.setName("Emoji/Character Info Command")
				.setDescription("Returns emoji or character info.")
				.setArgs(new Argument<>("emoji/character", String.class))
				.setAction((event, rawArgs) -> {
					String unicode = EmojiConverter.toUnicode((String) event.getArgument("emoji/character").get());
					event.sendMessage("Emoji/Character information:\n" + unicode).queue();
				})
				.build();
	}
}
