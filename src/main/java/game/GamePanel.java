package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * 遊戲畫面面板 (View)。
 * 僅負責讀取 GameModel 的資料並繪製在畫面上，絕對不包含任何座標更新或碰撞邏輯。
 * 遵循 MVC 架構中 View 的被動角色。
 */
public class GamePanel extends JPanel {

    private GameModel model;

    // 透過 FontManager 取得預先載入的復古字體 (提供系統預設退路)
    private Font defaultFont = FontManager.getSmallFont();
    private Font titleFont = FontManager.getLargeFont();

    public GamePanel(GameModel model) {
        this.model = model;
        // 設定背景顏色為黑色，模擬太空背景
        this.setBackground(Color.BLACK);
    }

    /**
     * 重寫 JPanel 的 paintComponent 方法，這是 Swing 繪圖的核心。
     * 所有畫面繪製都在此方法內完成。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 必須呼叫，以清除前一次的畫面並填滿背景色

        // 螢幕震動系統
        Graphics2D g2d = (Graphics2D) g;
        int shakeOffsetX = 0, shakeOffsetY = 0;
        if (model.getShakeTick() > 0) {
            int intensity = model.getShakeIntensity();
            shakeOffsetX = (int) (Math.random() * intensity * 2) - intensity;
            shakeOffsetY = (int) (Math.random() * intensity * 2) - intensity;
            g2d.translate(shakeOffsetX, shakeOffsetY);
        }

        int state = model.getState();

        switch (state) {
            case GameModel.TITLE_SCREEN:
                drawTitleScreen(g);
                break;
            case GameModel.START:
                drawStartScreen(g);
                break;
            case GameModel.RUNNING:
                drawGameSpace(g);
                break;
            case GameModel.PAUSE:
                drawGameSpace(g); // 繪製背景與物件
                drawPauseScreen(g); // 現代化暫停選單
                break;
            case GameModel.GAME_OVER:
                drawGameSpace(g); // 繪製背景與物件
                drawGameOverScreen(g); // 專屬結算畫面
                break;
            case GameModel.VICTORY:
                drawGameSpace(g); // 繪製背景與物件
                drawVictoryScreen(g); // 專屬勝利畫面
                break;
            case GameModel.CAMPAIGN_INTRO:
                drawCampaignIntro(g);
                break;
            case GameModel.CAMPAIGN_OUTRO:
                drawGameSpace(g); // 保留最後戰鬥畫面作為底圖
                drawCampaignOutro(g);
                break;
            case GameModel.WAVE_TRANSITION:
                drawGameSpace(g);
                // 閃爍的黃色過場提示
                if ((System.currentTimeMillis() / 250) % 2 == 0) {
                    drawOverlayText(g, "WAVE " + (model.getCurrentWave() + 1), "GET READY!", Color.YELLOW);
                }
                break;
        }

        // 螢幕震動復原
        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            g2d.translate(-shakeOffsetX, -shakeOffsetY);
        }
    }

    /**
     * 繪製遊戲進行中的所有實體 (Player, Enemies, Bullets) 與分數。
     */
    /**
     * 繪製遊戲進行中的所有實體 (Player, Enemies, Bullets) 與分數。
     */
    private void drawGameSpace(Graphics g) {
        // 0. 繪製視差星空背景 (Stars) - 最底層
        if (model.getStars() != null) {
            for (Star star : model.getStars()) {
                g.setColor(star.getColor());
                g.fillOval(star.getX(), star.getY(), star.getSize(), star.getSize());
            }
        }

        // 1. 繪製分數與生命數 UI、波次 UI
        g.setColor(Color.WHITE);
        g.setFont(defaultFont);

        // 分數與最高分 (僅限無盡模式)
        if (model.getGameMode() == 1) {
            g.drawString("Score: " + model.getScore(), 20, 30);
        }

        // 左上角波次 (置於分數下方或原位)
        int waveY = (model.getGameMode() == 1) ? 55 : 30;
        g.setColor(Color.YELLOW);
        g.drawString("WAVE: " + model.getCurrentWave(), 20, waveY);

        Player player = model.getPlayer();
        if (player != null) {
            // 畫出生命值 UI：在右上角 (或左上角分數下方)，利用縮小的飛船圖片來代表生命數
            for (int i = 0; i < player.getLives(); i++) {
                int lifeX = GameConfig.SCREEN_WIDTH - 40 - (i * 35);
                int lifeY = 15;
                if (ImageManager.playerImg != null) {
                    g.drawImage(ImageManager.playerImg, lifeX, lifeY, 25, 25, null);
                } else {
                    g.setColor(Color.GREEN);
                    g.fillRect(lifeX, lifeY, 25, 25);
                }
            }

            // 2. 繪製玩家實體 (Player) 包含無敵閃爍特效 (I-frames)
            boolean shouldDraw = true;
            if (player.isInvincible()) {
                // 利用 modulo 取餘數製造一閃一閃的效果 (每 10 幀為一個週期，前 5 幀顯示，後 5 幀隱藏)
                if (player.getInvincibleTick() % 10 >= 5) {
                    shouldDraw = false;
                }
            }

            if (shouldDraw) {
                if (player.getImage() != null) {
                    g.drawImage(player.getImage(), player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                            null);
                } else {
                    g.setColor(Color.GREEN); // Fallback 色塊
                    g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
                }
            }
        }

        // 3. 繪製爆炸特效 (Explosions) - 確保在畫敵人之前，避免爆炸圖層遮蓋存活的敵人
        if (model.getExplosions() != null) {
            for (Explosion exp : model.getExplosions()) {
                java.awt.image.BufferedImage frame = ImageManager.getExplosionFrame(exp.getCurrentFrame());
                if (frame != null) {
                    g.drawImage(frame, exp.getX(), exp.getY(), exp.getWidth(), exp.getHeight(), null);
                } else {
                    g.setColor(Color.RED); // Fallback 橘紅色圓形
                    g.fillOval(exp.getX(), exp.getY(), exp.getWidth(), exp.getHeight());
                }
            }
        }

        // 4. 繪製敵人陣列 (Enemies) - 依兵種渲染不同圖片或顏色
        for (Enemy enemy : model.getEnemies()) {
            if (enemy.isAlive()) {
                if (enemy.getImage() != null) {
                    g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(),
                            null);
                } else {
                    // Fallback 色塊依兵種區分
                    switch (enemy.getType()) {
                        case Enemy.TYPE_TANK:
                            g.setColor(Color.GREEN); // 坦克：綠色
                            break;
                        case Enemy.TYPE_SNIPER:
                            g.setColor(Color.MAGENTA); // 狙擊手：洋紅色
                            break;
                        default:
                            g.setColor(Color.RED); // 普通：紅色
                            break;
                    }
                    g.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
                }
                // 受擊閃白特效：在敵人上方疊加白色半透明矩形
                if (enemy.getHitFlashTick() > 0) {
                    g.setColor(new Color(255, 255, 255, 180));
                    g.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
                }
            }
        }

        // 4.5 繪製 Boss 與血條
        Boss boss = model.getBoss();
        if (boss != null && !boss.isDead()) {
            if (boss.getImage() != null) {
                g.drawImage(boss.getImage(), boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight(), null);
            } else {
                g.setColor(new Color(138, 43, 226)); // Fallback 紫色色塊
                g.fillRect(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight());
            }

            // 繪製 Boss 血條 (置中於畫面上方)
            int barWidth = 400;
            int barHeight = 20;
            int barX = (GameConfig.SCREEN_WIDTH - barWidth) / 2;
            int barY = 50;
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barWidth, barHeight); // 外框

            int currentHpWidth = (int) (((double) boss.getHp() / boss.getMaxHp()) * barWidth);
            // 狂暴時血條在紫與紅之間閃爍
            if (boss.isEnraged() && (System.currentTimeMillis() / 200) % 2 == 0) {
                g.setColor(Color.MAGENTA);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(barX + 1, barY + 1, currentHpWidth - 1, barHeight - 1);
        }
        // 4.6 繪製掉落道具 (PowerUps)
        for (PowerUp pu : model.getPowerUps()) {
            if (pu.getImage() != null) {
                g.drawImage(pu.getImage(), pu.getX(), pu.getY(), pu.getWidth(), pu.getHeight(), null);
            } else {
                if (pu.getType() == PowerUp.TYPE_WEAPON) {
                    g.setColor(Color.CYAN);   // Weapon = Cyan
                } else if (pu.getType() == PowerUp.TYPE_HEAL) {
                    g.setColor(Color.GREEN);  // Heal = Green
                } else if (pu.getType() == PowerUp.TYPE_SHIELD) {
                    g.setColor(Color.YELLOW); // Shield = Yellow
                }
                g.fillRect(pu.getX(), pu.getY(), pu.getWidth(), pu.getHeight());
            }
        }

        // 5. 繪製子彈陣列 (Bullets)
        for (Bullet bullet : model.getBullets()) {
            if (bullet.getImage() != null) {
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight(),
                        null);
            } else {
                g.setColor(Color.YELLOW); // Fallback 色塊
                g.fillRect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
            }
        }

        // 6. 繪製敵軍子彈陣列 (Enemy Bullets)
        if (model.getEnemyBullets() != null) {
            for (EnemyBullet eb : model.getEnemyBullets()) {
                if (eb.getImage() != null) {
                    g.drawImage(eb.getImage(), eb.getX(), eb.getY(), eb.getWidth(), eb.getHeight(), null);
                } else {
                    g.setColor(Color.ORANGE); // Fallback 色塊
                    g.fillRect(eb.getX(), eb.getY(), eb.getWidth(), eb.getHeight());
                }
            }
        }
    }

    /**
     * 第一層：標題閃屏畫面 (Title Screen)
     * 致敬經典《小蜜蜂 Galaga》的純展示畫面。
     */
    private void drawTitleScreen(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        int centerY = h / 2;

        // 0. 繪製流動星空背景
        if (model.getStars() != null) {
            for (Star star : model.getStars()) {
                g.setColor(star.getColor());
                g.fillOval(star.getX(), star.getY(), star.getSize(), star.getSize());
            }
        }

        // 1. 頂部 HI-SCORE 顯示
        g.setColor(Color.YELLOW);
        g.setFont(defaultFont);
        FontMetrics smallFm = g.getFontMetrics(defaultFont);
        String hiScoreText = "HI-SCORE: " + model.getHighScore();
        int hsWidth = smallFm.stringWidth(hiScoreText);
        g.drawString(hiScoreText, (w - hsWidth) / 2, centerY - 100);

        // 2. 遊戲大標題
        g.setColor(Color.CYAN);
        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        String title = GameConfig.GAME_TITLE;
        int titleWidth = titleFm.stringWidth(title);
        // 置中點往上偏移
        g.drawString(title, (w - titleWidth) / 2, centerY - 40);

        // 3. Sprite 展示區：在標題和提示之間展示飛船與外星人
        int spriteY = centerY + 10;

        // 畫出玩家飛船 (置中偏左)
        java.awt.Image playerImg = ImageManager.playerImg;
        if (playerImg != null) {
            g.drawImage(playerImg, w / 2 - 80, spriteY, 50, 30, null);
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(w / 2 - 80, spriteY, 50, 30);
        }

        // 畫出 "VS" 文字
        g.setColor(Color.WHITE);
        g.setFont(defaultFont);
        String vsText = "VS";
        int vsWidth = smallFm.stringWidth(vsText);
        g.drawString(vsText, (w - vsWidth) / 2, spriteY + 22);

        // 畫出敵人外星人 (置中偏右)
        java.awt.Image enemyImg = ImageManager.enemyImg;
        if (enemyImg != null) {
            g.drawImage(enemyImg, w / 2 + 30, spriteY, 40, 30, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(w / 2 + 30, spriteY, 40, 30);
        }

        // 4. 閃爍的開始提示
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            g.setColor(Color.GREEN);
            g.setFont(defaultFont);
            String startText = "PRESS SPACE TO START";
            int stWidth = smallFm.stringWidth(startText);
            g.drawString(startText, (w - stWidth) / 2, centerY + 100);
        }

        // 5. 底部操作說明
        g.setColor(new Color(150, 150, 150)); // 柔和灰色
        g.setFont(defaultFont);
        String controlsText = "ARROWS: MOVE | SPACE: SHOOT | ESC: EXIT";
        int ctWidth = smallFm.stringWidth(controlsText);
        g.drawString(controlsText, (w - ctWidth) / 2, h - 30);
    }

    /**
     * 第二層：模式選擇選單 (Mode Selection Screen)
     */
    private void drawStartScreen(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        // 0. 繪製流動星空背景
        if (model.getStars() != null) {
            for (Star star : model.getStars()) {
                g.setColor(star.getColor());
                g.fillOval(star.getX(), star.getY(), star.getSize(), star.getSize());
            }
        }

        // 1. 遊戲大標題 (保持在上方)
        g.setColor(Color.CYAN);
        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        String title = GameConfig.GAME_TITLE;
        int titleWidth = titleFm.stringWidth(title);
        int titleY = h / 2 - 100;
        g.drawString(title, (w - titleWidth) / 2, titleY);

        // 2. 互動式主選單
        g.setFont(defaultFont);
        FontMetrics smallFm = g.getFontMetrics(defaultFont);
        String[] menuOptions = {
                "CAMPAIGN MODE (3 WAVES)",
                "ENDLESS MODE",
                "EXIT GAME"
        };

        int menuStartY = titleY + 80;
        int menuSpacing = 40;
        int currentIndex = model.getMenuIndex();

        for (int i = 0; i < menuOptions.length; i++) {
            String text = menuOptions[i];

            // 被選中的項目用醒目顏色並加上游標符號
            if (i == currentIndex) {
                g.setColor(Color.GREEN);
                // 讓游標閃爍
                if ((System.currentTimeMillis() / 250) % 2 == 0) {
                    text = "> " + text;
                } else {
                    text = "  " + text; // 保持寬度一致
                }
            } else {
                g.setColor(Color.WHITE);
                text = "  " + text;
            }

            int stringWidth = smallFm.stringWidth(text);
            g.drawString(text, (w - stringWidth) / 2, menuStartY + (i * menuSpacing));
        }

        // 3. 底部選單說明
        g.setColor(new Color(150, 150, 150));
        g.setFont(defaultFont);
        String line1 = "USE UP/DOWN TO SELECT";
        String line2 = "SPACE TO CONFIRM";
        int w1 = smallFm.stringWidth(line1);
        int w2 = smallFm.stringWidth(line2);
        g.drawString(line1, (w - w1) / 2, h - 60);
        g.drawString(line2, (w - w2) / 2, h - 30);
    }

    /**
     * 輔助方法：用於在畫面上方繪製置中堆疊文字 (如 Pause 或 Game Over)。
     * 支援主標題與副標題分行顯示，避免單行文字過長超出螢幕。
     */
    private void drawOverlayText(Graphics g, String title, String subTitle, Color color) {
        g.setColor(color);

        // 1. 繪製主標題
        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics(titleFont);
        int titleWidth = titleMetrics.stringWidth(title);
        int titleAscent = titleMetrics.getAscent();

        int titleX = (getWidth() - titleWidth) / 2;
        int titleY = (getHeight() / 2) - 50 + titleAscent;
        g.drawString(title, titleX, titleY);

        // 2. 繪製副標題
        g.setFont(defaultFont);
        FontMetrics subMetrics = g.getFontMetrics(defaultFont);
        int subTitleWidth = subMetrics.stringWidth(subTitle);
        int subTitleAscent = subMetrics.getAscent();

        int subTitleX = (getWidth() - subTitleWidth) / 2;
        int subTitleY = (getHeight() / 2) + 20 + subTitleAscent;
        g.drawString(subTitle, subTitleX, subTitleY);
    }

    /**
     * 繪製 Game Over 專屬結算畫面。
     * 顯示本次得分與歷史最高分，讓玩家清楚知道差距。
     */
    private void drawGameOverScreen(Graphics g) {
        // 半透明黑色過度層，讓文字更清晰
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 紅色大字 "GAME OVER"
        g.setColor(Color.RED);
        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        String gameOverText = "GAME OVER";
        int goWidth = titleFm.stringWidth(gameOverText);
        g.drawString(gameOverText, centerX - goWidth / 2, centerY - 80);

        g.setFont(defaultFont);
        FontMetrics subFm = g.getFontMetrics(defaultFont);

        // 分數相關資訊僅在無盡模式顯示
        if (model.getGameMode() == 1) {
            // 白色副標題 "YOUR SCORE:"
            g.setColor(Color.WHITE);
            String scoreLabel = "YOUR SCORE: " + model.getScore();
            int slWidth = subFm.stringWidth(scoreLabel);
            g.drawString(scoreLabel, centerX - slWidth / 2, centerY - 20);

            // 黃色 "HI-SCORE:"
            int currentScore = model.getScore();
            int hiScore = model.getHighScore();
            boolean isNewRecord = (currentScore >= hiScore && currentScore > 0);

            g.setColor(Color.YELLOW);
            String hiLabel = "HI-SCORE: " + hiScore;
            int hlWidth = subFm.stringWidth(hiLabel);
            g.drawString(hiLabel, centerX - hlWidth / 2, centerY + 20);

            // 如果刷新紀錄，顯示閃爍的 "NEW RECORD!" 提示
            if (isNewRecord) {
                g.setColor(Color.GREEN);
                if ((System.currentTimeMillis() / 300) % 2 == 0) {
                    String newRecordText = "*** NEW RECORD! ***";
                    int nrWidth = subFm.stringWidth(newRecordText);
                    g.drawString(newRecordText, centerX - nrWidth / 2, centerY + 55);
                }
            }
        }

        // 底部提示
        g.setColor(Color.CYAN);
        String restartHint = "Press R to Return to Menu";
        int rhWidth = subFm.stringWidth(restartHint);
        g.drawString(restartHint, centerX - rhWidth / 2, centerY + 100);
    }

    /**
     * 繪製 Victory 專屬勝利畫面。
     * 只有在戰役模式擊敗 Wave 3 Boss 後出現。
     */
    private void drawVictoryScreen(Graphics g) {
        // 半透明黑色過度層
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        int w = getWidth();
        int centerY = getHeight() / 2;

        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        g.setFont(defaultFont);
        FontMetrics fm = g.getFontMetrics(defaultFont);

        // 1. 繪製綠色主標題
        String title = "VICTORY!";
        g.setColor(Color.GREEN);
        g.setFont(titleFont);
        g.drawString(title, (w - titleFm.stringWidth(title)) / 2, centerY - 100);

        // 2. 結算副標題 (強制使用純 SmallFont 避免過長出界)
        String subTitle = "YOU SAVED THE EARTH!";
        Font smallFont = FontManager.getSmallFont();
        g.setFont(smallFont);
        FontMetrics smallFm = g.getFontMetrics(smallFont);
        g.drawString(subTitle, (w - smallFm.stringWidth(subTitle)) / 2, centerY - 40);

        // 3. 戰役模式專屬排版 (不顯示分數)
        if (model.getGameMode() == 0) {
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                g.setColor(Color.CYAN);
                String restartHint = "Press R to Return to Menu";
                g.drawString(restartHint, (w - fm.stringWidth(restartHint)) / 2, centerY + 80);
            }
        } else {
            // 無盡模式 (防禦性設計，若未來無盡也可觸發 Victory)
            String currentScoreStr = "FINAL SCORE: " + model.getScore();
            g.setColor(Color.WHITE);
            g.drawString(currentScoreStr, (w - fm.stringWidth(currentScoreStr)) / 2, centerY + 20);

            String highScoreStr = "HI-SCORE: " + model.getHighScore();
            g.setColor(Color.YELLOW);
            g.drawString(highScoreStr, (w - fm.stringWidth(highScoreStr)) / 2, centerY + 60);

            if (model.getScore() > 0 && model.getScore() == model.getHighScore()) {
                g.setColor(Color.CYAN);
                String newRecordStr = "*** NEW RECORD! ***";
                g.drawString(newRecordStr, (w - fm.stringWidth(newRecordStr)) / 2, centerY + 100);
            }
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                g.setColor(Color.CYAN);
                String restartHint = "Press R to Return to Menu";
                g.drawString(restartHint, (w - fm.stringWidth(restartHint)) / 2, centerY + 160);
            }
        }
    }

    /**
     * 戰役模式專屬開場動畫 (黑螢幕閃爍打字效果)
     */
    private void drawCampaignIntro(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        int tick = model.getCinematicTick();
        int w = getWidth();
        int centerY = getHeight() / 2;

        g.setFont(defaultFont);
        FontMetrics fm = g.getFontMetrics(defaultFont);

        // tick 從 180 遞減至 0 (共約 3 秒)
        // 使用閃爍效果營造系統啟動的感覺
        if ((tick / 20) % 2 == 0) {
            String msg = "MISSION: ERADICATE ALIEN THREAT";
            g.setColor(Color.GREEN);
            g.drawString(msg, (w - fm.stringWidth(msg)) / 2, centerY);
        }
    }

    /**
     * 戰役模式專屬結尾動畫 (漸層黑幕覆蓋淡出)
     */
    private void drawCampaignOutro(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        int centerY = h / 2;
        int tick = model.getCinematicTick(); // 從 240 遞減

        // 根據剩餘時間計算淡出透明度 (漸黑)
        // tick=240 -> alpha=0; tick=0 -> alpha=255
        int alpha = 255 - Math.min(255, Math.max(0, (tick * 255) / 240));
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, w, h);

        // 文字在黑幕達到一定程度後浮現閃爍
        if (alpha > 150) {
            g.setFont(defaultFont);
            FontMetrics fm = g.getFontMetrics(defaultFont);
            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                String msg = "TARGET DESTROYED...";
                g.setColor(Color.WHITE);
                g.drawString(msg, (w - fm.stringWidth(msg)) / 2, centerY);
            }
        }
    }

    /**
     * 現代化暫停選單 (Modern Pause Screen)
     * 替換了舊版單純的疊加文字，加入互動選項與遮罩。
     */
    private void drawPauseScreen(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        int centerY = h / 2;

        // 半透明黑色遮罩 (凸顯暫停狀態)
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, w, h);

        // 黃色大字 "PAUSED"
        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        String pauseText = "PAUSED";
        g.setColor(Color.YELLOW);
        g.drawString(pauseText, (w - titleFm.stringWidth(pauseText)) / 2, centerY - 60);

        // 暫停選單選項
        g.setFont(defaultFont);
        FontMetrics fm = g.getFontMetrics(defaultFont);
        String[] options = { "RESUME", "RETURN TO MENU" };
        int menuStartY = centerY;
        int spacing = 40;
        int pIndex = model.getPauseIndex();

        for (int i = 0; i < options.length; i++) {
            String text = options[i];

            if (i == pIndex) {
                g.setColor(Color.CYAN);
                // 游標閃爍
                if ((System.currentTimeMillis() / 250) % 2 == 0) {
                    text = "> " + text;
                } else {
                    text = "  " + text; // 保持寬距一致
                }
            } else {
                g.setColor(Color.WHITE);
                text = "  " + text;
            }

            g.drawString(text, (w - fm.stringWidth(text)) / 2, menuStartY + (i * spacing));
        }
    }
}
