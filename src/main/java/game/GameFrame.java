package game;

import javax.swing.JFrame;

/**
 * 遊戲主視窗 (View Container)。
 * 將 GamePanel 鑲嵌其中，並負責管理視窗的生命週期、大小、標題等系統屬性。
 */
public class GameFrame extends JFrame {
    
    public GameFrame(GamePanel panel, GameController controller) {
        // 設定視窗標題
        setTitle(GameConfig.GAME_TITLE);
        
        // 設定視窗關閉時自動結束應用程式
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 將負責繪圖的 GamePanel 加入視窗中
        add(panel);
        
        // 確保 GamePanel 可以取得鍵盤焦點，並將事件交由 Controller 處理
        panel.setFocusable(true);
        panel.addKeyListener(controller);
        
        // 設定視窗尺寸 (注意: 如果 JFrame 沒有設定 Layout，pack() 會需要設定 preferredSize。
        // 為求簡單，我們直接使用 setSize，並考慮到作業系統邊框的耗損，這裡設大一點)
        setSize(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        
        // 固定視窗大小，避免玩家拉伸導致座標計算出錯
        setResizable(false);
        
        // 讓視窗顯示在螢幕正中央
        setLocationRelativeTo(null);
    }
}
