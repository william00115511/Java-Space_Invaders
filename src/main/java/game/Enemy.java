package game;

/**
 * 敵人類別。
 * 繼承自 Flyings，管理敵人的狀態 (存活、方向) 與移動邏輯。
 * 支援多兵種 (type): 0=一般, 1=坦克, 2=狙擊手
 */
public class Enemy extends Flyings {
    
    // 兵種常數
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_TANK = 1;
    public static final int TYPE_SNIPER = 2;
    
    // 水平移動速度
    private int speedX;
    // 敵人當前的移動方向：1 代表往右，-1 代表往左
    private int direction; 
    
    // 紀錄是否存活 (用於死亡判定，true 為存活，false 為準備從陣列中移除)
    private boolean alive;
    
    // 兵種與生命值
    private int type;
    private int hp;
    private int scoreValue; // 擊殺獎勵分數
    private int hitFlashTick; // 受擊閃白計時器
    
    public Enemy(int x, int y) {
        this(x, y, TYPE_NORMAL);
    }
    
    public Enemy(int x, int y, int type) {
        super(x, y, GameConfig.ENEMY_WIDTH, GameConfig.ENEMY_HEIGHT);
        this.speedX = GameConfig.ENEMY_SPEED_X;
        this.direction = 1; // 預設向右移動
        this.alive = true;
        this.type = type;
        
        // 根據兵種設定 HP、分數與圖片
        switch (type) {
            case TYPE_TANK:
                this.hp = 2;
                this.scoreValue = 20;
                this.setImage(ImageManager.enemyTankImg);
                break;
            case TYPE_SNIPER:
                this.hp = 1;
                this.scoreValue = 15;
                this.setImage(ImageManager.enemySniperImg);
                break;
            default: // TYPE_NORMAL
                this.hp = 1;
                this.scoreValue = 10;
                this.setImage(ImageManager.enemyImg);
                break;
        }
    }

    /**
     * 敵人每幀的預設移動邏輯 (水平移動)。
     * 撞牆後的向下移動與反向邏輯統一由 GameModel 處理。
     */
    @Override
    public void step() {
        if (alive) {
             setX(getX() + (speedX * direction));
        }
        if (hitFlashTick > 0) {
            hitFlashTick--;
        }
    }
    
    /**
     * 觸碰到左右邊界時呼叫。
     * 向下移動並反轉水平方向。
     */
    public void dropAndReverse() {
        setY(getY() + GameConfig.ENEMY_DROP_SPEED);
        direction = -direction; // 反向
    }
    
    /**
     * 扣血。回傳該敵人是否已經死亡。
     * @return true 表示已死亡，應移除
     */
    public boolean takeDamage() {
        hp--;
        if (hp <= 0) {
            alive = false;
            return true; // 死亡
        }
        hitFlashTick = 3; // 受擊閃白 3 幀
        return false; // 還活著
    }
    
    /**
     * 確認 Enemy 是否抵達畫面最下方。
     */
    @Override
    public boolean outOfBounds() {
        return getY() > GameConfig.SCREEN_HEIGHT;
    }
    
    // --- Getters & Setters ---

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public int getDirection() {
         return direction;
    }
    
    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }
    
    public int getType() {
        return type;
    }
    
    public int getHp() {
        return hp;
    }
    
    public int getScoreValue() {
        return scoreValue;
    }
    
    public int getHitFlashTick() {
        return hitFlashTick;
    }
}
