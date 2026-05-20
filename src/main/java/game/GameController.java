package game;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Random;

public class GameController implements Runnable, KeyListener {
    private GameModel model;
    private GamePanel view;
    private Thread gameThread;
    private boolean running = false;
    
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    private boolean spaceHandled = false;
    
    private int enemyDirection = 1;
    private double baseEnemySpeed = 2.0;
    private double currentEnemySpeed = 2.0;
    private Random random = new Random();
    
    public GameController(GameModel model, GamePanel view) {
        this.model = model;
        this.view = view;
        this.view.addKeyListener(this);
        initWave();
    }
    
    private void initWave() {
        model.enemies.clear();
        model.powerUps.clear();
        model.bullets.clear();
        model.boss = null;
        enemyDirection = 1;
        currentEnemySpeed = baseEnemySpeed + (model.wave - 1) * 0.5;
        
        if (model.wave % 5 == 0) {
            model.boss = new Boss(GameModel.WIDTH / 2 - 50, 50);
        } else {
            int rows = 4;
            int cols = 8;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    model.enemies.add(new Enemy(50 + c * 60, 50 + r * 40));
                }
            }
        }
    }
    
    public void startGame() {
        if (gameThread == null) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if (delta >= 1) {
                update();
                view.repaint();
                delta--;
            }
            
            try { Thread.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    
    private void update() {
        if (model.gameOver) return;
        
        // Player move
        if (leftPressed && model.player.x > 0) model.player.x -= model.player.speed;
        if (rightPressed && model.player.x < GameModel.WIDTH - model.player.width) model.player.x += model.player.speed;
        
        // Shooting
        if (spacePressed && !spaceHandled) {
            if (model.player.hasDoubleShot) {
                model.bullets.add(new Bullet(model.player.x, model.player.y));
                model.bullets.add(new Bullet(model.player.x + model.player.width - 5, model.player.y));
            } else {
                model.bullets.add(new Bullet(model.player.x + model.player.width / 2 - 2, model.player.y));
            }
            spaceHandled = true;
        }
        
        // Bullets move
        Iterator<Bullet> bit = model.bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            b.move();
            if (b.y < 0) bit.remove();
        }
        
        // PowerUps move
        Iterator<PowerUp> pit = model.powerUps.iterator();
        while (pit.hasNext()) {
            PowerUp p = pit.next();
            p.move();
            if (p.intersects(model.player)) {
                model.player.hasDoubleShot = true;
                pit.remove();
                model.score += 50;
            } else if (p.y > GameModel.HEIGHT) {
                pit.remove();
            }
        }
        
        // Enemies move
        boolean hitWall = false;
        int dx = (int)(currentEnemySpeed * enemyDirection);
        for (Enemy e : model.enemies) {
            e.move(dx, 0);
            if (e.x <= 0 || e.x >= GameModel.WIDTH - e.width) hitWall = true;
        }
        if (hitWall) {
            enemyDirection *= -1;
            for (Enemy e : model.enemies) e.move(0, 20);
        }
        
        // Boss move
        if (model.boss != null) {
            model.boss.move(dx, 0);
            if (model.boss.x <= 0 || model.boss.x >= GameModel.WIDTH - model.boss.width) {
                enemyDirection *= -1;
                model.boss.move(0, 20);
            }
        }
        
        // Collisions: Bullets vs Enemies/Boss
        bit = model.bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            boolean bulletRemoved = false;
            
            if (model.boss != null && b.intersects(model.boss)) {
                model.boss.hp--;
                bulletRemoved = true;
                if (model.boss.hp <= 0) {
                    model.score += 500;
                    spawnPowerUp(model.boss.x + model.boss.width/2, model.boss.y);
                    model.boss = null;
                }
            } else {
                Iterator<Enemy> eit = model.enemies.iterator();
                while (eit.hasNext()) {
                    Enemy e = eit.next();
                    if (b.intersects(e)) {
                        model.score += 10;
                        spawnPowerUp(e.x + e.width/2, e.y);
                        eit.remove();
                        bulletRemoved = true;
                        break;
                    }
                }
            }
            if (bulletRemoved) bit.remove();
        }
        
        // Collisions: Player vs Enemies/Boss
        if (model.boss != null && model.boss.intersects(model.player)) model.gameOver = true;
        if (model.boss != null && model.boss.y + model.boss.height >= GameModel.HEIGHT) model.gameOver = true;
        
        for (Enemy e : model.enemies) {
            if (e.intersects(model.player) || e.y + e.height >= GameModel.HEIGHT) {
                model.gameOver = true;
            }
        }
        
        // Wave progression
        if (model.enemies.isEmpty() && model.boss == null) {
            model.wave++;
            initWave();
        }
    }
    
    private void spawnPowerUp(int x, int y) {
        if (random.nextInt(100) < 5) { // 5% chance
            model.powerUps.add(new PowerUp(x, y));
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_SPACE) spacePressed = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) {
            spacePressed = false;
            spaceHandled = false;
        }
    }
}
