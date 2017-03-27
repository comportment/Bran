package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.net.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ResponseTimeoutEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import br.net.brjdevs.steven.bran.games.engine.GameState;
import br.net.brjdevs.steven.bran.games.tictactoe.TicTacToe;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

public class TicTacToeCommand {
    
    private static final String ACCEPT = "\u2705", DENY = "\u274C";
    @Command
    private static ICommand tictactoe() {
        return new CommandBuilder(Category.FUN)
                .setAliases("tictactoe")
                .setName("TicTacToe Command")
                .setDescription("Play tic tac toe with someone!")
                .setAction((event) -> {
                    if (event.getUserData().getProfileData().getCurrentGame() != null) {
                        event.sendMessage("You have another game running!").queue();
                        return;
                    } else if (event.getMessage().getMentionedUsers().isEmpty()) {
                        event.sendMessage("You have to mention a user to play with you!").queue();
                        return;
                    }
                    User user = event.getMessage().getMentionedUsers().get(0);
                    if (user.isBot()) {
                        event.sendMessage("You cannot invite bots to play with you! " + Emojis.X + Emojis.ROBOT).queue();
                        return;
                    } else if (user.equals(event.getAuthor())) {
                        event.sendMessage("You can't play with yourself " + Emojis.FACEPALM).queue();
                        return;
                    }
                    UserData userData = Bran.getInstance().getDataManager().getData().get().getUserData(user);
                    if (userData.getProfileData().getCurrentGame() != null) {
                        event.sendMessage(Utils.getUser(user) + " is already playing another game!").queue();
                        return;
                    }
                    event.sendMessage(Utils.getUser(user) + ", you have been challenged by " + event.getAuthor().getName() + " to a Tic Tac Toe match! React to this message to accept or deny.").queue(msg -> {
                        msg.addReaction(ACCEPT).queue();
                        msg.addReaction(DENY).queue();
                        new ResponseWaiter(user, event.getTextChannel(), event.getShard(), 30000, new String[] {ACCEPT, DENY}, ExpectedResponseType.REACTION, responseEvent -> {
                            if (responseEvent instanceof ValidResponseEvent) {
                                String r = ((MessageReaction) ((ValidResponseEvent) responseEvent).response).getEmote().getName();
                                if (r.equals(ACCEPT)) {
                                    msg.delete().queue();
                                    TicTacToe ticTacToe = new TicTacToe(event.getChannel(), event.getUserData());
                                    ticTacToe.invite(userData);
                                    if (ticTacToe.getGameState() == GameState.ERRORED) {
                                        event.sendMessage("Could not start game!").queue();
                                        return;
                                    }
                                    event.sendMessage(ticTacToe.toString()).queue();
                                } else {
                                    msg.editMessage(Utils.getUser(user) + ", you've denied the invite.").queue();
                                }
                            } else if (responseEvent instanceof UnexpectedResponseEvent) {
                                msg.editMessage("You've reacted with an invalid reaction!").queue();
                            } else if (responseEvent instanceof ResponseTimeoutEvent) {
                                msg.editMessage("You took too long to accept or deny the invite!").queue();
                            }
                        });
                    });
                })
                .build();
    }
    
}
