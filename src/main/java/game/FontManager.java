package game;

import java.awt.Font;
import java.io.File;

/**
 * 字體資源管理器。
 * 負責在遊戲初始階段載入復古的 Arcade 字體，並提供不同大小的 Getter 給 View 渲染。
 * 具備 Fallback 防護，若找不到特定 ttf 檔案則自動退回系統預設安全字體。
 */
public class FontManager {
    
    // 預先產生固定大小的字體物件，節省重複 generate 的效能
    private static Font smallFont;
    private static Font largeFont;

    static {
        loadFonts();
    }

    private static void loadFonts() {
        try {
            File fontFile = new File("src/main/resources/fonts/arcade.ttf");
            if (fontFile.exists()) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                smallFont = baseFont.deriveFont(20f); // 20pt給左上角計分
                largeFont = baseFont.deriveFont(50f); // 50pt給遊戲結束畫面
            } else {
                throw new java.io.FileNotFoundException("找不到字體檔：" + fontFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[FontManager 警告] 無法載入 arcade.ttf，改用 Monospaced 防護字體。");
            e.printStackTrace();
            smallFont = new Font("Monospaced", Font.BOLD, 20);
            largeFont = new Font("Monospaced", Font.BOLD, 50);
        }
    }

    public static Font getSmallFont() {
        return smallFont;
    }

    public static Font getLargeFont() {
        return largeFont;
    }
}
