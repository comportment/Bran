package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.Profile;
import br.com.brjdevs.steven.bran.core.itemManager.Item;
import br.com.brjdevs.steven.bran.core.itemManager.ItemContainer;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ResponseTimeoutEvent;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.com.brjdevs.steven.bran.core.utils.*;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.com.brjdevs.steven.bran.features.hangman.HangManGame;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
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
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
								return;
							}
							Profile profile = event.getClient().getProfile(event.getAuthor());
							HangManGame session = HangManGame.getSession(profile);
							if (session != null) {
								if (!session.getChannel(event.getClient()).canTalk(session.getChannel(event.getClient()).getGuild().getMember(session.getCreator().getUser(event.getJDA())))) {
									event.sendMessage("You had a Session running in another Channel, but you can't talk in there so I'm closing that session and starting another for you :)").queue();
									session.end();
								} else {
									event.sendMessage(Quotes.FAIL, "You can't start another Session because you have one running in " + session.getChannel(event.getClient()).getAsMention() + ". If you want to stop that session you can type `giveup`.").queue();
									return;
								}
							}
							session = new HangManGame(profile, CollectionUtils.getEntryByIndex(event.getClient().getData().getHangmanWordsManager().get(), MathUtils.random(event.getClient().getData().getHangmanWordsManager().get().size())).getKey(), event.getTextChannel(), event.getClient());
							event.sendMessage(session.createEmbed(event.getClient()).setDescription("You started a Hang Man Game in " + event.getTextChannel().getAsMention() + ". Why don't you invite someone to play with you?").build()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("invite")
						.setName("HangMan Invite Command")
						.setDescription("Invites someone to Play with you!")
						.setArgs(new Argument<>("mention", String.class))
						.setExample("hangman invite <@219186621008838669>")
						.setAction((event, args) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
								event.sendMessage("I need to have MESSAGE_ADD_REACTION permission to run this command!").queue();
								return;
							}
							HangManGame session = HangManGame.getSession(event.getClient().getProfile(event.getAuthor()));
							if (session == null) {
								event.sendMessage(Quotes.FAIL, "You don't have a Game Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to start one!").queue();
								return;
							} else if (session.getChannel(event.getClient()) != event.getTextChannel()) {
								event.sendMessage(Quotes.FAIL, "You can only invite users in " + session.getChannel(event.getClient()).getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quotes.FAIL, "You have to mention an User to play with you!").queue();
								return;
							} else if (user.isBot() || user.isFake()) {
								event.sendMessage(Quotes.FAIL, "You can't invite Bots to play with you!.").queue();
								return;
							}// else if (user.equals(event.getAuthor())) {
							//event.sendMessage(Quotes.FAIL, "You really just tried to invite yourself?! I hope that wasn't intentional...").queue();
							//return;
							//}
							Profile profile = event.getClient().getProfile(user);
							if (session.getProfiles().contains(profile)) {
								//event.sendMessage(Quotes.FAIL, "**" + OtherUtils.getUser(user) + "** is already playing with you.").queue();
								//return;
							}
							event.sendMessage(OtherUtils.getUser(user) + ", react to this message with " + ACCEPT + " to join the game or with " + DENY + " to deny.")
									.queue(msg -> {
										msg.addReaction(ACCEPT).queue();
										msg.addReaction(DENY).queue();
										new ResponseWaiter(user, event.getTextChannel(), event.getShard(), 50000, new String[] {ACCEPT, DENY}, ExpectedResponseType.REACTION, responseEvent -> {
											if (responseEvent instanceof ValidResponseEvent) {
												String r = ((MessageReaction) ((ValidResponseEvent) responseEvent).response).getEmote().getName();
												if (r.equals(ACCEPT)) {
													msg.editMessage(OtherUtils.getUser(user) + ", you've joined the session!").queue();
													session.invite(profile);
												} else {
													msg.editMessage(OtherUtils.getUser(user) + ", you've denied the invite.").queue();
												}
											} else if (responseEvent instanceof UnexpectedResponseEvent) {
												msg.editMessage("You've reacted with an invalid reaction!").queue();
											} else if (responseEvent instanceof ResponseTimeoutEvent) {
												msg.editMessage("You took too long to accept or deny the invite!").queue();
											}
										});
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
							HangManGame session = HangManGame.getSession(event.getClient().getProfile(event.getAuthor()));
							if (session == null) {
								event.sendMessage(Quotes.FAIL, "You don't have a Session Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to create one!").queue();
								return;
							} else if (session.getChannel(event.getClient()) != event.getTextChannel()) {
								event.sendMessage(Quotes.FAIL, "You can only do this in " + session.getChannel(event.getClient()).getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quotes.FAIL, "You have to mention an User to play with you!").queue();
								return;
							}
							Profile profile = event.getClient().getProfile(user);
							if (!session.getInvitedUsers().contains(profile)) {
								event.sendMessage(Quotes.FAIL, "You have to mention an invited user.").queue();
								return;
							}
							session.setCreator(profile);
							event.sendMessage(Quotes.SUCCESS, "Alright, now **" + OtherUtils.getUser(user) + "** is the new creator of the Session. " + event.getMember().getEffectiveName() + ", I've put you as an invited user, so you can type `giveup` to leave the session.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("use")
						.setName("Use Item Command")
						.setDescription("Uses a item in HangMan!")
						.setArgs(new Argument<>("item name", String.class))
						.setAction((event) -> {
							Profile profile = event.getClient().getProfile(event.getAuthor());
							HangManGame session = HangManGame.getSession(profile);
							if (session == null) {
								event.sendMessage(Quotes.FAIL, "You don't have a Session Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to create one!").queue();
								return;
							} else if (session.getChannel(event.getClient()) != event.getTextChannel()) {
								event.sendMessage(Quotes.FAIL, "You can only do this in " + session.getChannel(event.getClient()).getAsMention() + " because that's where the Game is running...").queue();
								return;
							} else if (session.isMultiplayer()) {
								event.sendMessage("You can't use items in a MultiPlayer session!").queue();
								return;
							}
							String itemName = (String) event.getArgument("item name").get();
							Item item = ItemContainer.getItemById("HangMan_" + StringUtils.neat(itemName));
							Inventory inventory = profile.getInventory();
							int matches = StringUtils.countMatches(session.getGuessedLetters(), '_');
							if (matches <= 1) {
								event.sendMessage("You can't use items now because you only have one guess left!").queue();
								return;
							} else if (item == null) {
								event.sendMessage("No such Item named \"" + itemName + "\" usable in HangMan!").queue();
								return;
							} else if (inventory.getAmountOf(item) == 0) {
								event.sendMessage("You don't have any " + item.getName() + " in your inventory!").queue();
								return;
							} else if (session.getUsedItems() > 2) {
								event.sendMessage("You can only use 2 items per session!").queue();
								return;
							}
							session.addUseItem();
							inventory.remove(item);
							if (item.getName().equals("Tip")) {
								List<String> tips = event.getClient().getData().getHangmanWordsManager().get().get(session.getWord());
								if (tips.isEmpty()) {
									event.sendMessage("This word doesn't have tips! Sorry!").queue();
									inventory.put(item);
									return;
								}
								event.sendMessage("**Here's a small tip:** " + CollectionUtils.random(event.getClient().getData().getHangmanWordsManager().get().get(session.getWord()))).queue();
							} else if (item.getName().equals("Guesser")) {
								String r = session.getRandomLetter();
								session.guess(r);
								Message message = new MessageBuilder()
										.append("**Here you go, I guessed a letter for you!**\n")
										.setEmbed(session.createEmbed(event.getClient()).build()).build();
								event.sendMessage(message).queue();
							}
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
									event.getClient().getData().getHangmanWordsManager().get().put(word, new ArrayList<>());
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
									if (!event.getClient().getData().getHangmanWordsManager().get().containsKey(word)) {
										event.sendMessage("Could not find word `" + word + "`.").queue();
										return;
									}
									String tip = (String) event.getArgument("tip").get();
									if (event.getClient().getData().getHangmanWordsManager().get().get(word).contains(tip)) {
										event.sendMessage("This word already has this Tip!").queue();
										return;
									}
									if (tip.equals(word)) {
										event.sendMessage("The Tip can't be the word!").queue();
										return;
									}
									event.getClient().getData().getHangmanWordsManager().get().get(word).add(tip);
									event.sendMessage(Quotes.SUCCESS, "Added new tip to this word! *(Total tips: " + event.getClient().getData().getHangmanWordsManager().get().get(word).size() + ")*").queue();
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
									List<String> list = event.getClient().getData().getHangmanWordsManager().get().entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue().size() + " tips)").collect(Collectors.toList());
									StringListBuilder listBuilder = new StringListBuilder(list, page, 10);
									listBuilder.setName("HangMan Words").setFooter("Total Words: " + list.size());
									event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
								})
								.build())
						.build())
				.build();
	}
}
