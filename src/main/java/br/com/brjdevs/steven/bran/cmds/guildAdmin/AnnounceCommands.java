package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import net.dv8tion.jda.core.entities.TextChannel;

public class AnnounceCommands {
	
	@Command
	private static ICommand greeting() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("greeting")
				.setName("Greeting Message Command")
				.setDescription("Sets, checks and disables the greeting message!")
				.setArgs(new Argument("msg/action", String.class, true))
				.setRequiredPermission(Permissions.ANNOUNCE)
				.setPrivateAvailable(false)
				.setAction((event) -> {
					Argument arg = event.getArgument("msg/action");
					GuildData data = event.getGuildData();
					if (!arg.isPresent()) {
						if (data.joinMsg == null)
							event.sendMessage(Emojis.X + " You didn't set a greeting message!").queue();
						else
							event.sendMessage(data.joinMsg).queue();
						return;
					} else if (arg.get().equals("clear") || arg.get().equals("none")) {
						if (data.joinMsg == null)
							event.sendMessage(Emojis.X + " There's no greeting message to clear!").queue();
						else {
							data.joinMsg = null;
							event.sendMessage(Quotes.SUCCESS, "Removed the greeting message!").queue();
						}
						return;
					}
					data.joinMsg = (String) arg.get();
					event.sendMessage(Quotes.SUCCESS, "Updated greeting message!").queue();
					Bran.getInstance().getDataManager().getDataHolderManager().update();
				})
				.build();
	}
	
	@Command
	private static ICommand farewell() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("farewell")
				.setName("Farewell Message Command")
				.setDescription("Sets, checks and disables the farewell message!")
				.setArgs(new Argument("msg/action", String.class, true))
				.setRequiredPermission(Permissions.ANNOUNCE)
				.setPrivateAvailable(false)
				.setAction((event) -> {
					Argument arg = event.getArgument("msg/action");
					GuildData data = event.getGuildData();
					if (!arg.isPresent()) {
						if (data.leaveMsg == null)
							event.sendMessage(Emojis.X + " You didn't set a farewell message!").queue();
						else
							event.sendMessage(data.leaveMsg).queue();
						return;
					} else if (arg.get().equals("clear") || arg.get().equals("none")) {
						if (data.leaveMsg == null)
							event.sendMessage(Emojis.X + " There's no farewell message to clear!").queue();
						else {
							data.leaveMsg = null;
							event.sendMessage(Quotes.SUCCESS, "Removed the farewell message!").queue();
						}
						return;
					}
					data.leaveMsg = (String) arg.get();
					event.sendMessage(Quotes.SUCCESS, "Updated farewell message!").queue();
					Bran.getInstance().getDataManager().getDataHolderManager().update();
				})
				.build();
	}
	
	@Command
	private static ICommand greetingDM() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("greetingdm")
				.setName("GreetingDM Message Command")
				.setDescription("Sets, checks and disables the greetingDM message!")
				.setArgs(new Argument("msg/action", String.class, true))
				.setRequiredPermission(Permissions.ANNOUNCE)
				.setPrivateAvailable(false)
				.setAction((event) -> {
					Argument arg = event.getArgument("msg/action");
					GuildData data = event.getGuildData();
					if (!arg.isPresent()) {
						if (data.joinMsgDM == null)
							event.sendMessage(Emojis.X + " You didn't set a greetingDM message!").queue();
						else
							event.sendMessage(data.joinMsgDM).queue();
						return;
					} else if (arg.get().equals("clear") || arg.get().equals("none")) {
						if (data.joinMsgDM == null)
							event.sendMessage(Emojis.X + " There's no greetingDM message to clear!").queue();
						else {
							data.joinMsgDM = null;
							event.sendMessage(Quotes.SUCCESS, "Removed the greetingDM message!").queue();
						}
						return;
					}
					data.joinMsgDM = (String) arg.get();
					event.sendMessage(Quotes.SUCCESS, "Updated greetingDM message!").queue();
					Bran.getInstance().getDataManager().getDataHolderManager().update();
				})
				.build();
	}
	
	@Command
	private static ICommand announceschannel() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("announcechannel", "achannel", "ac")
				.setName("Announce Channel Command")
				.setDescription("Sets the announce channel (greeting/farewell) for the current guild!")
				.setArgs(new Argument("channel/action", String.class))
				.setRequiredPermission(Permissions.ANNOUNCE)
				.setPrivateAvailable(false)
				.setAction((event) -> {
					String arg = ((String) event.getArgument("channel/action").get());
					if (arg.equals("none") || arg.equals("remove")) {
						event.getGuildData().setAnnounceTextChannel(null);
						event.sendMessage("Removed the Announce channel!").queue();
						return;
					}
					TextChannel channel = !event.getMessage().getMentionedChannels().isEmpty() ? event.getMessage().getMentionedChannels().get(0) : event.getTextChannel();
					if (channel == null) {
						event.sendMessage("No channels found matching that criteria.").queue();
						return;
					}
					event.getGuildData().setAnnounceTextChannel(channel);
					event.sendMessage("Done, set the announce channel to " + channel.getAsMention()).queue();
				})
				.build();
	}
}
