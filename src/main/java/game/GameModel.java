package game;

import java.util.List;
import java.util.List;

/**
 * 遊戲資料模型 (Model)。
 * 用於存放所有角色的狀態、物件清單、分數以及當前的遊戲狀態。
 * GameModel 是無狀態的 (Stateless UI / View-agnostic)，它不知道怎麼畫圖，僅保存純資料。
 */
public class GameModel {
    
    // 遊戲狀態常數
    public static final int START = 0;       // 準備開始
    public static final int RUNNING = 1;     // 遊戲進行中
    public static final int PAUSE = 2;       // 暫停
    public static final int GAME_OVER = 3;   // 遊戲結束
    public static final int VICTORY = 4;     // 勝利
    public static final int WAVE_TRANSITION = 5; // Phase 6.6: 波次推進過場
    public static final int TITLE_SCREEN = 6;    // Phase 7.5.1: 標題閃屏畫面
    public static final int CAMPAIGN_INTRO = 7;  // Phase 7.6: 戰役開場過場
    public static final int CAMPAIGN_OUTRO = 8;  // Phase 7.6: 戰役結尾過場
    
    // 核心狀態變數
    private int state;
    private int score;
    private int currentWave;
    private int highScore;
    private int shakeTick;       // 螢幕震動剩餘幀數
    private int shakeIntensity;  // 震動幅度 (像素)
    private int menuIndex;       // 選單所在位置 (0: 戰役, 1: 無盡, 2: 離開)
    private int pauseIndex;      // 暫停選單所在位置 (0: 繼續, 1: 回主選單)
    private int gameMode;        // 當前遊戲模式 (0: 戰役, 1: 無盡)
    private int cinematicTick;   // 動畫/過場計時器
    
    // 實體物件清單 (Data Structures)
    // 遊戲實體
    private Player player;
    private List<Enemy> enemies;
    private Boss boss; // Phase 6.4: 史詩級 Boss
    private List<Bullet> bullets;
    private List<EnemyBullet> enemyBullets;
    private List<Explosion> explosions;
    private List<PowerUp> powerUps; // 畫面上掉落的道具
    private List<Star> stars;
    
    public GameModel() {
        this.enemies = new java.util.ArrayList<>();
        this.bullets = new java.util.ArrayList<>();
        this.enemyBullets = new java.util.ArrayList<>();
        this.explosions = new java.util.ArrayList<>();
        this.powerUps = new java.util.ArrayList<>();
        this.stars = new java.util.ArrayList<>();
        initGame();
        
        // 從本地端安全拉取最高分
        this.highScore = ScoreManager.loadHighScore();
        // 初始化選單位置與模式
        this.menuIndex = 0;
        this.gameMode = 0;
        // 非常關鍵：遊戲第一次開啟時，必須強制進入 TITLE_SCREEN (標題準備畫面)
        // 否則 initGame() 預設的 RUNNING 狀態會導致遊戲直接開始，且略過按下 Space 的 BGM 觸發事件！
        this.state = TITLE_SCREEN;
    }
    
    /**
     * 初始化 / 重設遊戲狀態。
     */
    public void initGame() {
        this.state = RUNNING;
        this.score = 0;
        this.currentWave = 1;
        this.shakeTick = 0;
        this.shakeIntensity = 0;
        
        // 初始化玩家位置，置中於畫面下方
        int startX = (GameConfig.SCREEN_WIDTH - GameConfig.PLAYER_WIDTH) / 2;
        this.player = new Player(startX, GameConfig.PLAYER_Y_POS);
        
        // 初始化敵人陣列與子彈陣列
        this.enemies.clear();
        this.boss = null; // 重置 Boss
        this.bullets.clear();
        this.enemyBullets.clear();
        this.explosions.clear();
        this.powerUps.clear();
        this.stars.clear();
        
        // 建立背景隨機星星 (50 ~ 80 顆，此處固定為 80 顆營造星空感)
        for (int i = 0; i < 80; i++) {
            int x = (int)(Math.random() * GameConfig.SCREEN_WIDTH);
            int y = (int)(Math.random() * GameConfig.SCREEN_HEIGHT);
            int size = (int)(Math.random() * 3) + 1; // 1 ~ 3 pixel 大小
            int speed = (int)(Math.random() * 3) + 1; // 1 ~ 3 落速 (製造視差 Parallax)
            // 隨機灰度產生深淺不同的星星
            int shade = (int)(Math.random() * 155) + 100; // 100 ~ 255
            java.awt.Color starColor = new java.awt.Color(shade, shade, shade);
            stars.add(new Star(x, y, size, speed, starColor));
        }
        
        // 建立 4 列 8 行的敵人陣列
        startNextWave();
        // 第一次啟動因為 currentWave 是 1，所以速度不會疊加，直接等於 GameConfig.ENEMY_SPEED_X
        this.currentWave = 1; // 為了保證 initGame 的語意正確
    }
    
    /**
     * 開啟新的波次。
     * 推進 currentWave，清空殘留物，並重生速度更快的敵人。
     */
    public void startNextWave() {
        this.currentWave++;
        
        this.enemies.clear();
        this.boss = null;
        this.bullets.clear();
        this.enemyBullets.clear();
        this.powerUps.clear();
        this.explosions.clear();
        
        int startEnemyX = 50;
        int startEnemyY = 50;
        int spacingX = 60;
        int spacingY = 50;
        
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                int px = startEnemyX + col * spacingX;
                int py = startEnemyY + row * spacingY;
                
                // 根據波次決定兵種配置
                int type = Enemy.TYPE_NORMAL;
                int wave = this.currentWave;
                
                if (wave == 1) {
                    // Wave 1: 全部普通兵
                    type = Enemy.TYPE_NORMAL;
                } else if (wave == 2) {
                    // Wave 2: 上排 (row 0) 是坦克肉盾，其餘普通
                    type = (row == 0) ? Enemy.TYPE_TANK : Enemy.TYPE_NORMAL;
                } else if (wave == 3) {
                    // Wave 3: U 字陣型 - 兩翼狙擊手、中間坦克
                    boolean isFlank = (col <= 1 || col >= 6);
                    if (isFlank && row >= 2) {
                        type = Enemy.TYPE_SNIPER; // 兩翼後排 = 狙擊手
                    } else if (row == 0) {
                        type = Enemy.TYPE_TANK;   // 前排 = 坦克
                    } else {
                        type = Enemy.TYPE_NORMAL;
                    }
                } else {
                    // Wave 4+: 經典戰術混編
                    boolean isCorner = (col <= 1 || col >= 6) && (row >= 2);
                    boolean isCenterFront = (row == 0) && (col >= 2 && col <= 5);
                    
                    if (isCorner) {
                        type = Enemy.TYPE_SNIPER;  // 四角落 = 狙擊手
                    } else if (isCenterFront || row == 0) {
                        type = Enemy.TYPE_TANK;    // 前排中央 = 坦克肉盾
                    } else {
                        type = Enemy.TYPE_NORMAL;
                    }
                }
                
                Enemy enemy = new Enemy(px, py, type);
                int speedIncrease = this.currentWave - 1;
                enemy.setSpeedX(GameConfig.ENEMY_SPEED_X + speedIncrease);
                enemies.add(enemy);
            }
        }
    }

    // --- Getters & Setters ---
    // 提供給 View 與 Controller 的唯一介面，不暴露物件內部實作細節。

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public Boss getBoss() {
        return boss;
    }
    
    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public List<EnemyBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public List<Star> getStars() {
        return stars;
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public int getHighScore() {
        return highScore;
    }
    
    public void setHighScore(int score) {
        this.highScore = score;
    }
    
    public void addBullet(Bullet b) {
        this.bullets.add(b);
    }

    public void addEnemyBullet(EnemyBullet b) {
        this.enemyBullets.add(b);
    }

    public void addExplosion(Explosion e) {
        this.explosions.add(e);
    }
    
    // --- 螢幕震動系統 ---
    
    public void triggerShake(int ticks, int intensity) {
        this.shakeTick = ticks;
        this.shakeIntensity = intensity;
    }
    
    public void updateShake() {
        if (shakeTick > 0) {
            shakeTick--;
        }
    }
    
    public int getShakeTick() {
        return shakeTick;
    }
    
    public int getShakeIntensity() {
        return shakeIntensity;
    }
    
    public int getMenuIndex() {
        return menuIndex;
    }
    
    public void setMenuIndex(int index) {
        this.menuIndex = index;
    }
    
    public int getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(int mode) {
        this.gameMode = mode;
    }
    
    public int getCinematicTick() {
        return cinematicTick;
    }
    
    public void setCinematicTick(int tick) {
        this.cinematicTick = tick;
    }
    
    public int getPauseIndex() {
        return pauseIndex;
    }
    
    public void setPauseIndex(int index) {
        this.pauseIndex = index;
    }
}
