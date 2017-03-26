package br.net.brjdevs.steven.bran.games.tictactoe;

import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import br.net.brjdevs.steven.bran.games.engine.AbstractGame;
import br.net.brjdevs.steven.bran.games.engine.GameInfo;
import br.net.brjdevs.steven.bran.games.engine.GameLocation;
import br.net.brjdevs.steven.bran.games.engine.event.LooseEvent;
import br.net.brjdevs.steven.bran.games.engine.event.WinEvent;
import br.net.brjdevs.steven.bran.games.tictactoe.events.InvalidMoveEvent;
import br.net.brjdevs.steven.bran.games.tictactoe.events.MoveEvent;
import net.dv8tion.jda.core.entities.MessageChannel;

public class TicTacToe extends AbstractGame<TicTacToeListener> {
    
    private static int[][] winCombos = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            
            {0, 4, 8},
            {2, 4, 6}
    };
    
    private Tile[] board;
    private int turn;
    private long lastPlay0;
    private long lastPlay1;
    
    public TicTacToe(MessageChannel channel, UserData creator) {
        super(new GameLocation(channel), new GameInfo(creator, true, true), new TicTacToeListener(), channel.getJDA(), 600000 + System.currentTimeMillis());
    }
    
    public Tile[] getBoard() {
        return board;
    }
    
    public int getPlayer() {
        return turn;
    }
    
    public UserData getTurn() {
        return info.getPlayers().get(turn);
    }
    
    public void move(int player, int tilePos) {
        if (player != turn) {
            getLocation().send("\\" + Emojis.X + " It is not your turn!").queue();
            return;
        }
        Tile tile = board[tilePos];
        if (!tile.isFree()) {
            getEventListener().onInvalidMove(new InvalidMoveEvent(this, tile));
            return;
        }
        tile.setPlayer(player);
        turn = turn == 0 ? 1 : 0;
        getEventListener().onMove(new MoveEvent(this, tile));
        for (int[] combo : winCombos) {
            if (board[combo[0]].isFree())
                continue;
            if (board[combo[0]].getPlayer() == board[combo[1]].getPlayer()
                    && board[combo[1]].getPlayer() == board[combo[2]].getPlayer()) {
                getEventListener().onWin(new WinEvent(this));
                end();
                return;
            }
        }
        for (Tile t : board) {
            if (t.isFree())
                return;
        }
        getEventListener().onLoose(new LooseEvent(this));
        end();
    }
    
    @Override
    public String getName() {
        return "Tic Tac Toe";
    }
    
    @Override
    public boolean setup() {
        this.board = new Tile[9];
        for (int i = 0; i < board.length; i++) {
            board[i] = new Tile();
        }
        this.turn = 0;
        return true;
    }
    
    @Override
    public boolean leave(UserData user) {
        throw new UnsupportedOperationException("This is a UserVSUser, they can't leave!");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Tic Tac Toe**").append("\n\n");
        sb.append(Utils.getUser(getInfo().getPlayers().get(0).getUser(getShard().getJDA()))).append(" VS ").append(Utils.getUser(getInfo().getPlayers().get(1).getUser(getShard().getJDA())));
        sb.append("\n\n");
        for (int i = 0; i < board.length; i++) {
            Tile tile = board[i];
            if (!tile.isFree())
                sb.append(tile.getPlayer() == 0 ? ":x:" : ":o:");
            else {
                String s = "";
                switch (i) {
                    case 0:
                        s = "\u0031";
                        break;
                    case 1:
                        s = "\u0032";
                        break;
                    case 2:
                        s = "\u0033";
                        break;
                    case 3:
                        s = "\u0034";
                        break;
                    case 4:
                        s = "\u0035";
                        break;
                    case 5:
                        s = "\u0036";
                        break;
                    case 6:
                        s = "\u0037";
                        break;
                    case 7:
                        s = "\u0038";
                        break;
                    case 8:
                        s = "\u0039";
                        break;
                }
                sb.append(s).append('\u20e3');
            }
            sb.append((i + 1) % 3 != 0 ? "**|**" : "\n");
        }
        sb.append("\n\nIt's ").append(Utils.getUser(getTurn().getUser(getShard().getJDA()))).append("'s (").append(turn == 0 ? "X" : "O").append(") turn!\n\n");
        return sb.toString();
    }
}
