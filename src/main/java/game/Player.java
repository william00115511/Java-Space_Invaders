package game;
public class Player extends Entity {
    public int speed = 5;
    public boolean hasDoubleShot = false;
    public Player(int x, int y) {
        super(x, y, 50, 20);
    }
}
