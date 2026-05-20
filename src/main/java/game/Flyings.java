package game;

import java.awt.Image;
import java.awt.Rectangle;

/**
 * 所有遊戲飛行物體的主抽象類別 (對應基礎 Entity)。
 * 包含了所有飛行物的共通屬性：座標 (x, y)、大小 (width, height) 與圖片。
 * 符合要求：所有屬性皆為 private，並透過 getter/setter 存取。
 */
public abstract class Flyings {
    
    private int x;
    private int y;
    private int width;
    private int height;
    private Image image; // 如果之後要繪製圖片可用這個屬性，目前可以為 null 單純繪製圖形
    
    public Flyings(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 抽象方法：定義每個飛行物的移動邏輯。
     * 讓子類別 (Player, Enemy, Bullet) 自行實作。
     */
    public abstract void step();
    
    /**
     * 取得該物件的邊界範圍，用於後續的碰撞偵測 (Collision Detection)。
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * 判斷是否超出畫面邊界，用於回收記憶體 (例如子彈飛出畫面外)。
     */
    public abstract boolean outOfBounds();

    // --- Getters & Setters ---

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
