package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.ExitCodes;
import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.brjdevs.steven.bran.core.managers.Permissions.BOT_ADMIN;

public class BotCommand {
	
	@Command
	private static ICommand bot() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setName("Bot Command")
				.setAliases("bot")
				.setHelp("bot ?")
				.setExample("bot stats")
				.setDescription("Multiple options and informations on me!")
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("info")
						.setName("Info Command")
						.setDescription("Gives you information about me!")
						.setAction((event) -> event.sendMessage(event.getShard().getInfo()).queue())
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("inviteme", "invite")
						.setName("InviteMe Command")
						.setDescription("Gives you my OAuth URL!")
						.setAction((event) -> {
							if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("You can invite me to your server using this link: https://discordapp.com/oauth2/authorize?client_id=219186621008838669&scope=bot&permissions=0").queue();
								return;
							}
							MessageEmbed embed = new EmbedBuilder()
									.setAuthor("Bran's OAuth URL", null, Utils.getAvatarUrl(event.getJDA().getSelfUser()))
									.setDescription("You can invite me to your server by [clicking here](https://discordapp.com/oauth2/authorize?client_id=219186621008838669&scope=bot&permissions=0)\n" +
											"If you need any support join [this guild](https://discord.gg/8vXwwEQ) and ask your question in #support!")
									.setColor(Color.decode("#2759DB"))
									.build();
							event.sendMessage(embed).queue();
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.INFORMATIVE)
						.setAliases("stats", "status")
						.setName("Stats Command")
						.setDefault("general")
						.setDescription("Gives you my current statistics!")
						.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
								.setAliases("general")
								.setName("General Stats Command")
								.setDescription("Gives you statistics about me!")
								.setAction((event) -> event.sendMessage(Bran.getInstance().getSession().toEmbedAbout(event.getJDA())).queue())
								.build())
						.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
								.setAliases("technical")
								.setName("Technical Stats Command")
								.setDescription("Gives you technical statistics about me!")
								.setAction((event) -> event.sendMessage(Bran.getInstance().getSession().toEmbedTechnical(event.getJDA())).queue())
								.build())
						.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
								.setAliases("cmds")
								.setName("Commands Stats Command")
								.setDescription("Gives you command statistics about me!")
								.setAction((event) -> event.sendMessage(Bran.getInstance().getSession().toEmbedCmds(event.getJDA())).queue())
								.build())
						.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
								.setAliases("guilds")
								.setName("Guilds Stats Command")
								.setDescription("Gives you guild statistics about me!")
								.setAction((event) -> event.sendMessage(Bran.getInstance().getSession().toEmbedGuilds(event.getJDA())).queue())
								.build())
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("ping")
						.setName("Ping Command")
						.setDescription("Gives you my ping!")
						.setAction((event) -> {
							long time = System.currentTimeMillis();
							event.getChannel().sendTyping().queue(success -> {
								long ping = System.currentTimeMillis() - time;
								event.getChannel().sendMessage("\uD83C\uDFD3 Pong: `" + ping + "ms`").queue();
							});
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("admin")
						.setName("Bot Admin Command")
						.setHelp("bot admin ?")
						.setRequiredPermission(BOT_ADMIN)
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("save")
								.setName("Save Command")
								.setDescription("Saves Guild and Bot Data.")
								.setAction((event) -> {
									try {
										Bran.getInstance().getDataManager().getUserDataManager().update();
										Bran.getInstance().getDataManager().getConfigDataManager().update();
										Bran.getInstance().getDataManager().getHangmanWordsManager().update();
										event.sendMessage(Quotes.SUCCESS, "Successfully saved Bot and Guild data.").queue();
									} catch (Exception e) {
										event.sendMessage(Quotes.FAIL, "There was an error while saving the data: " + Hastebin.post(Utils.getStackTrace(e))).queue();
									}
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("stop", "shutdown")
								.setName("Shutdown Command")
								.setDescription("Saves Guild and Bot Data and stops the bot.")
								.setAction((event, args) -> {
									event.sendMessage("\uD83D\uDC4B").complete();
									Bran.getInstance().shutdownAll(ExitCodes.SHUTDOWN);
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("restart")
								.setName("Restart Command")
								.setDescription("Restarts the Bot")
								.setAction((event) -> {
									event.sendMessage(":wave:").queue();
									Bran.getInstance().shutdownAll(ExitCodes.RESTART);
								})
								.build())
						.addSubCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("account", "acc")
								.setName("Bot Account Command")
								.setHelp("bot admin account ?")
								.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("avatar")
										.setName("Account Avatar Command")
										.setDescription("Updates my Avatar.")
										.setArgs(new Argument("avatar", String.class))
										.setAction((event, args) -> {
											URL url;
											try {
												url = new URL((String) event.getArgument("avatar").get());
											} catch (MalformedURLException e) {
												event.sendMessage(Quotes.FAIL, "`" + event.getArgument("avatar").get() + "` is not a valid URL.").queue();
												return;
											}
											try {
												event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(url.openStream())).queue(success ->
																event.sendMessage(Quotes.SUCCESS, "Updated my Avatar! Yay, I've got a new avatar! :smile:").queue(),
														fail -> {
															String hastebin = Hastebin.post(Utils.getStackTrace(fail));
															event.sendMessage(Quotes.FAIL, "Something went wrong while updating my avatar. (`" + hastebin + "`)").queue();
														});
											} catch (IOException e) {
												event.sendMessage("Failed to open InputStream.").queue();
											}
										})
										.build())
								.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("name")
										.setName("Account Name Command")
										.setDescription("Updates my name.")
										.setArgs(new Argument("name", String.class))
										.setAction((event, args) -> {
											String name = (String) event.getArgument("name").get();
											Stream.of(Bran.getInstance().getShards()).forEach((shard) -> shard.getJDA().getSelfUser().getManager().setName(name).queue());
											event.sendMessage("Updated my name! Yay, I've got a new name, cool! :smile:").queue();
										})
										.build())
								.build())
						.addSubCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("guilds")
								.setHelp("bot admin guilds ?")
								.setName("Guilds Command")
								.setDefault("list")
								.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("list")
										.setName("Guilds List Command")
										.setDescription("Lists you all my guilds.")
										.setArgs(new Argument("page", Integer.class, true))
										.setAction((event, args) -> {
											Argument pageArg = event.getArgument("page");
											int page = pageArg.isPresent() ? (int) pageArg.get() : 1;
											StringListBuilder listBuilder = new StringListBuilder(Bran.getInstance().getGuilds().stream().map(g -> g.getName() + " (" + g.getId() + "[" + Bran.getInstance().getShardId(g.getJDA()) + "]) | Owner: " + Utils.getUser(g.getOwner().getUser())).collect(Collectors.toList()), page, 15);
											listBuilder.setName("Bran Server List").setFooter("Total Servers: " + Bran.getInstance().getGuilds().size());
											event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
										})
										.build())
								.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("leave")
										.setName("Leave Guild Command")
										.setDescription("Leaves a Guild.")
										.setArgs(new Argument("guildId", String.class))
										.setAction((event, args) -> {
											String guildId = (String) event.getArgument("guildId").get();
											Guild guild = event.getJDA().getGuildById(guildId);
											if (guild == null) {
												event.sendMessage("`" + guildId + "` is not a valid Guild ID or it's unknown for me...").queue();
												return;
											}
											guild.leave().queue();
											event.sendMessage(Quotes.SUCCESS, "Left " + guild.getName() + ".").queue();
										})
										.build())
								.build())
						.build())
				.build();
	}
}
