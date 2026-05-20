package game;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders MVP - MVC & Waves");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        GameModel model = new GameModel();
        GamePanel view = new GamePanel(model);
        GameController controller = new GameController(model, view);
        
        frame.add(view);
        frame.pack();
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        controller.startGame();
    }
}
