package game;

import javax.swing.SwingUtilities;

/**
 * 程式進入點 (Application Entry Point)。
 * 負責實例化 MVC 的各個組件並將其組合起來啟動遊戲。
 */
public class Main {
    
    public static void main(String[] args) {
        
        // 建議在 Swing 工具執行緒中啟動 GUI 應用程式，以確保執行緒安全
        SwingUtilities.invokeLater(() -> {
            
            // 1. 建立資料模型 (Model) - 負責存放所有狀態與座標資料
            GameModel model = new GameModel();
            
            // 2. 建立視圖 (View) - 負責根據 Model 更新畫面繪圖
            GamePanel view = new GamePanel(model);
            
            // 3. 建立控制器 (Controller) - 負責連結 Model 與 View，啟動 Game Loop，與接收鍵盤輸入
            GameController controller = new GameController(model, view);
            
            // 4. 建立主視窗，將 View 與 Controller 放進去
            GameFrame frame = new GameFrame(view, controller);
            
            // 5. 顯示視窗，主視窗顯示後，要求 GamePanel 取得鍵盤焦點
            frame.setVisible(true);
            view.requestFocusInWindow();
            
        });
    }
}
