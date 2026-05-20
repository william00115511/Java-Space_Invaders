package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 分數管理器 (ScoreManager)
 * 負責處理遊戲最高分紀錄的本地端讀寫作業。
 */
public class ScoreManager {
    
    private static final String FILE_PATH = "highscore.txt";

    /**
     * 讀取最高分紀錄。
     * @return 檔案中紀錄的分數，若檔案不存在、異常或解析失敗則回傳 0。
     */
    public static int loadHighScore() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return 0; // 初次遊玩，沒有紀錄
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("無法讀取最高分紀錄: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 儲存全新的最高分紀錄至本地端檔案。
     * @param score 要寫入的最高分
     */
    public static void saveHighScore(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(score));
        } catch (IOException e) {
            System.err.println("儲存最高分紀錄失敗: " + e.getMessage());
        }
    }
}
