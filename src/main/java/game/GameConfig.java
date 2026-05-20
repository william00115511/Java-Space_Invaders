package game;

/**
 * 遊戲設定常數類別。
 * 集中管理所有靜態常數，避免 Magic Numbers 散步在程式碼中。
 */
public class GameConfig {
    
    // --- 視窗設定 ---
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final String GAME_TITLE = "Space Invaders";
    
    // --- 遊戲邏輯設定 ---
    public static final int FPS = 60; // Game loop 更新頻率 (約 16ms 一次更新)
    
    // --- 玩家設定 ---
    public static final int PLAYER_WIDTH = 50;
    public static final int PLAYER_HEIGHT = 30;
    public static final int PLAYER_SPEED = 5;
    public static final int PLAYER_Y_POS = SCREEN_HEIGHT - 80; // 固定玩家在畫面下方
    
    // --- 敵人設定 ---
    public static final int ENEMY_WIDTH = 40;
    public static final int ENEMY_HEIGHT = 30;
    public static final int ENEMY_SPEED_X = 2; // 水平移動速度
    public static final int ENEMY_DROP_SPEED = 30; // 觸碰邊界後向下移動的距離
    
    // --- 子彈設定 ---
    public static final int BULLET_WIDTH = 5;
    public static final int BULLET_HEIGHT = 15;
    public static final int BULLET_SPEED = 10;
    public static final int ENEMY_BULLET_SPEED = 6;
}
