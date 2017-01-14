package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.poll.Option;
import br.com.brjdevs.steven.bran.core.poll.Poll;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;

public class PollCommand {
	
	@Command
	private static ICommand poll() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setAliases("poll")
				.setDefault("info")
				.setName("Poll Command")
				.setHelp("poll ?")
				.setDescription("Nah, nothing to talk about this command.")
				.setRequiredPermission(Permissions.POLL)
				.setExample("poll create What should I play? ;Game 1;Game 2;Game 3;Game 4;")
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("create")
						.setName("Poll Create Command")
						.setDescription("Creates polls in the current channel!")
						.setExample("poll create What should I play? ;Game 1;Game 2;Game 3;Game 4;")
						.setArgs(new Argument<>("name", String.class), new Argument<>("options", String.class))
						.setAction((event) -> {
							String name = ((String) event.getArgument("name").get());
							String rawOptions = (String) event.getArgument("options").get();
							LinkedList<String> list = new LinkedList<>(Arrays.asList(rawOptions.substring(rawOptions.indexOf(";") + 1).split(";")));
							if (list.isEmpty()) {
								event.sendMessage("I can't create a Poll without options!").queue();
								return;
							}
							if(list.size() == 1) {
								event.sendMessage("You want a Poll with one option...?").queue();
								return;
							}
							LinkedList<Option> options = new LinkedList<>();
							for (String string : list)
								options.add(new Option(list.indexOf(string), string));
							new Poll(name, event.getOriginMember(), options, event.getTextChannel());
							event.sendMessage("Created a Poll! You can vote by typing the number of the option.").queue();
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
							builder.setTitle(poll.getPollName());
							builder.setFooter("This Poll was created by " + Util.getUser(event.getJDA().getUserById(poll.getCreatorId())), Util.getAvatarUrl(event.getJDA().getUserById(poll.getCreatorId())));
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("**Current Votes**\n");
							poll.getOptions().forEach(option ->
								stringBuilder.append("**" + (option.getIndex() + 1) + ".** " + option.getContent() + "    *(Votes: " + option.getVotes().size() + ")*\n"));
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
							if (!poll.getCreatorId().equals(event.getAuthor().getId()) && !event.getMember().hasPermission(Permissions.GUILD_MOD, event.getJDA())) {
								event.sendMessage("You can't do this... You're not the creator of this poll nor a Guild Moderator to end this poll!").queue();
								return;
							}
							boolean wasOwner = poll.getCreatorId().equals(event.getAuthor().getId());
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle(poll.getPollName());
							builder.setFooter("This Poll was created by " + Util.getUser(event.getJDA().getUserById(poll.getCreatorId())), Util.getAvatarUrl(event.getJDA().getUserById(poll.getCreatorId())));
							StringBuilder stringBuilder = new StringBuilder();
							if (!wasOwner) stringBuilder.append("**This Poll was forcibly ended by a moderator!**\n\n");
							else stringBuilder.append("**The Poll creator has stopped it.**\n\n");
							if (!poll.getLeadership().isEmpty()) {
								stringBuilder.append("**Results**\n");
								poll.getOptions().forEach(option ->
										stringBuilder.append("**" + (option.getIndex() + 1) + ".** " + option.getContent() + "    *(Votes: " + option.getVotes().size() + ")*\n"));
							} else {
								stringBuilder.append("**That's kinda sad I guess, no one voted to the Poll!**");
							}
							builder.setDescription(stringBuilder.toString());
							builder.setColor(Color.decode("#F89F3F"));
							poll.remove();
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.build();
	}
}
