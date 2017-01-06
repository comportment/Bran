package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.utils.EmojiConverter;
import br.com.brjdevs.bran.core.utils.StringUtils;

@RegisterCommand
public class EmojiInfoCommand {
	public EmojiInfoCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
				.setAliases("emoji", "character")
				.setName("Emoji/Character Info Command")
				.setDescription("Returns emoji or character info.")
				.setArgs("[characters/emojis]")
				.setAction((event, rawArgs) -> {
					String string = StringUtils.splitArgs(rawArgs, 2)[1];
					if (!event.getMessage().getEmotes().isEmpty()) {
						event.sendMessage("I think you meant `" + event.getPrefix() + "emote info :" + event.getMessage().getEmotes().get(0).getName() + ":`.").queue();
						return;
					}
					if (string.isEmpty()) {
						event.sendMessage("You didn't tell me a character/emoji! Please use `" + event.getPrefix() + "character [character]` or `emoji [emoji]`").queue();
						return;
					}
					String unicode = EmojiConverter.toUnicode(string);
					event.sendMessage("Emoji/Character information:\n" + unicode).queue();
				})
				.build());
	}
}
