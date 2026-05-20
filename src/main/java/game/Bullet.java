package game;
public class Bullet extends Entity {
    public int speed = 10;
    public Bullet(int x, int y) {
        super(x, y, 5, 15);
    }
    public void move() {
        y -= speed;
    }
}
