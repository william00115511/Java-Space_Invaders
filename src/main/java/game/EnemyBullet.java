package game;

/**
 * 敵軍子彈類別。
 * 繼承自 Flyings，代表敵人發射出的直線飛行物。
 */
public class EnemyBullet extends Flyings {
    
    private int speed;
    private int speedX; // 支援 X 軸向的散射速度
    
    // 預設給小兵使用：只有 Y 軸垂直下落
    public EnemyBullet(int x, int y) {
        this(x, y, 0); // 委派給支援 speedX 的建構子
    }

    // 進階版建構子：支援 Boss 的扇形散射子彈
    public EnemyBullet(int x, int y, int speedX) {
        super(x, y, GameConfig.BULLET_WIDTH, GameConfig.BULLET_HEIGHT);
        this.speed = GameConfig.ENEMY_BULLET_SPEED;
        this.speedX = speedX;
        this.setImage(ImageManager.enemyBulletImg);
    }

    /**
     * 子彈的移動邏輯。
     * 敵軍的子彈往下發射，且若搭載了 speedX 則會產生斜向散射軌跡。
     */
    @Override
    public void step() {
        setY(getY() + speed);
        setX(getX() + speedX); // 同步套用水平速度，支援扇形散彈
    }
    
    /**
     * 偵測子彈是否已經飛出畫面底端。
     */
    @Override
    public boolean outOfBounds() {
        return getY() > GameConfig.SCREEN_HEIGHT; 
    }
}
