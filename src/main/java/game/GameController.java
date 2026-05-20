package game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.swing.Timer;

/**
 * 遊戲控制器 (Controller)。
 * 負責：
 * 1. 接收與處理鑑盤輸入 (KeyListener)。
 * 2. 啟動 Game Loop (javax.swing.Timer)，定期更新 Model 座標狀態。
 * 3. 執行碰撞偵測 (Collision Detection)。
 * 4. 通知 View (GamePanel) 進行畫面重繪 (`repaint()`)。
 */
public class GameController extends KeyAdapter implements ActionListener {

    private GameModel model;
    private GamePanel view;
    private Timer gameLoopTimer;

    // 記錄按鍵狀態，實現平滑移動 (避免 OS 鍵盤延遲卡頓)
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    private int waveTransitionTick = 0; // 用於波次過場計時

    public GameController(GameModel model, GamePanel view) {
        this.model = model;
        this.view = view;

        // 算出每個 frame 的毫秒數，約等於 1000ms / 60 FPS = 16.6ms
        int delay = 1000 / GameConfig.FPS;
        this.gameLoopTimer = new Timer(delay, this);
        this.gameLoopTimer.start(); // 啟動主迴圈
    }

    /**
     * 遊戲主迴圈 (Game Loop)，由 Timer 自動定期觸發。
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // 如果遊戲正在進行中，才更新座標與執行邏輯
        if (model.getState() == GameModel.RUNNING) {
            updatePlayerMovement();
            updateEntities();
            checkCollisions();
            checkVictoryCondition();
            model.updateShake(); // 每幀遞減螢幕震動計時器
        } else if (model.getState() == GameModel.WAVE_TRANSITION
                || model.getState() == GameModel.START
                || model.getState() == GameModel.TITLE_SCREEN
                || model.getState() == GameModel.GAME_OVER
                || model.getState() == GameModel.CAMPAIGN_INTRO
                || model.getState() == GameModel.CAMPAIGN_OUTRO) {
            // 這些狀態下仍然要更新背景星星製造飛行感
            for (Star star : model.getStars()) {
                star.step();
                if (star.getY() > GameConfig.SCREEN_HEIGHT) {
                    star.setY(0);
                    star.setX((int) (Math.random() * GameConfig.SCREEN_WIDTH));
                    star.setSpeed((int) (Math.random() * 3) + 1);
                }
            }

            // 波次過場專用邏輯
            if (model.getState() == GameModel.WAVE_TRANSITION) {
                waveTransitionTick--;
                if (waveTransitionTick <= 0) {
                    model.startNextWave();
                    model.setState(GameModel.RUNNING);
                }
            }

            // 電影級過場 (Intro)
            if (model.getState() == GameModel.CAMPAIGN_INTRO) {
                int tick = model.getCinematicTick() - 1;
                if (tick <= 0) {
                    model.setState(GameModel.RUNNING);
                    SoundManager.playBGM();
                } else {
                    model.setCinematicTick(tick);
                }
            }

            // 電影級過場 (Outro)
            if (model.getState() == GameModel.CAMPAIGN_OUTRO) {
                int tick = model.getCinematicTick() - 1;
                if (tick <= 0) {
                    model.setState(GameModel.VICTORY);
                    SoundManager.stopBGM();
                } else {
                    model.setCinematicTick(tick);
                }
                model.updateShake(); // Outro 允許震動延續
            }
        }

        // 無論遊戲狀態為何，每一幀都要求 View 重新繪製最新畫面
        view.repaint();
    }

    /**
     * 更新玩家位移與射擊邏輯 (基於狀態變數而非直接的事件觸發)。
     */
    private void updatePlayerMovement() {
        Player player = model.getPlayer();
        if (leftPressed) {
            player.moveLeft();
        }
        if (rightPressed) {
            player.moveRight();
        }

        // 如果玩家按著空白鍵，嘗試發射子彈 (冷卻邏輯已移交至 Player.shoot() 中控管)
        if (spacePressed) {
            java.util.List<Bullet> newBullets = player.shoot();
            if (!newBullets.isEmpty()) {
                model.getBullets().addAll(newBullets);
                SoundManager.playShootSound(); // 觸發射擊音效
            }
        }
    }

    /**
     * 更新所有實體(子彈、敵人、星星) 的座標狀態與邊界檢查。
     */
    private void updateEntities() {
        // -1. 更新背景視差星星 (Stars)
        for (Star star : model.getStars()) {
            star.step();
            if (star.getY() > GameConfig.SCREEN_HEIGHT) {
                star.setY(0); // 循環回到頂部
                star.setX((int) (Math.random() * GameConfig.SCREEN_WIDTH)); // 重新產生不可預測的 X 軸
                star.setSpeed((int) (Math.random() * 3) + 1); // 隨機新落速
            }
        }

        // 1. 更新子彈位置，並移除飛出畫面的子彈
        Iterator<Bullet> bulletIt = model.getBullets().iterator();
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            b.step();
            if (b.getY() < 0) { // 子彈飛出上方邊界
                bulletIt.remove();
            }
        }

        // 1.5 更新敵軍子彈位置
        Iterator<EnemyBullet> enemyBulletIt = model.getEnemyBullets().iterator();
        while (enemyBulletIt.hasNext()) {
            EnemyBullet eb = enemyBulletIt.next();
            eb.step(); // 敵軍子彈向下移動
            if (eb.outOfBounds()) { // 飛出下方邊界
                enemyBulletIt.remove();
            }
        }

        // 2. 更新敵人位置，並檢查是否有敵人觸碰畫面左右邊界
        boolean edgeHit = false;

        // --- 動態難度 (Dynamic Difficulty) 計算 ---
        int totalEnemies = 32; // 4列 * 8行
        int currentEnemies = model.getEnemies().size();

        // 速度隨著數量減少而線性增加 (初始: GameConfig.ENEMY_SPEED_X, 最高上限: 5)
        int maxSpeed = 5;
        int dynamicSpeed = GameConfig.ENEMY_SPEED_X;
        // 火力隨著數量減少而線性增加 (初始: 0.001, 最高上限: 0.005)
        double baseFireRate = 0.001;
        double maxFireRate = 0.005;
        double dynamicFireRate = baseFireRate;

        if (currentEnemies > 0) {
            double missingRatio = (totalEnemies - currentEnemies) / (double) totalEnemies;
            dynamicSpeed = GameConfig.ENEMY_SPEED_X + (int) (missingRatio * (maxSpeed - GameConfig.ENEMY_SPEED_X));
            dynamicFireRate = baseFireRate + (missingRatio * (maxFireRate - baseFireRate));
        }

        // 新增：尋找最前線敵人的邏輯。使用 HashMap 將同行的敵人 (X 座標相同) 群組化，只保留最底部的
        java.util.Map<Integer, Enemy> frontlineEnemies = new java.util.HashMap<>();

        for (Enemy e : model.getEnemies()) {
            e.setSpeedX(dynamicSpeed); // 套用動態難度速度
            e.step();
            // 由於整排敵人同步移動，只要有一隻碰壁，所有敵人都要反向並下降
            if (e.getX() < 0 || e.getX() + e.getWidth() > GameConfig.SCREEN_WIDTH) {
                edgeHit = true;
            }
            // 檢查是否 GameOver (敵人觸底)
            if (e.outOfBounds() || e.getY() + e.getHeight() >= GameConfig.SCREEN_HEIGHT) {
                model.setState(GameModel.GAME_OVER);
                SoundManager.stopBGM(); // 遊戲結束停止 BGM
                return; // 直接返回，避免後續修改引發 ConcurrentModificationException
            }

            // 將自己跟目前紀錄的同行最下面敵人比對，如果更外面(Y更大)，就取代它
            Enemy currentBottom = frontlineEnemies.get(e.getX());
            if (currentBottom == null || e.getY() > currentBottom.getY()) {
                frontlineEnemies.put(e.getX(), e);
            }
        }

        // 敵軍亂數發射子彈 (前線普通兵 + 獨立狙擊手)
        Player playerRef = model.getPlayer();

        // A. 前線普通兵/坦克正常開火
        for (Enemy shooter : frontlineEnemies.values()) {
            if (shooter.getType() == Enemy.TYPE_SNIPER)
                continue; // 狙擊手獨立處理
            if (Math.random() < dynamicFireRate) {
                int bulletX = shooter.getX() + shooter.getWidth() / 2 - GameConfig.BULLET_WIDTH / 2;
                int bulletY = shooter.getY() + shooter.getHeight();
                model.addEnemyBullet(new EnemyBullet(bulletX, bulletY));
            }
        }

        // B. 所有狙擊手無視前線限制，獨立開火 + 瞄準射擊
        for (Enemy sniper : model.getEnemies()) {
            if (sniper.getType() != Enemy.TYPE_SNIPER)
                continue;
            if (Math.random() < 0.005) { // 固定 0.5% 開火機率
                int bulletX = sniper.getX() + sniper.getWidth() / 2 - GameConfig.BULLET_WIDTH / 2;
                int bulletY = sniper.getY() + sniper.getHeight();
                // 瞄準玩家：計算 X 軸方向速度
                int aimSpeedX = 0;
                if (playerRef != null) {
                    int playerCenterX = playerRef.getX() + playerRef.getWidth() / 2;
                    int sniperCenterX = sniper.getX() + sniper.getWidth() / 2;
                    if (playerCenterX < sniperCenterX)
                        aimSpeedX = -2;
                    else if (playerCenterX > sniperCenterX)
                        aimSpeedX = 2;
                }
                model.addEnemyBullet(new EnemyBullet(bulletX, bulletY, aimSpeedX));
            }
        }

        // 如果碰壁，所有敵人統一向下移動並反轉方向
        if (edgeHit) {
            for (Enemy e : model.getEnemies()) {
                e.dropAndReverse();
            }
        }

        // --- 2.5 更新 Boss 位置與發射邏輯 ---
        Boss boss = model.getBoss();
        if (boss != null && !boss.isDead()) {
            boss.step();

            if (!boss.isEnraged()) {
                // 普通模式：2% 機率發射 3 方向散彈
                if (Math.random() < 0.02) {
                    int bx = boss.getX() + boss.getWidth() / 2 - GameConfig.BULLET_WIDTH / 2;
                    int by = boss.getY() + boss.getHeight();
                    model.addEnemyBullet(new EnemyBullet(bx, by, 0)); // 垂直向下
                    model.addEnemyBullet(new EnemyBullet(bx, by, -3)); // 往左下
                    model.addEnemyBullet(new EnemyBullet(bx, by, 3)); // 往右下
                }
            } else {
                // 狂暴模式：4% 機率發射 5 方向大扇形 + 追蹤彈
                if (Math.random() < 0.04) {
                    int bx = boss.getX() + boss.getWidth() / 2 - GameConfig.BULLET_WIDTH / 2;
                    int by = boss.getY() + boss.getHeight();
                    // 5 方向大扇形散彈
                    model.addEnemyBullet(new EnemyBullet(bx, by, 0));
                    model.addEnemyBullet(new EnemyBullet(bx, by, -2));
                    model.addEnemyBullet(new EnemyBullet(bx, by, 2));
                    model.addEnemyBullet(new EnemyBullet(bx, by, -4));
                    model.addEnemyBullet(new EnemyBullet(bx, by, 4));

                    // 精準追蹤彈：直逼玩家 X 座標
                    Player p = model.getPlayer();
                    if (p != null) {
                        int playerCX = p.getX() + p.getWidth() / 2;
                        int bossCX = boss.getX() + boss.getWidth() / 2;
                        int aimX = 0;
                        if (playerCX < bossCX)
                            aimX = -2;
                        else if (playerCX > bossCX)
                            aimX = 2;
                        model.addEnemyBullet(new EnemyBullet(bx, by, aimX));
                    }
                }
            }
        }

        // 2.7 更新道具位置
        Iterator<PowerUp> powerUpIt = model.getPowerUps().iterator();
        while (powerUpIt.hasNext()) {
            PowerUp pu = powerUpIt.next();
            pu.step();
            if (pu.outOfBounds()) {
                powerUpIt.remove();
            }
        }

        // 3. 更新爆炸特效生命週期
        Iterator<Explosion> expIt = model.getExplosions().iterator();
        while (expIt.hasNext()) {
            Explosion exp = expIt.next();
            exp.update();
            if (exp.isFinished()) {
                expIt.remove();
            }
        }
    }

    /**
     * 處理碰撞偵測與邊界侵略 (Game Over 條件)。
     */
    private void checkCollisions() {
        Player player = model.getPlayer();
        if (player != null) {
            player.updateInvincibility(); // 每幀推進無敵時間計時器
            player.updatePowerUp(); // 每幀推進強化倒數計時器
        }

        // a.0 檢查玩家是否吃到道具
        Iterator<PowerUp> powerUpIt = model.getPowerUps().iterator();
        while (powerUpIt.hasNext()) {
            PowerUp pu = powerUpIt.next();
            if (pu.getBounds().intersects(player.getBounds())) {
                powerUpIt.remove(); // 食用後消失

                // 根據道具類型給予不同強化 (修改為 switch 確保安全區分)
                switch (pu.getType()) {
                    case PowerUp.TYPE_WEAPON:
                        player.upgradeWeapon();
                        break;
                    case PowerUp.TYPE_HEAL:
                        player.heal();
                        break;
                    case PowerUp.TYPE_SHIELD:
                        player.addShield();
                        break;
                }

                SoundManager.playShootSound();// 以射擊音效暫時代替拾取音效
            }
        }

        // a. 獨立檢查敵軍子彈是否擊中玩家
        Iterator<EnemyBullet> enemyBulletIt = model.getEnemyBullets().iterator();
        while (enemyBulletIt.hasNext()) {
            EnemyBullet eb = enemyBulletIt.next();
            if (eb.getBounds().intersects(player.getBounds())) {
                enemyBulletIt.remove(); // 子彈消失
                if (!player.isInvincible()) {
                    player.loseLife();
                    SoundManager.playExplosionSound(); // 被擊中音效
                    model.triggerShake(20, 5); // 受擊螢幕震動

                    if (player.getLives() <= 0) {
                        checkAndSaveHighScore();
                        model.setState(GameModel.GAME_OVER);
                        SoundManager.stopBGM();
                        return; // 遊戲結束
                    }
                }
            }
        }

        // 使用 Iterator 安全地在迴圈中移除元素
        Iterator<Enemy> enemyIt = model.getEnemies().iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();

            // a. 檢查敵人是否撞到玩家實體
            if (enemy.getBounds().intersects(player.getBounds())) {
                if (!player.isInvincible()) {
                    player.loseLife();
                    SoundManager.playExplosionSound();
                    model.triggerShake(20, 5); // 受擊螢幕震動
                    if (player.getLives() <= 0) {
                        checkAndSaveHighScore();
                        model.setState(GameModel.GAME_OVER);
                        SoundManager.stopBGM();
                        return; // 遊戲結束
                    }
                }
            }

            // b. 檢查入侵線 (Invasion Line)：只要怪物的底部超過了玩家的頂部，視同防線被突破 (Game Over)
            if (enemy.getY() + enemy.getHeight() >= player.getY()) {
                checkAndSaveHighScore();
                model.setState(GameModel.GAME_OVER);
                SoundManager.stopBGM();
                return; // 遊戲結束
            }

            // b. 檢查每一發子彈是否打中目前的敵人
            Iterator<Bullet> bulletIt = model.getBullets().iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    bulletIt.remove(); // 子彈消耗

                    boolean killed = enemy.takeDamage(); // 扣血

                    if (killed) {
                        // 敵人死亡：產生爆炸、掉寶、加分
                        int expSize = 50;
                        int expX = enemy.getX() + (enemy.getWidth() - expSize) / 2;
                        int expY = enemy.getY() + (enemy.getHeight() - expSize) / 2;
                        model.addExplosion(new Explosion(expX, expY, expSize, expSize));

                        if (Math.random() < 0.20) {
                            model.getPowerUps()
                                    .add(new PowerUp(enemy.getX() + enemy.getWidth() / 2 - 10, enemy.getY()));
                        }

                        enemyIt.remove();
                        SoundManager.playExplosionSound();
                        model.setScore(model.getScore() + enemy.getScoreValue());
                    } else {
                        // 敵人僗尾未死 (坦克)：播放音效但不移除
                        SoundManager.playShootSound();
                    }
                    break;
                }
            }
        }

        // c. 檢查玩家子彈是否擊中 Boss
        Boss boss = model.getBoss();
        if (boss != null && !boss.isDead()) {
            Iterator<Bullet> bulletIt = model.getBullets().iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (bullet.getBounds().intersects(boss.getBounds())) {
                    bulletIt.remove(); // 移除子彈
                    SoundManager.playExplosionSound(); // 擊中音效
                    boss.takeDamage(1);
                    // 若 Boss 死掉，產生巨大的連環爆炸
                    if (boss.isDead()) {
                        model.setScore(model.getScore() + 1000);
                        model.triggerShake(30, 8); // Boss 死亡巨大震動
                        model.addExplosion(new Explosion(boss.getX(), boss.getY(), 80, 80));
                        model.addExplosion(new Explosion(boss.getX() + 70, boss.getY(), 80, 80));
                        model.addExplosion(new Explosion(boss.getX() + 35, boss.getY() + 30, 80, 80));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 確認是否所有敵人都已被消滅。
     */
    private void checkVictoryCondition() {
        if (model.getEnemies().isEmpty()) {
            Boss boss = model.getBoss();
            if (boss == null) {
                // 如果小兵清空且沒有 Boss，則產生 Boss (作為關卡最終考驗)
                // 只有第 3 波 (或之後) 的 Boss 允許狂暴 (Phase 2)
                boolean canEnrage = (model.getCurrentWave() >= 3);
                model.setBoss(new Boss((GameConfig.SCREEN_WIDTH - 150) / 2, 50, canEnrage));
            } else if (boss.isDead()) {
                // Boss 死亡後的勝利分流邏輯
                if (model.getGameMode() == 0 && model.getCurrentWave() >= 3) {
                    // 戰役模式通關 -> 進入 Outro 過場 (約 4 秒)
                    model.setState(GameModel.CAMPAIGN_OUTRO);
                    model.setCinematicTick(240);
                } else {
                    // 無盡模式，或是戰役模式尚未達到 3 波 -> 波次推進
                    model.setState(GameModel.WAVE_TRANSITION);
                    waveTransitionTick = 180; // 約停留 3 秒
                    // 不停止 BGM，讓戰鬥音樂連續播放延續緊張氣氛
                }
            }
        }
    }

    // --- 鍵盤事件處理 --- //

    /**
     * 比較當前分數與最高分，如果刷新紀錄則寫入檔案。
     */
    private void checkAndSaveHighScore() {
        if (model.getScore() > model.getHighScore()) {
            model.setHighScore(model.getScore());
            ScoreManager.saveHighScore(model.getScore());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int state = model.getState();

        // ================= 各種狀態下的輸入處理 ================= //

        // 第一層選單：在標題畫面按 Space/Enter 進入模式選擇選單
        if (state == GameModel.TITLE_SCREEN) {
            if (key == KeyEvent.VK_SPACE) {
                model.setState(GameModel.START);
            }
            return;
        }

        // 第二層選單：處理模式選擇控制
        if (state == GameModel.START) {
            int currentMenu = model.getMenuIndex();
            if (key == KeyEvent.VK_UP) {
                currentMenu--;
                if (currentMenu < 0)
                    currentMenu = 2;
                model.setMenuIndex(currentMenu);
            } else if (key == KeyEvent.VK_DOWN) {
                currentMenu++;
                if (currentMenu > 2)
                    currentMenu = 0;
                model.setMenuIndex(currentMenu);
            } else if (key == KeyEvent.VK_SPACE) {
                if (currentMenu == 2) {
                    System.exit(0);
                } else if (currentMenu == 0) {
                    // 進入戰役模式 -> 電影式開場 (約 3 秒)
                    model.setGameMode(0);
                    model.setState(GameModel.CAMPAIGN_INTRO);
                    model.setCinematicTick(180);
                } else {
                    // 進入無盡模式 -> 直接開始
                    model.setGameMode(1);
                    model.setState(GameModel.RUNNING);
                    SoundManager.playBGM();
                }
            }
            return;
        }

        // 在遊戲結束或勝利畫面：按 R 回到最一開始的標題畫面
        if ((state == GameModel.GAME_OVER || state == GameModel.VICTORY) && key == KeyEvent.VK_R) {
            model.initGame(); // 重設所有參數與狀態
            model.setState(GameModel.TITLE_SCREEN); // 退回致敬閃屏畫面
            SoundManager.stopBGM();
            return;
        }

        // 現代化 ESC 暫停選單
        if (key == KeyEvent.VK_ESCAPE) {
            if (state == GameModel.RUNNING) {
                model.setState(GameModel.PAUSE);
                model.setPauseIndex(0);
                SoundManager.pauseBGM();
            } else if (state == GameModel.PAUSE) {
                model.setState(GameModel.RUNNING);
                SoundManager.playBGM();
            }
            return;
        }

        // 暫停選單控制
        if (state == GameModel.PAUSE) {
            int pIndex = model.getPauseIndex();
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                pIndex = (pIndex == 0) ? 1 : 0;
                model.setPauseIndex(pIndex);
            } else if (key == KeyEvent.VK_SPACE) {
                if (pIndex == 0) {
                    // Resume
                    model.setState(GameModel.RUNNING);
                    SoundManager.playBGM();
                } else {
                    // Return to Menu
                    model.initGame();
                    model.setState(GameModel.TITLE_SCREEN);
                    SoundManager.stopBGM();
                }
            }
            return;
        }

        // 遊戲運行中的控制：移動與射擊 (僅設定狀態標記)
        if (state == GameModel.RUNNING) {
            if (key == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (key == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (key == KeyEvent.VK_SPACE) {
                spacePressed = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        // 放開按鍵時停止持續移動或射擊
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        if (key == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
    }
}
