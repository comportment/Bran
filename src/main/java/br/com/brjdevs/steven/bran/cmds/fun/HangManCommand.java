package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.action.Action;
import br.com.brjdevs.steven.bran.core.action.Action.onInvalidResponse;
import br.com.brjdevs.steven.bran.core.action.ActionType;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.bot.BotData;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import br.com.brjdevs.steven.bran.features.hangman.HangManGame;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HangManCommand {
	
	private static final String ACCEPT = "\u2705", DENY = "\u274C";
	
	@Command
	private static ICommand hangMan() {
		return new TreeCommandBuilder(Category.FUN)
				.setAliases("hangman", "hm")
				.setHelp("hangman ?")
				.setName("HangMan Command")
				.setExample("hangman start")
				.setDescription("Play HangMan in multiplayer or singleplayer!")
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("start")
						.setDescription("Starts a HangMan Session.")
						.setName("HangMan Start Command")
						.setAction((event, args) -> {
							HangManGame session = HangManGame.getSession(event.getGuildMember().getProfile());
							if (HangManGame.getSession(event.getGuildMember().getProfile()) != null) {
								if (!session.getChannel().canTalk(session.getChannel().getGuild().getMember(session.getCreator().getUser(event.getJDA())))) {
									event.sendMessage("You had a Session running in another Channel, but you can't talk in there so I'm closing that session and starting another for you :)").queue();
									session.end();
								} else {
									event.sendMessage(Quotes.FAIL, "You can't start another Session because you have one running in " + session.getChannel().getAsMention() + ". If you want to stop that session you can type `giveup`.").queue();
									return;
								}
							}
							session = new HangManGame(event.getGuildMember().getProfile(), Util.getEntryByIndex(Bot.getData().getHangManWords(), MathUtils.random(Bot.getData().getHangManWords().size())).getKey(), event.getTextChannel());
							event.sendMessage(session.createEmbed().setDescription("You started a Hang Man Game in " + event.getTextChannel().getAsMention() + ". Why don't you invite someone to play with you?").build()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("invite")
						.setName("HangMan Invite Command")
						.setDescription("Invites someone to Play with you!")
						.setArgs(new Argument<>("mention", String.class))
						.setExample("hangman invite <@219186621008838669>")
						.setAction((event, args) -> {
							HangManGame session = HangManGame.getSession(event.getGuildMember().getProfile());
							if (session == null) {
								event.sendMessage(Quotes.FAIL, "You don't have a Game Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to start one!").queue();
								return;
							}
							if (session.getChannel() != event.getTextChannel()) {
								event.sendMessage(Quotes.FAIL, "You can only invite users in " + session.getChannel().getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quotes.FAIL, "You have to mention an User to play with you!").queue();
								return;
							}
							if (user.isBot() || user.isFake()) {
								event.sendMessage(Quotes.FAIL, "You can't invite Bots to play with you!.").queue();
								return;
							}
							if (user.equals(event.getAuthor())) {
								event.sendMessage(Quotes.FAIL, "You really just tried to invite yourself?! I hope that wasn't intentional...").queue();
								return;
							}
							Profile profile = event.getDiscordGuild().getMember(user).getProfile();
							if (session.getProfiles().contains(profile)) {
								event.sendMessage(Quotes.FAIL, "**" + Util.getUser(user) + "** is already playing with you.").queue();
								return;
							}
							event.getChannel().sendMessage(Util.getUser(user) + ", react to this message with " + ACCEPT + " to join the game or with " + DENY + " to deny.")
									.queue(msg -> {
										msg.addReaction(ACCEPT).queue();
										msg.addReaction(DENY).queue();
										Action action = new Action(ActionType.REACTION, onInvalidResponse.IGNORE, msg,
												response -> {
													if (response.equals(ACCEPT)) {
														msg.editMessage(Util.getUser(user) + ", you've joined the session!").queue();
														session.invite(profile);
													} else {
														msg.editMessage(Util.getUser(user) + ", you've denied the invite.").queue();
													}
													if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
														msg.clearReactions().queue();
												}, ACCEPT, DENY);
										action.addUser(user);
									});
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("pass")
						.setName("HangMan Session Pass Command")
						.setDescription("Changes the owner of the current session.")
						.setArgs(new Argument<>("mention", String.class))
						.setExample("hangman pass <@219186621008838669>")
						.setAction((event) -> {
							HangManGame session = HangManGame.getSession(event.getGuildMember().getProfile());
							if (session == null) {
								event.sendMessage(Quotes.FAIL, "You don't have a Session Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to create one!").queue();
								return;
							}
							if (session.getChannel() != event.getTextChannel()) {
								event.sendMessage(Quotes.FAIL, "You can only do this in " + session.getChannel().getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quotes.FAIL, "You have to mention an User to play with you!").queue();
								return;
							}
							Profile profile = event.getDiscordGuild().getMember(user).getProfile();
							if (!session.getInvitedUsers().contains(profile)) {
								event.sendMessage(Quotes.FAIL, "You have to mention an invited user.").queue();
								return;
							}
							session.setCreator(profile);
							event.sendMessage(Quotes.SUCCESS, "Alright, now **" + Util.getUser(user) + "** is the new creator of the Session. " + event.getMember().getEffectiveName() + ", I've put you as an invited user, so you can type `giveup` to leave the session.").queue();
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("words")
						.setName("HangMan Words Command")
						.setHelp("hm words ?")
						.setExample("hm words add Cool")
						.setRequiredPermission(Permissions.BOT_ADMIN)
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("add")
								.setExample("hm words add Cool")
								.setName("HangMan Add Word Command")
								.setDescription("Adds words to the HangMan Game!")
								.setArgs(new Argument<>("word", String.class))
								.setAction((event, rawArgs) -> {
									String word = (String) event.getArgument("word").get();
									Bot.getData().getHangManWords().put(word, new ArrayList<>());
									event.sendMessage(Quotes.SUCCESS, "Added word to HangMan, you can add tips to it using `" + event.getPrefix() + "hangman words tip " + word + " [tip]`.").queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("tip")
								.setName("HangMan Add Tip Command")
								.setDescription("Adds tips to words.")
								.setArgs(new Argument<>("word", String.class), new Argument<>("tip", String.class))
								.setAction((event) -> {
									String word = (String) event.getArgument("word").get();
									BotData data = Bot.getData();
									if (!data.getHangManWords().containsKey(word)) {
										event.sendMessage("Could not find word `" + word + "`.").queue();
										return;
									}
									String tip = (String) event.getArgument("tip").get();
									if (data.getHangManWords().get(word).contains(tip)) {
										event.sendMessage("This word already has this Tip!").queue();
										return;
									}
									if (tip.equals(word)) {
										event.sendMessage("The Tip can't be the word!").queue();
										return;
									}
									data.getHangManWords().get(word).add(tip);
									event.sendMessage(Quotes.SUCCESS, "Added new tip to this word! *(Total tips: " + data.getHangManWords().get(word).size() + ")*").queue();
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("list")
								.setName("HangMan List Words Command")
								.setArgs(new Argument<>("page", Integer.class, true))
								.setDescription("Gives you the HangMan Words.")
								.setAction((event, rawArgs) -> {
									Argument argument = event.getArgument("page");
									int page = argument.isPresent() && (int) argument.get() > 0 ? (int) argument.get() : 1;
									List<String> list = Bot.getData().getHangManWords().entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue().size() + " tips)").collect(Collectors.toList());
									ListBuilder listBuilder = new ListBuilder(list, page, 10);
									listBuilder.setName("HangMan Words").setFooter("Total Words: " + list.size());
									event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
								})
								.build())
						.build())
				.build();
	}
}
