package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.TreeCommandBuilder;
import br.com.brjdevs.bran.core.poll.Option;
import br.com.brjdevs.bran.core.poll.Poll;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;

@RegisterCommand
public class PollCommand {
	public PollCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setAliases("poll")
				.setDefault("info")
				.setName("Poll Command")
				.setHelp("poll ?")
				.setRequiredPermission(Permissions.POLL)
				.setExample("poll create What should I play? ;Game 1;Game 2;Game 3;Game 4;")
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("create")
						.setName("Poll Create Command")
						.setDescription("Creates polls in the current channel!")
						.setExample("poll create What should I play? ;Game 1;Game 2;Game 3;Game 4;")
						.setArgs("[poll Name] [options]")
						.setAction((event, rawArgs) -> {
							rawArgs = rawArgs.substring(rawArgs.indexOf(" ") + 1);
							String[] args = rawArgs.split(";");
							String name = args[0];
							LinkedList<String> list = new LinkedList<>(Arrays.asList(rawArgs.substring(rawArgs.indexOf(";") + 1).split(";")));
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
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
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
							poll.getOptions().forEach(option ->
								stringBuilder.append("**" + (option.getIndex() + 1) + ".** " + option.getContent() + "    *(Votes: " + option.getVotes().size() + ")*\n"));
							builder.setDescription(stringBuilder.toString());
							builder.setColor(Color.decode("#F89F3F"));
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
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
								event.sendMessage("You... can't do this... You're not the creator of this poll neither a Guild Moderator to end this poll!").queue();
								return;
							}
							boolean wasOwner = poll.getCreatorId().equals(event.getAuthor().getId());
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle(poll.getPollName());
							builder.setFooter("This Poll was created by " + Util.getUser(event.getJDA().getUserById(poll.getCreatorId())), Util.getAvatarUrl(event.getJDA().getUserById(poll.getCreatorId())));
							StringBuilder stringBuilder = new StringBuilder();
							if (!wasOwner) stringBuilder.append("This Poll was forcibly ended by a moderator.\n\n");
							else
								stringBuilder.append("This Poll was ended by its creator.\n\n");
							if (!poll.getLeadership().isEmpty()) {
								poll.getOptions().forEach(option ->
										stringBuilder.append("**" + (option.getIndex() + 1) + ".** " + option.getContent() + "    *(Votes: " + option.getVotes().size() + ")*\n"));
							} else {
								stringBuilder.append("This Poll had no votes! Nothing won.");
							}
							builder.setDescription(stringBuilder.toString());
							builder.setColor(Color.decode("#F89F3F"));
							poll.remove();
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.build());
	}
}
