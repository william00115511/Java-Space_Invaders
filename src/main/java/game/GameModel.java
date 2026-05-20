package game;
import java.util.ArrayList;
import java.util.List;

public class GameModel {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    public Player player;
    public List<Enemy> enemies;
    public List<Bullet> bullets;
    public List<PowerUp> powerUps;
    public Boss boss;
    
    public int wave = 1;
    public int score = 0;
    public boolean gameOver = false;
    
    public GameModel() {
        player = new Player(WIDTH / 2 - 25, HEIGHT - 50);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        powerUps = new ArrayList<>();
    }
}
