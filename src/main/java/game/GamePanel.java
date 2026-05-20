package game;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class GamePanel extends JPanel {
    private GameModel model;
    
    public GamePanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(GameModel.WIDTH, GameModel.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (model.gameOver) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER - Score: " + model.score + " Wave: " + model.wave, GameModel.WIDTH / 2 - 80, GameModel.HEIGHT / 2);
            return;
        }
        
        // UI
        g.setColor(Color.WHITE);
        g.drawString("SCORE: " + model.score, 10, 20);
        g.drawString("WAVE: " + model.wave, GameModel.WIDTH - 80, 20);
        
        // Player
        g.setColor(Color.BLUE);
        g.fillRect(model.player.x, model.player.y, model.player.width, model.player.height);
        
        // Bullets
        g.setColor(Color.YELLOW);
        for (Bullet b : model.bullets) {
            g.fillRect(b.x, b.y, b.width, b.height);
        }
        
        // Enemies
        g.setColor(Color.RED);
        for (Enemy e : model.enemies) {
            g.fillRect(e.x, e.y, e.width, e.height);
        }
        
        // Boss
        if (model.boss != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(model.boss.x, model.boss.y, model.boss.width, model.boss.height);
            g.setColor(Color.WHITE);
            g.drawString("HP: " + model.boss.hp, model.boss.x + 30, model.boss.y + 25);
        }
        
        // PowerUps
        g.setColor(Color.GREEN);
        for (PowerUp p : model.powerUps) {
            g.fillRect(p.x, p.y, p.width, p.height);
        }
    }
}
