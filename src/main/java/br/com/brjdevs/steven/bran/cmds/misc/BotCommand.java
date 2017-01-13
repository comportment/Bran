package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.BotManager;
import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.data.DataManager;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.*;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.managers.Permissions.BOT_ADMIN;

public class BotCommand {
	
	@Command
	public static ICommand bot() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setName("Bot Command")
				.setAliases("bot")
				.setHelp("bot ?")
				.setExample("bot stats")
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("info")
						.setName("Info Command")
						.setDescription("Gives you information about me!")
						.setAction((event) -> event.sendMessage(Bot.getInstance().getInfo()).queue())
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("inviteme", "invite")
						.setName("InviteMe Command")
						.setDescription("Gives you my OAuth URL!")
						.setAction((event) -> {
							MessageEmbed embed = new EmbedBuilder()
									.setAuthor("Bran's OAuth URL", null, Util.getAvatarUrl(event.getJDA().getSelfUser()))
									.setDescription("You can invite me to your server by [clicking here](https://discordapp.com/oauth2/authorize?client_id=219186621008838669&scope=bot&permissions=0)")
									.setColor(Color.decode("#2759DB"))
									.build();
							event.sendMessage(embed).queue();
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
									try {
										DataManager.saveData();
										event.sendMessage(Quotes.SUCCESS, "Successfully saved Bot and Guild data.").queue();
									} catch (Exception e) {
										event.sendMessage(Quotes.FAIL, "There was an error while saving the data: " + Hastebin.post(Util.getStackTrace(e))).queue();
									}
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("stop", "shutdown")
								.setName("Shutdown Command")
								.setDescription("Saves Guild and Bot Data and stops the bot.")
								.setAction((event, args) -> {
									BotManager.preShutdown();
									event.sendMessage("\uD83D\uDC4B").complete();
									BotManager.shutdown(false);
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("updatestats")
								.setName("Update Stats Command")
								.setDescription("Updates the current guild amount in DiscordBots.")
								.setAction((event) -> {
									Bot.getInstance().updateStats();
									event.sendMessage(Quotes.getQuote(Quotes.SUCCESS)).queue();
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
												event.sendMessage(Quotes.FAIL, "Insufficient arguments, please use `" + event.getPrefix() + "bot admin acc avatar [URL]` to update my avatar.").queue();
												return;
											}
											URL url;
											try {
												url = new URL(splitArgs[1]);
											} catch (MalformedURLException e) {
												event.sendMessage(Quotes.FAIL, "`" + splitArgs[1] + "` is not a valid URL.").queue();
												return;
											}
											try {
												event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(url.openStream())).queue(success ->
																event.sendMessage(Quotes.SUCCESS, "Updated my Avatar! Yay, I've got a new avatar! :smile:").queue(),
														fail -> {
															String hastebin = Hastebin.post(Util.getStackTrace(fail));
															event.sendMessage(Quotes.FAIL, "Something went wrong while updating my avatar. (`" + hastebin + "`)").queue();
														});
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
												event.sendMessage(Quotes.FAIL, "Insufficient arguments, please use `" + event.getPrefix() + "bot admin acc avatar [URL]` to update my avatar.").queue();
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
											if (splitArgs.length < 2) {
												event.sendMessage("You have to tell me a Guild ID to me to leave, you can see the guilds I'd like to leave by using `" + event.getPrefix() + "bot admin guilds botc`.").queue();
												return;
											}
											Guild guild = event.getJDA().getGuildById(splitArgs[1]);
											if (guild == null) {
												event.sendMessage("`" + splitArgs[1] + "` is not a valid Guild ID or it's unknown for me...").queue();
												return;
											}
											guild.leave().queue();
											event.sendMessage(Quotes.SUCCESS, "Left " + guild.getName() + "." +
													(RequirementsUtils.getBotCollections().contains(guild) ? " *Finally, I really wanted to leave that Bot Collection Guild...*" : " But... Why?! That Guild wasn't a Bot Collection!")).queue();
										})
										.build())
								.build())
						.build())
				.build();
	}
}
