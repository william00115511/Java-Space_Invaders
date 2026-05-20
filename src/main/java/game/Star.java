package game;

import java.awt.Color;

/**
 * 星星實體。
 * 用於背景 Parallax (視差) 動態滾動效果。
 * 每顆星星有不同的尺寸、下落速度與亮度，營造宇宙深度感。
 */
public class Star {
    private int x;
    private int y;
    private int size;
    private int speed;
    private Color color;

    public Star(int x, int y, int size, int speed, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.color = color;
    }

    /**
     * 更新星星的 Y 軸座標
     */
    public void step() {
        y += speed;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
}
