package game;
public class PowerUp extends Entity {
    public int speed = 3;
    public PowerUp(int x, int y) {
        super(x, y, 15, 15);
    }
    public void move() {
        y += speed;
    }
}
