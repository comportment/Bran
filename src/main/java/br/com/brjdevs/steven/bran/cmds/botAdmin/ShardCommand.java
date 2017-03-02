package br.com.brjdevs.steven.bran.cmds.botAdmin;

import br.com.brjdevs.steven.bran.core.client.ClientShard;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.TimeUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;

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
						.setAliases("info", "information", "i")
						.setDescription("Gives you information on a shard.")
						.setName("ShardInfo Command")
						.setArgs(new Argument("shard", String.class))
						.setAction((event) -> {
							String shard = ((String) event.getArgument("shard").get());
							if (shard.charAt(0) == '*') {
								Stream.of(event.getClient().getShards()).forEach(s -> event.sendMessage(createShardInfo(s)).queue());
							} else {
								if (!MathUtils.isInteger(shard)) {
									event.sendMessage("You have to tell me a Shard ID or use `*` to get info on all shards!").queue();
									return;
								}
								int shardId = Integer.parseInt(shard);
								ClientShard s = event.getClient().getShards()[shardId];
								event.sendMessage(createShardInfo(s)).queue();
							}
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.BOT_ADMINISTRATOR)
						.setAliases("reboot", "restart", "r")
						.setName("Shard Reboot Command")
						.setDescription("Reboots a Shard")
						.setArgs(new Argument("shard", String.class))
						.setAction((event) -> {
							String shard = ((String) event.getArgument("shard").get());
							if (shard.charAt(0) == '*') {
								Stream.of(event.getClient().getShards()).forEach(s -> {
									try {
										event.getClient().reboot(s);
									} catch (Exception e) {
										e.printStackTrace();
									}
									Utils.sleep(5_000L);
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
	
	private static MessageEmbed createShardInfo(ClientShard shard) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		JDA jda = shard.getJDA();
		embedBuilder.setTitle("Shard #" + shard.getId(), null);
		embedBuilder.addField("Total Uptime", TimeUtils.format(System.currentTimeMillis() - shard.getStartup()), true);
		embedBuilder.addField("Last Reboot", TimeUtils.format(System.currentTimeMillis() - shard.getLastReboot()), true);
		embedBuilder.addField("Last Event", TimeUtils.format(System.currentTimeMillis() - shard.getClient().getLastEvents().get(shard.getId())), true);
		embedBuilder.addField("Event Manager Shutdown", String.valueOf(shard.getEventManager().executor.isShutdown()), true);
		embedBuilder.addField("Status", jda.getStatus().name(), true);
		embedBuilder.addField("General", "**Users:** " + jda.getUsers().size() + "\n**Guilds:** " + jda.getGuilds().size() + "\n**Audio Connections:** " + jda.getGuilds().stream().filter(guild -> guild.getAudioManager().isConnected()).count(), true);
		return embedBuilder.build();
	}
}
