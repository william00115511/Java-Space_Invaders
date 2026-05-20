package game;

/**
 * 爆炸特效模型。
 * 具有特定的生命週期 (以畫格數 currentFrame 計算)。
 * 不具備物理碰撞特性，純粹用於視圖表現。
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int currentFrame;

    public Explosion(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.currentFrame = 0;
    }

    /**
     * 每個 Frame 呼叫，推進動畫
     */
    public void update() {
        // 為了營造快節奏射擊遊戲的短促爆裂感，每個 update (約 16ms) 直接推進一格。
        // 原先的 tick % 3 會拖太長 (約 0.5秒)。現在修正為 9 幀全跑完只要 0.15 秒！
        currentFrame++;
    }

    /**
     * @return 判斷特效是否已經結束 (畫完)
     */
    public boolean isFinished() {
        return currentFrame >= 9;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCurrentFrame() { return currentFrame; }
}
