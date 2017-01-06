package br.com.brjdevs.bran.cmds.guildAdmin;

import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.data.guild.configs.Announces;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.entities.TextChannel;

@RegisterCommand
public class AnnounceCommands {
    public AnnounceCommands() {
        register();
    }
    private static void register() {
        CommandManager.addCommand(
                new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
                        .setAliases("announce", "ann")
                        .setName("Announce Command")
                        .setDefault("list")
                        .setExample("announce set join Welcome to $guild, $user!")
                        .setHelp("ann ?")
                        .setPrivateAvailable(false)
                        .setRequiredPermission(Permissions.ANNOUNCE)
                        .addCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
                                .setAliases("set")
                                .setHelp("ann set ?")
                                .setName("Set Announces Command")
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("channel")
                                        .setName("Set Channel Announce Command")
                                        .setDescription("Sets the Announces Channel.")
                                        .setArgs("<channel mention>")
                                        .setPrivateAvailable(false)
                                        .setAction((event) -> {
                                            AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
                                            TextChannel channel = event.getMessage().getMentionedChannels().isEmpty() ? event.getTextChannel() : event.getMessage().getMentionedChannels().get(0);
                                            event.getGuild().getAnnounces().setAnnouncesChannel(channel);
                                            builder.append(Quote.SUCCESS);
                                            builder.append("Now the Announces will be sent in " + channel.getAsMention() + "!");
                                            event.sendMessage(builder.build()).queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("join")
                                        .setName("Set Join Announce Command")
                                        .setDescription("Sets the Join Message Announce.")
                                        .setArgs("[join message]")
                                        .setExample("announce set join Welcome to $guild, $user!")
                                        .setPrivateAvailable(false)
                                        .setAction((event, args) -> {
                                            String message = StringUtils.splitArgs(args, 2)[1];
                                            if (message.isEmpty()) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "You have to tell me a message! The correct usage is `" + (event.getCommand().getRequiredArgs()) + "`").queue();
                                                return;
                                            }
                                            event.getGuild().getAnnounces().setJoinAnnounce(message);
                                            event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Successfully set the Join Announce Message. " +
                                                    "When someone joins this guild, it'll look like this:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("joindm")
                                        .setName("Set JoinDM Announce Command")
                                        .setDescription("Sets the Join DM Announce message.")
                                        .setArgs("[joinDM message]")
                                        .setExample("announce set joindm Hello, thank you for joining $guild!")
                                        .setPrivateAvailable(false)
                                        .setAction((event, args) -> {
                                            String message = StringUtils.splitArgs(args, 2)[1];
                                            if (message.isEmpty()) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "You have to tell me a message! The correct usage is `" + (event.getCommand().getRequiredArgs()) + "`").queue();
                                                return;
                                            }
                                            event.getGuild().getAnnounces().setJoinDMAnnounce(message);

                                            event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Successfully set the Join DM Announce Message. " +
                                                    "When someone joins this guild, it'll receive this message:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("leave")
                                        .setName("Set Leave Announce Command")
                                        .setDescription("Sets the Leave Announce message.")
                                        .setArgs("[leave message]")
                                        .setExample("announce set leave Goodbye $user, we won't miss you!")
                                        .setPrivateAvailable(false)
                                        .setAction((event, args) -> {
                                            String message = StringUtils.splitArgs(args, 2)[1];
                                            if (message.isEmpty()) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "You have to tell me a message! The correct usage is `" + (event.getCommand().getRequiredArgs()) + "`").queue();
                                                return;
                                            }
                                            event.getGuild().getAnnounces().setLeaveAnnounce(message);
                                            event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Successfully set the Leave Announce Message. " +
                                                    "When someone leaves this guild, it'll look like this:\n\n" + Announces.parse(message, event.getOriginMember())).queue();
                                        })
                                        .build())
                                .build())
                        .addCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
                                .setAliases("preview")
                                .setName("Announce Preview Command")
                                .setHelp("ann preview ?")
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("channel")
                                        .setDescription("Returns you the Announce Channel.")
                                        .setName("Announce Preview Channel Command")
                                        .setAction((event) -> {
                                            if (event.getGuild().getAnnounces().getChannel(event.getJDA()) == null) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "The Announce Channel is not set, please use `" + event.getPrefix() + "ann set channel [MENTION]` to set one.").queue();
                                                return;
                                            }
                                            event.sendMessage("The Announces will be sent in " + event.getGuild().getAnnounces().getChannel(event.getJDA()).getAsMention() + ".").queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("join")
                                        .setDescription("Returns you the Join Announce Message.")
                                        .setName("Join Announce Preview Command")
                                        .setPrivateAvailable(false)
                                        .setAction((event) -> {
                                            if (Util.isEmpty(event.getGuild().getAnnounces().getJoinAnnounce())) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "The Join Announce is not set! Please use `" + event.getPrefix() + "ann set join [MESSAGE]` to set it.").queue();
                                                return;
                                            }
                                            AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
                                            builder.append("The Join Announce Message will look like this when an user joins here:");
                                            builder.append("\n\n" + Announces.parse(event.getGuild().getAnnounces().getJoinAnnounce(), event.getOriginMember()));
                                            event.sendMessage(builder.build()).queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("leave")
                                        .setDescription("Returns you the Leave Announce Message.")
                                        .setName("Leave Announce Preview Command")
                                        .setPrivateAvailable(false)
                                        .setAction((event) -> {
                                            if (Util.isEmpty(event.getGuild().getAnnounces().getLeaveAnnounce())) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "The Leave Announce is not set! Please use `" + event.getPrefix() + "ann set leave [MESSAGE]` to set it.").queue();
                                                return;
                                            }
                                            AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
                                            builder.append("The Leave Announce Message will look like this when an user joins here:");
                                            builder.append("\n\n" + Announces.parse(event.getGuild().getAnnounces().getLeaveAnnounce(), event.getOriginMember()));
                                            event.sendMessage(builder.build()).queue();
                                        })
                                        .build())
                                .addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
                                        .setAliases("joindm")
                                        .setDescription("Returns you the Join DM Announce Message.")
                                        .setName("JoinDM Announce Preview Command")
                                        .setPrivateAvailable(false)
                                        .setAction((event) -> {
                                            if (Util.isEmpty(event.getGuild().getAnnounces().getJoinDMAnnounce())) {
                                                event.sendMessage(Quote.getQuote(Quote.FAIL) + "The Join DM Announce is not set! Please use `" + event.getPrefix() + "ann set joindm [MESSAGE]` to set it.").queue();
                                                return;
                                            }
                                            AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
                                            builder.append("The Join DM Announce Message will look like this when an user joins here:");
                                            builder.append("\n\n" + Announces.parse(event.getGuild().getAnnounces().getJoinDMAnnounce(), event.getOriginMember()));
                                            event.sendMessage(builder.build()).queue();
                                        })
                                        .build())
                                .build())
                        .build());
    }
}
