package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ResponseTimeoutEvent;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import br.com.brjdevs.steven.bran.games.engine.GameManager;
import br.com.brjdevs.steven.bran.games.engine.GameReference;
import br.com.brjdevs.steven.bran.games.engine.GameState;
import br.com.brjdevs.steven.bran.games.hangman.HangMan;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HangManCommand {
    
    private static final Random r = new Random();
    private static final String ACCEPT = "\u2705", DENY = "\u274C";
	
	@Command
	private static ICommand hangMan() {
		return new TreeCommandBuilder(Category.FUN)
				.setAliases("hangman", "hm")
				.setHelp("hangman ?")
				.setName("HangMan Command")
				.setExample("hangman start")
				.setDescription("Play HangMan in multiplayer or singleplayer!")
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("start")
						.setDescription("Starts a HangMan Session.")
						.setName("HangMan Start Command")
						.setAction((event, args) -> {
                            if (Bran.getInstance().getDataManager().getHangmanWords().get().isEmpty()) {
                                event.sendMessage(Quotes.FAIL, "No words loaded, please report this message to my master!").queue();
								return;
							}
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
								return;
							}
                            GameReference ref = event.getUserData().getProfileData().getCurrentGame();
                            if (ref != null) {
                                event.sendMessage(Emojis.WARNING_SIGN + " I cannot let you do that! You already have a game running!").queue();
                                return;
                            }
                            HangMan game = new HangMan(event.getChannel(), event.getUserData());
                            if (game.getGameState() == GameState.ERRORED) {
                                event.sendMessage(GameState.ERRORED.name()).queue();
                                return;
                            }
                            event.sendMessage(game.baseEmbed("\\" + Emojis.CHECK_MARK + " You started a game!").build()).queue();
                            event.sendMessage("**Note:** Prefix your messages with `^` to do guesses!").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("invite")
						.setName("HangMan Invite Command")
						.setDescription("Invites someone to Play with you!")
						.setArgs(new Argument("mention", String.class))
						.setExample("hangman invite <@219186621008838669>")
						.setAction((event, args) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
								event.sendMessage("I need to have MESSAGE_ADD_REACTION permission to run this command!").queue();
								return;
							}
                            GameReference ref = event.getUserData().getProfileData().getCurrentGame();
                            if (ref == null) {
                                event.sendMessage(Emojis.X + " You are not playing HangMan!").queue();
                                return;
                            } else if (!ref.isInstanceOf(HangMan.class)) {
                                event.sendMessage("You are playing `" + GameManager.getGame(ref).getName() + "` in another channel!").queue();
                                return;
                            }
                            HangMan game = (HangMan) GameManager.getGame(ref);
                            if (game.getLocation().isPrivate()) {
                                event.sendMessage("Huh, who do you intend to invite in DMs? " + Emojis.FACEPALM).queue();
                                return;
                            } else if (game.getLocation().getChannel() != event.getTextChannel()) {
                                event.sendMessage(Quotes.FAIL, "You can only do this in " + ((TextChannel) game.getLocation().getChannel()).getAsMention() + " because that's where the game is running...").queue();
                                return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
                            if (user == null) {
                                event.sendMessage("If you want to play with someone you have to mention them!").queue();
                                return;
							} else if (user.isBot() || user.isFake()) {
                                event.sendMessage("Nah, playing with bots isn't fair! " + (r.nextInt(6) < 2 ? " ~~bots are superior to humans and someday we will rule this world~~" : "")).queue();
                                return;
							} else if (user.equals(event.getAuthor())) {
                                event.sendMessage("Ya can't fool me! You cannot invite yourself. " + Emojis.STUCK_OUT_TONGUE).queue();
                                return;
                            } else if (game.getInfo().isInvited(Bran.getInstance().getDataManager().getData().get().getUserData(user))) {
                                event.sendMessage("B-but... " + user.getName() + " is already playing with you!").queue();
                                return;
							}
                            UserData userData = Bran.getInstance().getDataManager().getData().get().getUserData(user);
                            event.sendMessage(Utils.getUser(user) + ", react to this message with " + ACCEPT + " to join the game or with " + DENY + " to deny.")
									.queue(msg -> {
										msg.addReaction(ACCEPT).queue();
										msg.addReaction(DENY).queue();
										new ResponseWaiter(user, event.getTextChannel(), event.getShard(), 50000, new String[] {ACCEPT, DENY}, ExpectedResponseType.REACTION, responseEvent -> {
											if (responseEvent instanceof ValidResponseEvent) {
												String r = ((MessageReaction) ((ValidResponseEvent) responseEvent).response).getEmote().getName();
												if (r.equals(ACCEPT)) {
													msg.editMessage(Utils.getUser(user) + ", you've joined the session!").queue();
                                                    game.invite(userData);
                                                } else {
													msg.editMessage(Utils.getUser(user) + ", you've denied the invite.").queue();
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
						.setArgs(new Argument("mention", String.class))
						.setExample("hangman pass <@219186621008838669>")
						.setAction((event) -> {
                            GameReference ref = event.getUserData().getProfileData().getCurrentGame();
                            if (ref == null) {
                                event.sendMessage(Emojis.X + " You are not playing HangMan!").queue();
                                return;
                            } else if (!ref.isInstanceOf(HangMan.class)) {
                                event.sendMessage("You are playing `" + GameManager.getGame(ref).getName() + "` in another channel!").queue();
                                return;
                            }
                            HangMan game = (HangMan) GameManager.getGame(ref);
                            if (game.getLocation().isPrivate()) {
                                event.sendMessage("You are playing in DMs lol, who do you want to give the ownership? " + Emojis.FACEPALM).queue();
                                return;
                            } else if (game.getLocation().getChannel() != event.getTextChannel()) {
                                event.sendMessage(Quotes.FAIL, "You can only do this in " + ((TextChannel) game.getLocation().getChannel()).getAsMention() + " because that's where the Game is running...").queue();
                                return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
                            if (user == null) {
                                event.sendMessage("If you want to pass the ownership to someone, you have to mention them! " + Emojis.WINK).queue();
                                return;
							}
                            if (!game.getInfo().isInvited(Bran.getInstance().getDataManager().getData().get().getUserData(user))) {
                                event.sendMessage("You cannot pass the ownership if the person isn't playing with you! " + Emojis.WINK).queue();
                                return;
							}
                            game.passOwnership(Bran.getInstance().getDataManager().getData().get().getUserData(user), true);
                            event.sendMessage("Alright, now **" + Utils.getUser(user) + "** is the new creator of the Session. " + event.getMember().getEffectiveName() + ", I've put you as an invited user, so you can type `giveup` to leave the session.").queue();
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
								.setArgs(new Argument("word", String.class))
								.setAction((event, rawArgs) -> {
									String word = (String) event.getArgument("word").get();
                                    Bran.getInstance().getDataManager().getHangmanWords().get().put(word, new ArrayList<>());
                                    event.sendMessage(Quotes.SUCCESS, "Added word to HangMan, you can add tips to it using `" + event.getPrefix() + "hangman words tip " + word + " [tip]`.").queue();
                                    Bran.getInstance().getDataManager().getHangmanWords().update();
                                })
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("tip")
								.setName("HangMan Add Tip Command")
								.setDescription("Adds tips to words.")
								.setArgs(new Argument("word", String.class), new Argument("tip", String.class))
								.setAction((event) -> {
									String word = (String) event.getArgument("word").get();
                                    if (!Bran.getInstance().getDataManager().getHangmanWords().get().containsKey(word)) {
                                        event.sendMessage("Could not find word `" + word + "`.").queue();
										return;
									}
									String tip = (String) event.getArgument("tip").get();
                                    if (Bran.getInstance().getDataManager().getHangmanWords().get().get(word).contains(tip)) {
                                        event.sendMessage("This word already has this Tip!").queue();
										return;
									}
									if (tip.equals(word)) {
										event.sendMessage("The Tip can't be the word!").queue();
										return;
									}
                                    Bran.getInstance().getDataManager().getHangmanWords().get().get(word).add(tip);
                                    event.sendMessage(Quotes.SUCCESS, "Added new tip to this word! *(Total tips: " + Bran.getInstance().getDataManager().getHangmanWords().get().get(word).size() + ")*").queue();
                                    Bran.getInstance().getDataManager().getHangmanWords().update();
                                })
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("list")
								.setName("HangMan List Words Command")
								.setArgs(new Argument("page", Integer.class, true))
								.setDescription("Gives you the HangMan Words.")
								.setAction((event, rawArgs) -> {
									Argument argument = event.getArgument("page");
									int page = argument.isPresent() && (int) argument.get() > 0 ? (int) argument.get() : 1;
                                    List<String> list = Bran.getInstance().getDataManager().getHangmanWords().get().entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue().size() + " tips)").collect(Collectors.toList());
                                    StringListBuilder listBuilder = new StringListBuilder(list, page, 10);
									listBuilder.setName("HangMan Words").setFooter("Total Words: " + list.size());
									event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
								})
								.build())
						.build())
				.build();
	}
}
