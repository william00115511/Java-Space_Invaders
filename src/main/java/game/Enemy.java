package game;
public class Enemy extends Entity {
    public Enemy(int x, int y) {
        super(x, y, 40, 20);
    }
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
