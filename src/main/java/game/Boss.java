package game;

/**
 * 史詩級 Boss 實體。
 * 擁有巨大血條、來回掃射能力，為遊戲的終極挑戰。
 * 支援「狂暴轉階段」：HP 降至一半時進入不規則波浪軌跡。
 */
public class Boss extends Flyings {
    
    private int hp;
    private int maxHp;
    private int speedX;
    private int direction; // 1: 往右, -1: 往左
    private boolean dead;
    private boolean enraged;
    private int tick; // 動畫計時器（用於正弦波計算）
    private int baseY; // 基準 Y 座標（狂暴時用來做正弦浮動的中心點）
    private boolean canEnrage; // 是否允許進入狂暴模式

    public Boss(int x, int y, boolean canEnrage) {
        super(x, y, 150, 100);
        this.maxHp = 30;
        this.hp = maxHp;
        this.speedX = 2;
        this.direction = 1;
        this.dead = false;
        this.enraged = false;
        this.tick = 0;
        this.baseY = y;
        this.canEnrage = canEnrage;
        this.setImage(ImageManager.bossImg);
    }

    /**
     * Boss 左右來回掃蕩移動。
     * 狂暴後加入正弦波 Y 軸浮動。
     */
    @Override
    public void step() {
        if (!dead) {
            tick++;
            setX(getX() + (speedX * direction));
            
            // 碰觸到邊界時反轉方向
            if (getX() <= 0) {
                setX(0);
                direction = 1;
            } else if (getX() + getWidth() >= GameConfig.SCREEN_WIDTH) {
                setX(GameConfig.SCREEN_WIDTH - getWidth());
                direction = -1;
            }
            
            // 狂暴後：正弦波 Y 軸浮動
            if (enraged) {
                setY(baseY + (int)(Math.sin(tick * 0.1) * 40));
            }
        }
    }

    /**
     * 承受傷害。半血觸發狂暴轉階段。
     */
    public boolean takeDamage(int damage) {
        hp -= damage;
        
        // 半血觸發狂暴 (必須啟用 canEnrage)
        if (!enraged && canEnrage && hp <= maxHp / 2 && hp > 0) {
            enraged = true;
            speedX = (int)(speedX * 1.5);
        }
        
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
        return dead;
    }

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isDead() { return dead; }
    public boolean isEnraged() { return enraged; }

    @Override
    public boolean outOfBounds() {
        return false;
    }
}
