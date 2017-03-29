package br.net.brjdevs.steven.bran.cmds.botAdmin;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.Client;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.core.utils.TimeUtils;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
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
                        .setArgs(new Argument("shard", Integer.class, true))
                        .setAction((event) -> {
                            if (!event.getArgument("shard").isPresent()) {
                                event.sendMessage(createShardInfo()).queue();
                                return;
                            }
                            int shardId = ((int) event.getArgument("shard").get());
                            Client s = Bran.getInstance().getShards()[shardId];
                            event.sendMessage(createShardInfo(s)).queue();
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
								Stream.of(Bran.getInstance().getShards()).forEach(s -> {
									try {
										Bran.getInstance().reboot(s);
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
                                Client s = Bran.getInstance().getShards()[shardId];
                                try {
									Bran.getInstance().reboot(s);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						})
						.build())
				.build();
	}
    
    private static MessageEmbed createShardInfo(Client shard) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
		JDA jda = shard.getJDA();
		embedBuilder.setTitle("Shard #" + shard.getId(), null);
		embedBuilder.addField("Total Uptime", TimeUtils.format(System.currentTimeMillis() - shard.getStartup()), true);
		embedBuilder.addField("Last Reboot", TimeUtils.format(System.currentTimeMillis() - shard.getLastReboot()), true);
        embedBuilder.addField("Last Event", TimeUtils.format(System.currentTimeMillis() - Bran.getInstance().getLastEvents().get(shard.getId())), true);
        embedBuilder.addField("Event Manager Shutdown", String.valueOf(shard.getEventManager().executor.isShutdown()), true);
		embedBuilder.addField("Status", jda.getStatus().name(), true);
		embedBuilder.addField("General", "**Users:** " + jda.getUsers().size() + "\n**Guilds:** " + jda.getGuilds().size() + "\n**Audio Connections:** " + jda.getGuilds().stream().filter(guild -> guild.getAudioManager().isConnected()).count(), true);
        embedBuilder.setFooter("Ping: " + shard.getJDA().getPing() + "ms", "https://discordapp.com/assets/371886a66446c46e66e9435158468720.svg");
        return embedBuilder.build();
	}
    
    private static String createShardInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("```diff\n");
        for (Client client : Bran.getInstance().getShards()) {
            sb.append(client.getJDA().getStatus() == Status.CONNECTED ? "+" : "-").append(" ");
            sb.append("Shard ").append(client.getJDA().getShardInfo() != null ? client.getJDA().getShardInfo().getShardString() : "[0 / 1]").append(" - Last event: ")
                    .append(TimeUtils.format(System.currentTimeMillis() - Bran.getInstance().getLastEvents().get(client.getId())))
                    .append(" - WS Ping: ").append(client.getJDA().getPing()).append("ms\n");
        }
        return sb.append("```").toString();
    }
}
