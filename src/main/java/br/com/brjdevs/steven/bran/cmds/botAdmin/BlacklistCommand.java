package br.com.brjdevs.steven.bran.cmds.botAdmin;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlacklistCommand {
	private static final Pattern USER_PATTERN = Pattern.compile("(<@!?[0-9]{17,18}>)");
	
	@Command
	private static ICommand blacklist() {
		return new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
				.setAliases("blacklist")
				.setName("Blacklist Command")
				.setDescription("Whoever is in this list doesn't exist for me.")
				.setHelp("blacklist ?")
				.setRequiredPermission(Permissions.BLACKLIST)
				.addSubCommand(new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("user")
						.setName("Blacklist User Command")
						.setHelp("blacklist user ?")
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("list")
								.setName("Blacklist List User Command")
								.setDescription("Lists you all the blacklisted users.")
								.setArgs(new Argument<>("page", Integer.class, true))
								.setAction((event) -> {
									if (Bot.getInstance().getData().getBlacklist().getUserBlacklist().isEmpty()) {
										event.sendMessage("There are no blacklisted users.").queue();
										return;
									}
									event.sendMessage("Just give me a second to index all users...").queue(msg -> {
										int page = event.getArgument("page").isPresent() && (int) event.getArgument("page").get() > 0 ? (int) event.getArgument("page").get() : 1;
										List<String> list = Bot.getInstance()
												.getData().getBlacklist().getUserBlacklist().stream()
												.map(id -> {
													Entry shardEntry = Bot.getInstance().getShards().entrySet().stream()
															.filter(entry -> entry.getValue().getUserById(id) != null)
															.findFirst().orElse(null);
													JDA shard = shardEntry == null ? null : (JDA) shardEntry.getValue();
													if (shard == null) return id + " (Unknown)";
													User user = shard.getUserById(id);
													return Util.getUser(user) + " (ID: " + id + " [" + Bot.getInstance().getShardId(shard) + "])";
													
												}).collect(Collectors.toList());
										ListBuilder listBuilder = new ListBuilder(list, page, 15);
										listBuilder.setName("Blacklisted Users").setFooter("Total Blacklisted Users: " + list.size());
										msg.editMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
									});
								})
								.build())
						.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
								.setAliases("add")
								.setName("Blacklist Add User Command")
								.setDescription("Adds a User to the blacklist.")
								.setArgs(new Argument<>("mention/id", String.class))
								.setAction((event) -> {
									if (event.getMessage().getMentionedUsers().isEmpty()) {
										String arg = (String) event.getArgument("mention/id").get();
										if (USER_PATTERN.matcher(arg).find())
											arg = arg.replaceAll("(<@!?|>)", "");
										boolean added = Bot.getInstance().getData().getBlacklist().addUserById(arg);
										event.sendMessage(added ? "Added User ID **" + arg + "(( to the Blacklist." : "User ID **" + arg + "** is already blacklisted.").queue();
										return;
									}
									User user = event.getMessage().getMentionedUsers().get(0);
									boolean added = Bot.getInstance().getData().getBlacklist().addUser(user);
									event.sendMessage(added ? "Added **" + Util.getUser(user) + "** to the blacklist." : "**" + Util.getUser(user) + "** is already blacklisted.").queue();
								})
								.build())
						.build())
				.build();
	}
}
