package br.net.brjdevs.steven.bran.cmds.guildAdmin;

import br.net.brjdevs.steven.bran.core.audio.AudioLoader;
import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.CommandManager;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.core.utils.TimeUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ConfigCommand {
	
	@Command
	private static ICommand config() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("config", "cfg", "options", "opts")
				.setName("Config Command")
				.setRequiredPermission(Permissions.GUILD_MANAGE)
				.setPrivateAvailable(false)
				.setDescription("Manage extra options with this command!")
								.addSubCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("music")
												.setName("Config Music Command")
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("max_songs_per_user")
								.setName("Config Music Command")
								.setDescription("Configures the maximum amount of songs per user in the queue.")
								.setArgs(new Argument("value", String.class, true))
								.setAction((event) -> {
									Argument argument = event.getArgument("value");
									if (!argument.isPresent()) {
                                        
                                        long current = event.getGuildData(true).maxSongsPerUser;
                                        if (current > 0)
											event.sendMessage("The current maximum amount of songs per user in the queue is " + current).queue();
										else
											event.sendMessage("There is no limit of maximum amount of songs per user set in this guild").queue();
										return;
									}
									String value = (String) argument.get();
									if (!MathUtils.isInteger(value) && !"none".equals(value)) {
										event.sendMessage("`" + value + "` is not a valid number.").queue();
										return;
									}
									int i = !value.equals("none") ? Integer.parseInt(value) : -1;
									if (i < -1) i = -1;
                                    event.getGuildData(false).maxSongsPerUser = i;
                                    if (i > 0)
										event.sendMessage(Quotes.SUCCESS, "Now each user can only have " + i + " song(s) in the queue at once.").queue();
									else
										event.sendMessage(Quotes.SUCCESS, "Now each user can have unlimited songs in the queue at once.").queue();
                                    Bran.getInstance().getDataManager().getData().update();
                                })
								.build())
						.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("max_song_duration")
								.setName("Max Song Duration Command")
								.setDescription("Change the max song duration for the current guild!")
								.setArgs(new Argument("duration", String.class, true))
								.setRequiredPermission(Permissions.GUILD_MOD)
                                .setAction((event) -> {
                                    Argument argument = event.getArgument("duration");
                                    if (!argument.isPresent()) {
                                        event.sendMessage("The current max song duration: `" +
                                                TimeUtils.format(event.getGuildData(true).maxSongDuration) + "`.").queue();
                                        return;
                                    }
                                    try {
                                        long duration = ((String) argument.get()).matches("none|remove|default") ? AudioLoader.MAX_SONG_LENGTH : TimeUtils.getTime(((String) argument.get()), TimeUnit.MILLISECONDS);
                                        if (duration > AudioLoader.MAX_SONG_LENGTH || duration < 0) {
                                            event.sendMessage("The max song duration has to be bigger than 0 and lower than 3 hours!").queue();
                                            return;
                                        }
                                        event.getGuildData(false).maxSongDuration = duration;
                                        event.sendMessage(Quotes.SUCCESS, "Now the max song duration is `" +
                                                TimeUtils.format(event.getGuildData(false).maxSongDuration) + "`!").queue();
                                        Bran.getInstance().getDataManager().getData().update();
                                    } catch (UnsupportedOperationException e) {
                                        event.sendMessage(e.getMessage() + " The correct format is `2h59m59s` for example.").queue();
                                    }
                                })
                                .build())
                        
                        .addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                .setAliases("fairqueue")
                                .setName("FairQueue Command")
                                .setDescription("Change the FairQueue configs for the Guild.")
                                .setArgs(new Argument("level", Integer.class, true))
                                .setRequiredPermission(Permissions.GUILD_MOD)
                                .setAction((event) -> {
                                    Argument argument = event.getArgument("level");
                                    if (!argument.isPresent()) {
                                        event.sendMessage("The current FairQueue Level for this Guild is `" + event.getGuildData(true).fairQueueLevel + "`.").queue();
                                    } else if (((int) argument.get()) > 2) {
                                        event.sendMessage("The biggest FairQueue Level is 2!").queue();
                                    } else if (((int) argument.get()) < 0) {
                                        event.sendMessage("The FairQueue Level has to be bigger or equal 0!").queue();
                                    } else {
                                        event.getGuildData(false).fairQueueLevel = ((int) argument.get());
                                        event.sendMessage("Done, now the FairQueue Level for this Guild is `" + argument.get() + "`.").queue();
                                        Bran.getInstance().getDataManager().getData().update();
                                    }
                                })
                                .build())
                        .build())
                .addSubCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
                        .setAliases("commands", "cmds")
                        .setName("Commands Config")
                        .setDescription("Enable/disable commands!")
                        .addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                .setName("Command Enable")
                                .setAliases("enable")
                                .setDescription("Enable commands.")
                                .setArgs(new Argument("command", String.class), new Argument("channel", String.class))
                                .setArgumentParser((input) -> {
                                    if (!input.contains(" "))
                                        return new String[] {input};
                                    int index = input.lastIndexOf(" ");
                                    return new String[] {input.substring(0, index), input.substring(index + 1)};
                                })
                                .setAction((event) -> {
                                    if (event.getMessage().getMentionedChannels().isEmpty()) {
                                        event.sendMessage("You have to mention a channel!").queue();
                                        return;
                                    }
                                    String rawCmd = ((String) event.getArgument("command").get());
                                    CommandManager m = Bran.getInstance().getCommandManager();
                                    ICommand cmd = null;
                                    do {
                                        String[] split = rawCmd.split(" ");
                                        if (cmd == null)
                                            cmd = m.getCommand(split[0]);
                                        else
                                            cmd = m.getCommand((ITreeCommand) cmd, split[0]);
                                        try {
                                            rawCmd = rawCmd.substring(split[0].length() + 1);
                                        } catch (Exception ignored) {
                                            rawCmd = "";
                                        }
                                    } while (!rawCmd.isEmpty() && cmd instanceof ITreeCommand);
                                    if (cmd == null) {
                                        event.sendMessage(Emojis.X + " No commands found matching the following criteria: " + event.getArgument("command").get() + ". Make sure you didn't include prefixes in the command.").queue();
                                        return;
                                    } else if (!event.getGuildData(true).getDisabledCommands(event.getTextChannel()).contains(cmd.getKey())) {
                                        event.sendMessage("This command isn't disabled!").queue();
                                        return;
                                    }
                                    TextChannel channel = event.getMessage().getMentionedChannels().get(0);
                                    event.getGuildData(false).getDisabledCommands(channel).remove(cmd.getKey());
                                    event.sendMessage(Emojis.CHECK_MARK + " Command " + cmd.getName() + " was enabled in " + channel.getAsMention() + "!").queue();
                                })
                                .build())
                        .addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                .setAliases("disable")
                                .setName("Command Disable")
                                .setDescription("Enable commands.")
                                .setArgs(new Argument("command", String.class), new Argument("channel", String.class))
                                .setArgumentParser((input) -> {
                                    if (!input.contains(" "))
                                        return new String[] {input};
                                    int index = input.lastIndexOf(" ");
                                    return new String[] {input.substring(0, index), input.substring(index + 1)};
                                })
                                .setAction((event) -> {
                                    if (event.getMessage().getMentionedChannels().isEmpty()) {
                                        event.sendMessage("You have to mention a channel!").queue();
                                        return;
                                    }
                                    String rawCmd = ((String) event.getArgument("command").get());
                                    CommandManager m = Bran.getInstance().getCommandManager();
                                    ICommand cmd = null;
                                    do {
                                        String[] split = rawCmd.split(" ");
                                        if (cmd == null)
                                            cmd = m.getCommand(split[0]);
                                        else
                                            cmd = m.getCommand((ITreeCommand) cmd, split[0]);
                                        try {
                                            rawCmd = rawCmd.substring(split[0].length() + 1);
                                        } catch (Exception ignored) {
                                            rawCmd = "";
                                        }
                                    } while (!rawCmd.isEmpty() && cmd instanceof ITreeCommand);
                                    if (cmd == null) {
                                        event.sendMessage(Emojis.X + " No commands found matching the following criteria: " + event.getArgument("command").get() + ". Make sure you didn't include prefixes in the command.").queue();
                                        return;
                                    } else if (event.getGuildData(true).getDisabledCommands(event.getTextChannel()).contains(cmd.getKey())) {
                                        event.sendMessage("This command is already disabled!").queue();
                                        return;
                                    }
                                    TextChannel channel = event.getMessage().getMentionedChannels().get(0);
                                    event.getGuildData(false).getDisabledCommands().computeIfAbsent(Long.parseLong(channel.getId()), id -> new ArrayList<>()).add(cmd.getKey());
                                    event.sendMessage(Emojis.CHECK_MARK + " Command " + cmd.getName() + " was disabled in " + channel.getAsMention() + "!").queue();
                                })
                                .build())
                        .build())
				.build();
	}
}
