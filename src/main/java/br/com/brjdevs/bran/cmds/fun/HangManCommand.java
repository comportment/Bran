package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.ListBuilder;
import br.com.brjdevs.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.bran.core.utils.MathUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import br.com.brjdevs.bran.features.hangman.HMSession;
import br.com.brjdevs.bran.features.hangman.HMSession.Status;
import br.com.brjdevs.bran.features.hangman.HMWord;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

@RegisterCommand
public class HangManCommand {
	public HangManCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.FUN)
				.setAliases("hangman", "hm")
				.setHelp("hangman ?")
				.setName("HangMan Command")
				.setExample("hangman start")
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("start")
						.setDescription("Starts a HangMan Session.")
						.setName("HangMan Start Command")
						.setAction((event, args) -> {
							HMSession session = HMSession.getSession(event.getMember().getProfile());
							if (HMSession.getSession(event.getMember().getProfile()) != null) {
								if (!session.getChannel().canTalk(session.getChannel().getGuild().getMember(session.getCreator().getUser(event.getJDA())))) {
									event.sendMessage("You had a Session running in another Channel, but you can't talk in there so I'm closing that session and starting another for you :)").queue();
									session.end();
								} else {
									event.sendMessage(Quote.getQuote(Quote.FAIL) + "You can't start another Session because you have one running in " + session.getChannel().getAsMention() + ". If you want to stop that session you can type `giveup`.").queue();
									return;
								}
							}
							session = new HMSession(event.getMember().getProfile(), Bot.getInstance().getData().getHMWords().get(MathUtils.random(Bot.getInstance().getData().getHMWords().size())), event.getTextChannel());
							event.sendMessage(session.createEmbed(Status.INFO, event.getJDA())).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("invite")
						.setName("HangMan Invite Command")
						.setDescription("Invites someone to Play with you!")
						.setArgs("[MENTION]")
						.setExample("hangman invite <@219186621008838669>")
						.setAction((event, args) -> {
							HMSession session = HMSession.getSession(event.getMember().getProfile());
							if (session == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You don't have a Session Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to create one!").queue();
								return;
							}
							if (session.getChannel() != event.getTextChannel()) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You can only invite users in " + session.getChannel().getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) +"You have to mention an User to play with you!").queue();
								return;
							}
							if (user.isBot() || user.isFake()) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You can't invite Bots to play with you!.").queue();
								return;
							}
							if (user.equals(event.getAuthor())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You really just tried to invite yourself?! I hope that wasn't intentional...").queue();
								return;
							}
							Profile profile = event.getGuild().getMember(user).getProfile();
							if (session.getProfiles().contains(profile)) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "**" + Util.getUser(user) + "** is already playing with you.").queue();
								return;
							}
							session.invite(profile);
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("pass")
						.setName("HangMan Session Pass Command")
						.setDescription("Changes the owner of the current session.")
						.setArgs("[MENTION]")
						.setExample("hangman pass <@219186621008838669>")
						.setAction((event) -> {
							HMSession session = HMSession.getSession(event.getMember().getProfile());
							if (session == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You don't have a Session Running in anywhere, if you want you can use `" + event.getPrefix() + "hm start` to create one!").queue();
								return;
							}
							if (session.getChannel() != event.getTextChannel()) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You can only do this in " + session.getChannel().getAsMention() + " because that's where the Game is running...").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? null : event.getMessage().getMentionedUsers().get(0);
							if (user == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) +"You have to mention an User to play with you!").queue();
								return;
							}
							Profile profile = event.getGuild().getMember(user).getProfile();
							if (!session.getInvitedUsers().contains(profile)) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You have to mention an invited user.").queue();
								return;
							}
							session.pass(profile);
							event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Alright, now **" + Util.getUser(user) + "** is the new creator of the Session. " + event.getOriginMember().getEffectiveName() + ", I've put you as invited, so you can type `giveup` to leave the session.").queue();
						})
						.build())
				.addCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("words")
						.setName("HangMan Words Command")
						.setHelp("hm words ?")
						.setExample("hm words add Cool")
						.setRequiredPermission(Permissions.BOT_ADMIN)
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("add")
								.setExample("hm words add Cool")
								.setName("HangMan Add Word Command")
								.setDescription("Adds words to the HangMan Game!")
								.setArgs("[word]")
								.setAction((event, rawArgs) -> {
									String word = StringUtils.splitArgs(rawArgs, 2)[1];
									Bot.getInstance().getData().getHMWords().add(new HMWord(word));
									event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Added word to HangMan, you can add tips to it using `" + event.getPrefix() + "hangman words tip " + word + " | [tip]`.").queue();
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("tip")
								.setName("HangMan Add Tip Command")
								.setDescription("Adds tips to words.")
								.setArgs("[word] | [tip]")
								.setAction((event, rawArgs) -> {
									String[] args = StringUtils.splitArgs(rawArgs, 2);
									String word = args[1].substring(0, args[1].indexOf("|")).trim();
									HMWord hmWord =  HMWord.getHMWord(word);
									if (hmWord == null) {
										event.sendMessage("Could not find word `" + word + "`.").queue();
										return;
									}
									boolean added = hmWord.addTip(args[1].substring(args[1].indexOf("|")).trim());
									event.sendMessage(added ? ":white_check_mark:" : "You can't add duplicated tips.").queue();
								})
								.build())
						.addCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("list")
								.setName("HangMan List Words Command")
								.setArgs("<page>")
								.setDescription("Gives you the HangMan Words.")
								.setAction((event, rawArgs) -> {
									String[] args = StringUtils.splitArgs(rawArgs, 3);
									int page = 1;
									if (MathUtils.isInteger(args[1]))
										page = Integer.parseInt(args[1]);
									List<String> list = Bot.getInstance().getData().getHMWords().stream().map(w -> w.getWord() + " (" + w.getTips().size() + " tips)").collect(Collectors.toList());
									ListBuilder listBuilder = new ListBuilder(list, page, 10);
									listBuilder.setName("HangMan Words").setFooter("Total Words: " + list.size());
									event.sendMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
								})
								.build())
						.build())
				.build());
	}
}
