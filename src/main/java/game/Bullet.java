package game;

/**
 * 子彈類別。
 * 繼承自 Flyings，代表玩家發射出的直線飛行物。
 */
public class Bullet extends Flyings {
    
    private int speedX;
    private int speedY;
    
    public Bullet(int x, int y, int speedX, int speedY) {
        super(x, y, GameConfig.BULLET_WIDTH, GameConfig.BULLET_HEIGHT);
        this.speedX = speedX;
        this.speedY = speedY;
        this.setImage(ImageManager.bulletImg);
    }
    
    public Bullet(int x, int y) {
        this(x, y, 0, -GameConfig.BULLET_SPEED);
    }

    /**
     * 子彈的移動邏輯。
     * 直接加上速度向量。支援散彈散射軌跡。
     */
    @Override
    public void step() {
        setX(getX() + speedX);
        setY(getY() + speedY);
    }
    
    /**
     * 偵測子彈是否已經飛出畫面頂端。
     * 如果超出則回傳 true，讓 GameModel 可以將其從清單中移除以釋放記憶體。
     */
    @Override
    public boolean outOfBounds() {
        return getY() + getHeight() < 0; 
    }
}
