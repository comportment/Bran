package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import br.com.brjdevs.steven.bran.games.engine.GameState;
import br.com.brjdevs.steven.bran.games.tictactoe.TicTacToe;
import net.dv8tion.jda.core.entities.User;

public class TicTacToeCommand {
    
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
                    TicTacToe ticTacToe = new TicTacToe(event.getChannel(), event.getUserData());
                    ticTacToe.invite(userData);
                    if (ticTacToe.getGameState() == GameState.ERRORED) {
                        event.sendMessage("Could not start game!").queue();
                        return;
                    }
                    event.sendMessage(ticTacToe.toString()).queue();
                })
                .build();
    }
    
}
