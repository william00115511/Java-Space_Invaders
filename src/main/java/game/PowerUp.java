package game;

/**
 * 掉落道具類別。
 * 繼承自 Flyings，被小兵摧毀時有一定機率掉落，給玩家提供火力升級。
 */
public class PowerUp extends Flyings {
    
    private int speed;
    private int type;
    
    public static final int TYPE_WEAPON = 0;
    public static final int TYPE_HEAL = 1;
    public static final int TYPE_SHIELD = 2;

    public PowerUp(int x, int y) {
        super(x, y, 20, 20); // 假設道具為 20x20 大小
        this.speed = 3; // 修復：不使用 ENEMY_DROP_SPEED(30)，改為緩慢掉落 3
        
        // 增高 Type 0 (Weapon) 掉落機率 (60% Weapon, 20% Heal, 20% Shield)
        double r = Math.random();
        if (r < 0.6) {
            this.type = TYPE_WEAPON;
        } else if (r < 0.8) {
            this.type = TYPE_HEAL;
        } else {
            this.type = TYPE_SHIELD;
        }
        
        if (this.type == TYPE_WEAPON) {
            this.setImage(ImageManager.powerupWeaponImg);
        } else if (this.type == TYPE_HEAL) {
            this.setImage(ImageManager.powerupHealImg);
        } else if (this.type == TYPE_SHIELD) {
            this.setImage(ImageManager.powerupShieldImg);
        }
    }
    
    public int getType() {
        return type;
    }

    @Override
    public void step() {
        setY(getY() + speed);
    }

    @Override
    public boolean outOfBounds() {
        return getY() > GameConfig.SCREEN_HEIGHT;
    }
}
