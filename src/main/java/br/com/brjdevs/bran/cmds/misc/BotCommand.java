package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.data.DataManager;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.ListBuilder;
import br.com.brjdevs.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.bran.core.utils.RequirementsUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import static br.com.brjdevs.bran.core.Permissions.BOT_ADMIN;

@RegisterCommand
public class BotCommand {
	
	public BotCommand () {
		register();
	}

	private static void register () {
		CommandManager.addCommand(new TreeCommandBuilder(Category.INFORMATIVE)
				.setName("Bot Command")
				.setAliases("bot")
				.setHelp("bot ?")
				.setExample("bot stats")
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("info")
						.setName("Info Command")
						.setDescription("Gives you information about me!")
						.setAction((event) -> {
							AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
							builder.append(Bot.getInstance().getInfo());
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("stats", "status")
						.setName("Stats Command")
						.setDescription("Gives you my current statistics!")
						.setAction((event) -> event.sendMessage(Bot.getInstance().getSession().toString(event.getJDA())).queue())
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("ping")
						.setName("Ping Command")
						.setDescription("Gives you my ping!")
						.setAction((event) -> {
							long time = System.currentTimeMillis();
							event.getChannel().sendTyping().queue(success -> {
								long ping = System.currentTimeMillis() - time;
								event.getChannel().sendMessage("Pong: `" + ping + "ms`").queue();
							});
						})
						.build())
				.addCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("admin")
						.setName("Bot Admin Command")
						.setHelp("bot admin ?")
						.setRequiredPermission(BOT_ADMIN)
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("save")
								.setName("Save Command")
								.setDescription("Saves Guild and Bot Data.")
								.setAction((event) -> {
									AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
									try {
										DataManager.saveData();
										builder.append(Quote.SUCCESS);
										builder.append("Successfully saved Bot and Guild data.");
									} catch (Exception e) {
										builder.append(Quote.FAIL);
										builder.append("I failed to save Bot and Guild data because of a fucking " + e.getClass().getSimpleName() + ", please check out the log and fix it ;-;");
									}
									event.sendMessage(builder.build()).queue();
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("stop", "shutdown")
								.setName("Shutdown Command")
								.setDescription("Saves Guild and Bot Data and stops the bot.")
								.setAction((event, args) -> {
									AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
									try {
										DataManager.saveData();
										builder.append(Quote.SUCCESS);
										builder.append("Internally saved Bot and Guild data.");
									} catch (Exception e) {
										builder.append("Something went wrong while internally saving Bot and Guild data because of a fucking " + e.getClass().getSimpleName() + ", please check out the log and fix it ;-;");
										e.printStackTrace();
										return;
									} finally {
										event.sendMessage(builder.build()).queue();
									}
									event.sendMessage("\uD83D\uDC4B").complete();
									Bot.getInstance().getShards().forEach((i, jda) -> {
										jda.shutdown();
										Bot.LOG.info("Shutdown on shard " + i);
									});
									System.exit(0);
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("updatestats")
								.setName("Update Stats Command")
								.setDescription("Updates the current guild amount in DiscordBots.")
								.setAction((event) -> {
									Bot.getInstance().updateStats();
									event.sendMessage(Quote.getQuote(Quote.SUCCESS)).queue();
								})
								.build())
						.addCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("account", "acc")
								.setName("Bot Account Command")
								.setHelp("bot admin account ?")
								.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("avatar")
										.setName("Account Avatar Command")
										.setDescription("Updates my Avatar.")
										.setArgs("[avatar URL]")
										.setAction((event, args) -> {
											String[] splitArgs = StringUtils.splitArgs(args, 2);
											if (Util.isEmpty(splitArgs[1])) {
												event.sendMessage(Quote.getQuote(Quote.FAIL) + "Insufficient arguments, please use `" + event.getPrefix() + "bot admin acc avatar [URL]` to update my avatar.").queue();
												return;
											}
											URL url;
											try {
												url = new URL(splitArgs[1]);
											} catch (MalformedURLException e) {
												event.sendMessage(Quote.getQuote(Quote.FAIL) + "`" + splitArgs[1] + "` is not a valid URL.").queue();
												return;
											}
											try {
												event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(url.openStream())).queue(success ->
																event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Updated my Avatar! Yay, I've got a new avatar! :smile:").queue(),
														fail -> event.sendMessage(Quote.getQuote(Quote.FAIL) + "Something went wrong while updating my avatar. (`" + fail.getClass().getSimpleName() + "`)").queue());
											} catch (IOException e) {
												event.sendMessage("Failed to open InputStream.").queue();
											}
										})
										.build())
								.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("name")
										.setName("Account Name Command")
										.setDescription("Updates my name.")
										.setArgs("[new Name]")
										.setAction((event, args) -> {
											String[] splitArgs = StringUtils.splitArgs(args, 2);
											if (Util.isEmpty(splitArgs[1])) {
												event.sendMessage(Quote.getQuote(Quote.FAIL) + "Insufficient arguments, please use `" + event.getPrefix() + "bot admin acc avatar [URL]` to update my avatar.").queue();
												return;
											}
											String name = splitArgs[1];
											event.getJDA().getSelfUser().getManager().setName(name).queue();
											event.sendMessage("Updated my name! Yay, I've got a new name, cool! :smile:").queue();
										})
										.build())
								.build())
						.addCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("guilds")
								.setHelp("bot admin guilds ?")
								.setName("Guilds Command")
								.setDefault("list")
								.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("botc", "botcollections")
										.setName("Bot Collection Guilds Command")
										.setDescription("Lists you the Bot Collection Guilds.")
										.setArgs("<page>")
										.setAction((event, args) -> {
											if (RequirementsUtils.getBotCollections().isEmpty()) {
												event.sendMessage("Oh yeah, I'm not in any Bot Collection Guilds!").queue();
												return;
											}
											int page = 1;
											try {
												page = Integer.parseInt(StringUtils.splitSimple(args)[1]);
											} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
											}
											ListBuilder listBuilder = new ListBuilder(RequirementsUtils.getBotCollections().stream().map(g -> g.getName() + " (" + g.getId() + "[" + Bot.getInstance().getShardId(g.getJDA()) +"]) Bots: " + Util.DECIMAL_FORMAT.format(RequirementsUtils.getBotsPercentage(g))).collect(Collectors.toList()), page, 15);
											listBuilder.setName("Bot Collection Guilds").setFooter("Total Guilds: " + RequirementsUtils.getBotCollections().size());
											event.sendMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
										})
										.build())
								.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("list")
										.setName("Guilds List Command")
										.setDescription("Lists you all my guilds.")
										.setArgs("<page>")
										.setAction((event, args) -> {
											int page = 1;
											try {
												page = Integer.parseInt(StringUtils.splitSimple(args)[1]);
											} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
											}
											ListBuilder listBuilder = new ListBuilder(Bot.getInstance().getGuilds().stream().map(g -> g.getName() + " (" + g.getId() + "[" + Bot.getInstance().getShardId(g.getJDA()) + "]) | Owner: " + Util.getUser(g.getOwner().getUser())).collect(Collectors.toList()), page, 15);
											listBuilder.setName("Bran Server List").setFooter("Total Servers: " + Bot.getInstance().getGuilds().size());
											event.sendMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
										})
										.build())
								.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
										.setAliases("leave")
										.setName("Leave Guild Command")
										.setDescription("Leaves a Guild.")
										.setArgs("<guild ID>")
										.setAction((event, args) -> {
											String[] splitArgs = StringUtils.splitSimple(args);
											AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
											if (splitArgs.length < 2) {
												builder.append(Quote.FAIL);
												builder.append("You have to tell me a Guild ID to me to leave, you can see the guilds I'd like to leave by using `" + event.getPrefix() + "bot admin guilds botc`.");
												event.sendMessage(builder.build()).queue();
												return;
											}
											Guild guild = event.getJDA().getGuildById(splitArgs[1]);
											if (guild == null) {
												builder.append(Quote.FAIL);
												builder.append("`" + splitArgs[1] + "` is not a valid Guild ID or it's unknown for me...");
												event.sendMessage(builder.build()).queue();
												return;
											}
											guild.leave().queue();
											builder.append(Quote.SUCCESS);
											builder.append("Successfully left " + guild.getName() + ".");
											builder.append(RequirementsUtils.getBotCollections().contains(guild) ? " *Finally, I really wanted to leave that Bot Collection Guild...*" : " But... Why?! That Guild wasn't a Bot Collection!");
											event.sendMessage(builder.build()).queue();
											register();
										})
										.build())
								.build())
						.build())
				.build());
	}
}
