package game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 圖片資源載入與管理器 (Utility)。
 * 在遊戲啟動階段預先載入所有 BufferedImage 到記憶體中。
 * 提供靜態方法供各實體或畫布取用，並包含找不到檔案時的 Fallback 防止當機機制。
 */
public class ImageManager {
    
    // 定義所有遊戲內使用到的圖片全域靜態變數
    public static BufferedImage playerImg;
    public static BufferedImage enemyImg;
    public static BufferedImage enemyTankImg;
    public static BufferedImage enemySniperImg;
    public static BufferedImage bulletImg;
    public static BufferedImage enemyBulletImg;
    public static BufferedImage bossImg;
    public static BufferedImage powerupWeaponImg;
    public static BufferedImage powerupHealImg;
    public static BufferedImage powerupShieldImg;
    public static BufferedImage[] explosionFrames = new BufferedImage[9];
    
    // 使用 static 區塊，在 ImageManager 第一次被參照時自動執行載入
    static {
        // 直接從專案的來源資料夾讀取圖片，以解決未使用 Maven 建置時目標資料夾遺失資源檔的問題
        playerImg = loadImage("src/main/resources/images/player.png");
        enemyImg = loadImage("src/main/resources/images/enemy.png");
        enemyTankImg = loadImage("src/main/resources/images/enemy_tank.png");
        enemySniperImg = loadImage("src/main/resources/images/enemy_sniper.png");
        bulletImg = loadImage("src/main/resources/images/bullet.png");
        enemyBulletImg = loadImage("src/main/resources/images/enemyBullet.png");
        bossImg = loadImage("src/main/resources/images/boss.png");
        powerupWeaponImg = loadImage("src/main/resources/images/powerup_weapon.png");
        powerupHealImg = loadImage("src/main/resources/images/powerup_heal.png");
        powerupShieldImg = loadImage("src/main/resources/images/powerup_shield.png");
        
        // 載入爆炸序列動畫圖檔 (explosion00.png ~ explosion08.png)
        for (int i = 0; i < 9; i++) {
            String suffix = String.format("%02d", i);
            explosionFrames[i] = loadImage("src/main/resources/images/explosion" + suffix + ".png");
        }
    }
    
    /**
     * 取得指定影格的爆炸特效圖片
     */
    public static BufferedImage getExplosionFrame(int index) {
        if (index >= 0 && index < 9) {
            return explosionFrames[index];
        }
        System.err.println("[ImageManager 警告] 請求的 Explosion 影格外溢: " + index);
        return null;
    }
    
    /**
     * 安全地載入本機圖片。
     * 
     * @param path 檔案相對於專案根目錄的路徑
     * @return BufferedImage，若找不到則回傳 null 且印出警告。
     */
    private static BufferedImage loadImage(String path) {
        try {
            // 首先嘗試作為 ClassPath 資源讀取 (適用於 JAR 檔與標準 IDE 資源配置)
            java.net.URL url = ImageManager.class.getResource("/" + path.replace("src/main/resources/", ""));
            if (url != null) {
                return ImageIO.read(url);
            }
            
            // 若 ClassPath 找不到，退回原本實體檔案讀取 (適用於無配置資源資料夾的傳統 IDE)
            File imgFile = new File(path);
            if (!imgFile.exists()) {
                System.err.println("[ImageManager 警告] 找不到圖片檔案: " + imgFile.getAbsolutePath());
                return null;
            }
            return ImageIO.read(imgFile);
        } catch (IOException e) {
            System.err.println("[ImageManager 錯誤] 無法讀取圖片檔案: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
