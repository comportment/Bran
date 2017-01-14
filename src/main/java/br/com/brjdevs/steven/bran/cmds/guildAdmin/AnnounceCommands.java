package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.data.guild.configs.Announces;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.entities.TextChannel;

public class AnnounceCommands {
	
	@Command
	private static ICommand announce() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("announce", "ann")
				.setName("Announce Command")
				.setDefault("list")
				.setDescription("Why not be gentile? Welcome people using this command!")
				.setExample("announce set join Welcome to $guild, $user!")
				.setHelp("ann ?")
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.ANNOUNCE)
				.addSubCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("set")
						.setHelp("ann set ?")
						.setName("Set Announces Command")
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("channel")
								.setName("Set Channel Announce Command")
								.setDescription("Sets the Announces Channel.")
								.setArgs(new Argument<>("channel", String.class, true))
								.setPrivateAvailable(false)
								.setAction((event) -> {
									TextChannel channel = event.getMessage().getMentionedChannels().isEmpty() ? event.getTextChannel() : event.getMessage().getMentionedChannels().get(0);
									event.getDiscordGuild().getAnnounces().setAnnouncesChannel(channel);
									event.sendMessage(Quotes.SUCCESS, "Now the Announces will be sent in " + channel.getAsMention() + "!").queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("join")
								.setName("Set Join Announce Command")
								.setDescription("Sets the Join Message Announce.")
								.setArgs(new Argument<>("joinmsg", String.class))
								.setExample("announce set join Welcome to $guild, $user!")
								.setPrivateAvailable(false)
								.setAction((event, args) -> {
									String message = (String) event.getArgument("joinmsg").get();
									event.getDiscordGuild().getAnnounces().setJoinAnnounce(message);
									event.sendMessage(Quotes.SUCCESS, "Successfully set the Join Announce Message. " +
											"When someone joins this guild, it'll look like this:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("joindm")
								.setName("Set JoinDM Announce Command")
								.setDescription("Sets the Join DM Announce message.")
								.setArgs(new Argument<>("joindmmsg", String.class))
								.setExample("announce set joindm Hello, thank you for joining $guild!")
								.setPrivateAvailable(false)
								.setAction((event, args) -> {
									String message = (String) event.getArgument("joindmmsg").get();
									event.getDiscordGuild().getAnnounces().setJoinDMAnnounce(message);
									event.sendMessage(Quotes.SUCCESS, "Successfully set the Join DM Announce Message. " +
											"When someone joins this guild, it'll receive this message:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("leave")
								.setName("Set Leave Announce Command")
								.setDescription("Sets the Leave Announce message.")
								.setArgs(new Argument<>("leavemsg", String.class))
								.setExample("announce set leave Goodbye $user, we won't miss you!")
								.setPrivateAvailable(false)
								.setAction((event, args) -> {
									String message = (String) event.getArgument("leavemsg").get();
									event.getDiscordGuild().getAnnounces().setLeaveAnnounce(message);
									event.sendMessage(Quotes.SUCCESS, "Successfully set the Leave Announce Message. " +
											"When someone leaves this guild, it'll look like this:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
								})
								.build())
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("preview")
						.setName("Announce Preview Command")
						.setHelp("ann preview ?")
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("channel")
								.setDescription("Returns you the Announce Channel.")
								.setName("Announce Preview Channel Command")
								.setAction((event) -> {
									if (event.getDiscordGuild().getAnnounces().getChannel(event.getJDA()) == null) {
										event.sendMessage(Quotes.FAIL, "The Announce Channel is not set, please use `" + event.getPrefix() + "ann set channel [MENTION]` to set one.").queue();
										return;
									}
									event.sendMessage("The Announces will be sent in " + event.getDiscordGuild().getAnnounces().getChannel(event.getJDA()).getAsMention() + ".").queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("join")
								.setDescription("Returns you the Join Announce Message.")
								.setName("Join Announce Preview Command")
								.setPrivateAvailable(false)
								.setAction((event) -> {
									if (Util.isEmpty(event.getDiscordGuild().getAnnounces().getJoinAnnounce())) {
										event.sendMessage(Quotes.FAIL, "The Join Announce is not set! Please use `" + event.getPrefix() + "ann set join [MESSAGE]` to set it.").queue();
										return;
									}
									String s = "The Join Announce Message will look like this when an user joins here:";
									s += "\n\n" + Announces.parse(event.getDiscordGuild().getAnnounces().getJoinAnnounce(), event.getOriginMember());
									event.sendMessage(s).queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("leave")
								.setDescription("Returns you the Leave Announce Message.")
								.setName("Leave Announce Preview Command")
								.setPrivateAvailable(false)
								.setAction((event) -> {
									if (Util.isEmpty(event.getDiscordGuild().getAnnounces().getLeaveAnnounce())) {
										event.sendMessage(Quotes.FAIL, "The Leave Announce is not set! Please use `" + event.getPrefix() + "ann set leave [MESSAGE]` to set it.").queue();
										return;
									}
									String s = "The Leave Announce Message will look like this when an user joins here:";
									s += "\n\n" + Announces.parse(event.getDiscordGuild().getAnnounces().getLeaveAnnounce(), event.getOriginMember());
									event.sendMessage(s).queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("joindm")
								.setDescription("Returns you the Join DM Announce Message.")
								.setName("JoinDM Announce Preview Command")
								.setPrivateAvailable(false)
								.setAction((event) -> {
									if (Util.isEmpty(event.getDiscordGuild().getAnnounces().getJoinDMAnnounce())) {
										event.sendMessage(Quotes.FAIL, "The Join DM Announce is not set! Please use `" + event.getPrefix() + "ann set joindm [MESSAGE]` to set it.").queue();
										return;
									}
									String s = "The Join DM Announce Message will look like this when an user joins here:";
									s += "\n\n" + Announces.parse(event.getDiscordGuild().getAnnounces().getJoinDMAnnounce(), event.getOriginMember());
									event.sendMessage(s).queue();
								})
								.build())
						.build())
				.build();
	}
}
