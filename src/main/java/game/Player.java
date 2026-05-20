package game;

/**
 * 玩家類別。
 * 繼承自 Flyings，管理玩家的狀態與移動限制。
 */
public class Player extends Flyings {
    
    private int speed;
    private int lives;
    private int invincibleTick;
    private int weaponLevel;
    private int powerUpTick;
    private int fireTick;
    
    public Player(int x, int y) {
        super(x, y, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        this.speed = GameConfig.PLAYER_SPEED;
        this.lives = 8; // 開發者模式：8 條命方便測試
        this.invincibleTick = 0;
        this.weaponLevel = 1; // 預設武器等級
        this.powerUpTick = 0;
        this.fireTick = 0; // 新增射擊冷卻計時器
        // 將統一的圖片賦予該實體
        this.setImage(ImageManager.playerImg);
    }
    
    /**
     * 玩家的預設 step() 不自動移動，而是透過鍵盤事件呼叫 moveLeft() 或 moveRight()
     */
    @Override
    public void step() {
        // Player 的移動是由外部輸入控制，因此這裡保持空白
    }
    
    /**
     * 玩家向左移動，並進行邊界檢查。
     */
    public void moveLeft() {
        int newX = getX() - speed;
        // 邊界卡死：不可超出畫面左方
        if (newX < 0) {
            newX = 0;
        }
        setX(newX);
    }
    
    /**
     * 玩家向右移動，並進行邊界檢查。
     */
    public void moveRight() {
        int newX = getX() + speed;
        // 邊界卡死：不可超出畫面右方
        if (newX > GameConfig.SCREEN_WIDTH - getWidth()) {
            newX = GameConfig.SCREEN_WIDTH - getWidth();
        }
        setX(newX);
    }
    
    /**
     * 發射子彈的邏輯。
     * 回傳一個包含產生子彈的 List 物件，支援散彈或多重攻擊等級。
     * 
     * @return 新的 Bullet 清單
     */
    public java.util.List<Bullet> shoot() {
        java.util.List<Bullet> bullets = new java.util.ArrayList<>();
        
        // 冷卻中不允許發射
        if (fireTick > 0) {
            return bullets;
        }
        
        // 發射後重置冷卻時間 (將射速下調，拉長冷卻幀數)
        fireTick = 25;
        
        int bulletY = getY() - GameConfig.BULLET_HEIGHT;
        
        if (weaponLevel == 1) {
            // Level 1: 單發置中
            int bulletX = getX() + (getWidth() / 2) - (GameConfig.BULLET_WIDTH / 2);
            bullets.add(new Bullet(bulletX, bulletY, 0, -GameConfig.BULLET_SPEED));
        } else if (weaponLevel == 2) {
            // Level 2: 雙發並排
            int bullet1X = getX() + 10;
            int bullet2X = getX() + getWidth() - 15 - GameConfig.BULLET_WIDTH;
            bullets.add(new Bullet(bullet1X, bulletY, 0, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bullet2X, bulletY, 0, -GameConfig.BULLET_SPEED));
        } else if (weaponLevel == 3) {
            // Level 3: 3發散射
            int bulletX = getX() + (getWidth() / 2) - (GameConfig.BULLET_WIDTH / 2);
            bullets.add(new Bullet(bulletX, bulletY, 0, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bulletX, bulletY, -3, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bulletX, bulletY, 3, -GameConfig.BULLET_SPEED));
        } else if (weaponLevel >= 4) {
            // Level 4: 4發大角度散射
            int bulletX = getX() + (getWidth() / 2) - (GameConfig.BULLET_WIDTH / 2);
            bullets.add(new Bullet(bulletX, bulletY, -5, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bulletX, bulletY, -2, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bulletX, bulletY, 2, -GameConfig.BULLET_SPEED));
            bullets.add(new Bullet(bulletX, bulletY, 5, -GameConfig.BULLET_SPEED));
        }
        
        return bullets;
    }
    
    /**
     * 更新無敵計時器，於遊戲迴圈每幀呼叫。
     */
    public void updateInvincibility() {
        if (invincibleTick > 0) {
            invincibleTick--;
        }
        if (fireTick > 0) {
            fireTick--; // 同步更新射擊冷卻
        }
    }
    
    /**
     * 更新武器升級計時器，於遊戲迴圈每幀呼叫。
     */
    public void updatePowerUp() {
        if (powerUpTick > 0) {
            powerUpTick--;
            if (powerUpTick == 0) {
                weaponLevel = 1; // 時間到，降階回基礎武器
            }
        }
    }
    
    /**
     * 升級武器等級，並刷新強化時間。
     */
    public void upgradeWeapon() {
        weaponLevel = Math.min(weaponLevel + 1, 4); // 上限提高至 4
        powerUpTick = 600; // 約 10 秒 (以 60 FPS 計算)
    }
    
    /**
     * 治癒。回復一條生命。
     */
    public void heal() {
        lives = Math.min(lives + 1, 8);
    }
    
    /**
     * 上護盾。給予 3 秒無敵時間。
     */
    public void addShield() {
        invincibleTick = 180;
    }

    public int getWeaponLevel() {
        return weaponLevel;
    }
    
    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
        invincibleTick = 120; // 約 2 秒無敵時間 (60 fps)
    }
    
    public boolean isInvincible() {
        return invincibleTick > 0;
    }
    
    public int getInvincibleTick() {
        return invincibleTick;
    }
    
    public int getLives() {
        return lives;
    }
    
    /**
     * 玩家不會飛出螢幕 (受到邊界保護)。
     */
    @Override
    public boolean outOfBounds() {
        return false; 
    }
}
