package game;
public class Boss extends Entity {
    public int hp = 10;
    public Boss(int x, int y) {
        super(x, y, 100, 50);
    }
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
