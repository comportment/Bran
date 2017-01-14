package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

public class ConfigCommand {
	
	@Command
	private static ICommand config() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("config", "cfg")
				.setName("Config Command")
				.setRequiredPermission(Permissions.GUILD_MANAGE)
				.setPrivateAvailable(false)
				.setExample("cfg max_musics_per_user 10")
				.setDescription("Manage extra options with this command!")
				.setHelp("cfg ?")
				.addSubCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("music")
						.setHelp("cfg music ?")
						.setName("Config Music Command")
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("max_songs_per_user")
								.setName("Config Music Command")
								.setDescription("Configures the maximum amount of songs per user in the queue.")
								.setArgs(new Argument<>("value", String.class, true))
								.setAction((event) -> {
									Argument argument = event.getArgument("value");
									if (!argument.isPresent()) {
										long current = event.getDiscordGuild().getMusicSettings().getMaxSongsPerUser();
										if (current > 0)
											event.sendMessage("The current maximum amount of songs per user in the queue is " + current).queue();
										else
											event.sendMessage("There is no limit of maximum amount of songs per user set in this guild").queue();
										return;
									}
									String value = (String) argument.get();
									if (!MathUtils.isInteger(value) && !"none".equals(value)) {
										event.sendMessage("`" + value + "` is not a valid number.").queue();
										return;
									}
									int i = !value.equals("none") ? Integer.parseInt(value) : -1;
									if (i < -1) i = -1;
									event.getDiscordGuild().getMusicSettings().setMaxSongsPerUser(i);
									if (i > 0)
										event.sendMessage("Got it! Now each user can only have " + i + " song(s) in the queue at once.").queue();
									else
										event.sendMessage("Got it! Now each user can have unlimited songs in the queue at once.").queue();
								})
								.build())
						.build())
				.build();
	}
}
