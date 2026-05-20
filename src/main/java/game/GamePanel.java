package game;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Thread gameThread;
    private boolean running = false;
    
    // Player
    private int playerX = WIDTH / 2 - 25;
    private int playerY = HEIGHT - 50;
    private final int playerWidth = 50;
    private final int playerHeight = 20;
    private int playerSpeed = 5;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    // Bullet
    private int bulletX = -1;
    private int bulletY = -1;
    private boolean bulletActive = false;
    private final int bulletWidth = 5;
    private final int bulletHeight = 15;
    private final int bulletSpeed = 10;
    
    // Enemies
    private List<Enemy> enemies;
    private int enemyDirection = 1; // 1 for right, -1 for left
    private int enemySpeed = 2;
    
    // Game State
    private boolean gameOver = false;
    private boolean youWin = false;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initEnemies();
    }
    
    private void initEnemies() {
        enemies = new ArrayList<>();
        int rows = 4;
        int cols = 8;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                enemies.add(new Enemy(50 + c * 60, 50 + r * 40, 40, 20));
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
                repaint();
                delta--;
            }
            
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void update() {
        if (gameOver || youWin) return;
        
        // Update Player
        if (leftPressed && playerX > 0) {
            playerX -= playerSpeed;
        }
        if (rightPressed && playerX < WIDTH - playerWidth) {
            playerX += playerSpeed;
        }
        
        // Update Bullet
        if (bulletActive) {
            bulletY -= bulletSpeed;
            if (bulletY < 0) {
                bulletActive = false;
            }
        }
        
        // Update Enemies
        boolean hitWall = false;
        for (Enemy e : enemies) {
            e.x += enemySpeed * enemyDirection;
            if (e.x <= 0 || e.x >= WIDTH - e.width) {
                hitWall = true;
            }
        }
        
        if (hitWall) {
            enemyDirection *= -1;
            for (Enemy e : enemies) {
                e.y += 20; // Move down
            }
        }
        
        // Collisions
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            // Bullet hits enemy
            if (bulletActive && 
                bulletX < e.x + e.width && bulletX + bulletWidth > e.x &&
                bulletY < e.y + e.height && bulletY + bulletHeight > e.y) {
                it.remove();
                bulletActive = false;
                continue;
            }
            // Enemy hits player
            if (playerX < e.x + e.width && playerX + playerWidth > e.x &&
                playerY < e.y + e.height && playerY + playerHeight > e.y) {
                gameOver = true;
            }
            // Enemy reaches bottom
            if (e.y + e.height >= HEIGHT) {
                gameOver = true;
            }
        }
        
        if (enemies.isEmpty()) {
            youWin = true;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameOver) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", WIDTH / 2 - 30, HEIGHT / 2);
            return;
        }
        if (youWin) {
            g.setColor(Color.WHITE);
            g.drawString("YOU WIN", WIDTH / 2 - 25, HEIGHT / 2);
            return;
        }
        
        // Draw Player
        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, playerWidth, playerHeight);
        
        // Draw Bullet
        if (bulletActive) {
            g.setColor(Color.YELLOW);
            g.fillRect(bulletX, bulletY, bulletWidth, bulletHeight);
        }
        
        // Draw Enemies
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            g.fillRect(e.x, e.y, e.width, e.height);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            if (!bulletActive && !gameOver && !youWin) {
                bulletActive = true;
                bulletX = playerX + playerWidth / 2 - bulletWidth / 2;
                bulletY = playerY;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
    
    private static class Enemy {
        int x, y, width, height;
        
        Enemy(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
