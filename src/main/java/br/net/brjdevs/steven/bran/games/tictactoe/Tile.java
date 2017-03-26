package br.net.brjdevs.steven.bran.games.tictactoe;

public class Tile {
    
    private int player;
    
    public Tile() {
        player = -1;
    }
    
    public boolean isFree() {
        return player == -1;
    }
    
    public int getPlayer() {
        return player;
    }
    
    public void setPlayer(int player) {
        this.player = player;
    }
}
