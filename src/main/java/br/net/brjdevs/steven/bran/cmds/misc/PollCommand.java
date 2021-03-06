package br.net.brjdevs.steven.bran.cmds.misc;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.poll.Option;
import br.net.brjdevs.steven.bran.core.poll.Poll;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class PollCommand {
	
	@Command
	private static ICommand poll() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setAliases("poll")
				.setDefault("info")
				.setName("Poll Command")
								.setDescription("Nah, nothing to talk about this command.")
				.setRequiredPermission(Permissions.POLL)
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("create")
						.setName("Poll Create Command")
						.setDescription("Creates polls in the current channel!")
						.setExample("poll create What should I play? ;Game 1;Game 2;Game 3;Game 4;")
                        .setArgs(new Argument("pollName", String.class), new Argument("options", String.class))
                        .setArgumentParser((input) -> {
                            if (!input.contains(";"))
                                return new String[] {input};
                            int index = input.indexOf(";");
                            return new String[] {input.substring(0, index), input.substring(index + 1)};
                        })
                        .setAction((event) -> {
							if (Poll.getPoll(event.getTextChannel()) != null) {
								event.sendMessage("There's already a Poll running in this Channel!").queue();
								return;
							}
                            String name = ((String) event.getArgument("pollName").get());
                            String rawOptions = ((String) event.getArgument("options").get()).trim();
                            name = name.trim();
							if (name.isEmpty()) {
								event.sendMessage(Quotes.FAIL, "You cannot create a Poll without a name!").queue();
							} else {
								LinkedList<String> list = new LinkedList<>(Arrays.stream(rawOptions.split("(?<=[^\\\\]);")).filter(string -> !string.isEmpty()).map(String::trim).distinct().collect(Collectors.toList()));
								if (list.isEmpty()) {
									event.sendMessage("I can't create a Poll without options!").queue();
									return;
								}
								if (list.size() == 1) {
									event.sendMessage("You want a Poll with one option...?").queue();
									return;
								}
								LinkedList<Option> options = new LinkedList<>();
								for (String string : list)
									options.add(new Option(list.indexOf(string), string));
								new Poll(name, event.getMember(), options, event.getTextChannel());
								event.sendMessage("Created a Poll! You can vote by typing the number of the option, I'll add reactions to the message as the votes get added/removed.").queue();
                                Bran.getInstance().getDataManager().getPolls().update();
                            }
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("info", "view")
						.setName("Poll Information Command")
						.setDescription("Gives you information about a poll running in the current channel.")
						.setAction((event) -> {
							Poll poll = Poll.getPoll(event.getTextChannel());
							if (poll == null) {
								event.sendMessage("No Polls running in this channel!").queue();
								return;
							}
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle(poll.getPollName(), null);
							builder.setFooter("This Poll was created by " + Utils.getUser(event.getJDA().getUserById(poll.getCreatorId())), Utils.getAvatarUrl(event.getJDA().getUserById(poll.getCreatorId())));
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("**Current Votes**\n");
							poll.getOptions().forEach(option ->
									stringBuilder.append("**").append(option.getIndex() + 1).append(".** ").append(option.getContent()).append("    *(Votes: ").append(option.getVotes().size()).append(")*\n"));
							builder.setDescription(stringBuilder.toString());
							builder.setColor(Color.decode("#F89F3F"));
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("end")
						.setName("Poll End Command")
						.setDescription("Ends a Poll running in the current channel.")
						.setAction((event) -> {
							Poll poll = Poll.getPoll(event.getTextChannel());
							if (poll == null) {
								event.sendMessage("No Polls running in this channel!").queue();
								return;
							}
                            if (!poll.getCreatorId().equals(event.getAuthor().getId()) && !event.getGuildData(true).hasPermission(event.getAuthor(), Permissions.GUILD_MOD)) {
                                event.sendMessage("You can't do this... You're not the creator of this poll nor a Guild Moderator to end this poll!").queue();
								return;
							}
							boolean wasOwner = poll.getCreatorId().equals(event.getAuthor().getId());
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle(poll.getPollName(), null);
							builder.setFooter("This Poll was created by " + Utils.getUser(event.getJDA().getUserById(poll.getCreatorId())), Utils.getAvatarUrl(event.getJDA().getUserById(poll.getCreatorId())));
							StringBuilder stringBuilder = new StringBuilder();
							if (!wasOwner) stringBuilder.append("**This Poll was forcibly ended by a moderator!**\n\n");
							else stringBuilder.append("**The Poll creator has stopped it.**\n\n");
							if (!poll.getLeadership().isEmpty()) {
								stringBuilder.append("**Results**\n");
								poll.getOptions().forEach(option ->
										stringBuilder.append("**").append(option.getIndex() + 1).append(".** ").append(option.getContent()).append("    *(Votes: ").append(option.getVotes().size()).append(")*\n"));
							} else {
								stringBuilder.append("**That's kinda sad I guess, no one voted to the Poll!**");
							}
							builder.setDescription(stringBuilder.toString());
							builder.setColor(Color.decode("#F89F3F"));
							poll.remove();
							event.sendMessage(builder.build()).queue();
                            Bran.getInstance().getDataManager().getPolls().update();
                        })
						.build())
				.build();
	}
}
