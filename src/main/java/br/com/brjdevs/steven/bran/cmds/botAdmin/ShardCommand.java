package br.com.brjdevs.steven.bran.cmds.botAdmin;

import br.com.brjdevs.steven.bran.ClientShard;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;

import java.util.stream.Stream;

public class ShardCommand {
	
	@Command
	private static ICommand shard() {
		return new TreeCommandBuilder(Category.BOT_ADMINISTRATOR)
				.setAliases("shard")
				.setName("Shard Command")
				.setDescription("Gives you info and manage the shards")
				.setRequiredPermission(Permissions.BOT_ADMIN)
				.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("info")
						.setDescription("Gives you information on a shard.")
						.setName("ShardInfo Command")
						.setArgs(new Argument<>("shard", String.class))
						.setAction((event) -> {
							String shard = ((String) event.getArgument("shard").get());
							if (shard.charAt(0) == '*') {
								Stream.of(event.getClient().getShards()).forEach(s -> event.sendMessage(OtherUtils.createShardInfo(s)).queue());
							} else {
								if (!MathUtils.isInteger(shard)) {
									event.sendMessage("You have to tell me a Shard ID or use `*` to get info on all shards!").queue();
									return;
								}
								int shardId = Integer.parseInt(shard);
								ClientShard s = event.getClient().getShards()[shardId];
								event.sendMessage(OtherUtils.createShardInfo(s)).queue();
							}
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("reboot")
						.setName("Shard Reboot Command")
						.setDescription("Reboots a Shard")
						.setArgs(new Argument<>("shard", String.class))
						.setAction((event) -> {
							String shard = ((String) event.getArgument("shard").get());
							if (shard.charAt(0) == '*') {
								Stream.of(event.getClient().getShards()).forEach(s -> {
									try {
										event.getClient().reboot(s);
									} catch (Exception e) {
										e.printStackTrace();
									}
									OtherUtils.sleep(5_000L);
								});
							} else {
								if (!MathUtils.isInteger(shard)) {
									return;
								}
								int shardId = Integer.parseInt(shard);
								ClientShard s = event.getClient().getShards()[shardId];
								try {
									event.getClient().reboot(s);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						})
						.build())
				.build();
	}
}
