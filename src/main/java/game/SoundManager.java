package game;

import java.io.File;
import org.magiclen.magicaudioplayer.AudioPlayer;

/**
 * 音效驅動管理器 (Utility)。
 * 封裝教授指定的 org.magiclen.magicaudioplayer.AudioPlayer。
 * 統一負責背景音樂 (BGM) 與音效 (SFX) 的載入與播放，並具備防呆機制。
 */
public class SoundManager {

    private static AudioPlayer bgmPlayer;
    private static AudioPlayer shootPlayer;
    private static AudioPlayer explosionPlayer;

    // 用來控制 BGM 是否應該持續循環播放 (volatile 確保跨執行緒可見性)
    private static volatile boolean bgmLooping = false;

    private static final String BGM_PATH = "src/main/resources/audio/bgm.wav";

    static {
        // SFX 依然可以在載入時初始化，因為它們很短且通常即刻觸發無異常
        shootPlayer = createPlayer("src/main/resources/audio/shoot.wav");
        explosionPlayer = createPlayer("src/main/resources/audio/explosion.wav");
    }

    /**
     * 安全地建立 AudioPlayer。
     */
    private static AudioPlayer createPlayer(String path) {
        try {
            // 嘗試 ClassPath 模式 (針對最終打包的 JAR 或設定齊全的 IDE)
            java.net.URL url = SoundManager.class.getResource("/" + path.replace("src/main/resources/", ""));
            if (url != null) {
                return AudioPlayer.createPlayer(url);
            }
            
            // 回退到實體路徑模式 (允許一般的 IDE 直接開啟遊戲遊玩)
            File audioFile = new File(path);
            if (!audioFile.exists()) {
                System.err.println("[SoundManager 警告] 找不到音效檔案: " + audioFile.getAbsolutePath());
                return null;
            }
            return AudioPlayer.createPlayer(audioFile);
        } catch (Exception e) {
            System.err.println("[SoundManager 錯誤] 無法載入音效檔案: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 初始化 BGM 播放器 (延遲載入，確保第一次播放時有最新狀態)
     */
    private static void initBGMPlayer() {
        if (bgmPlayer == null) {
            bgmPlayer = createPlayer(BGM_PATH);
            if (bgmPlayer != null) {
                bgmPlayer.halfPower();
                bgmPlayer.setStatusChangedListener(
                        (AudioPlayer.Status oldStatus, AudioPlayer.Status newStatus) -> {
                            if (newStatus == AudioPlayer.Status.STOP && bgmLooping) {
                                bgmPlayer.playOver();
                            }
                        });
                // MagicAudioPlayer 的設計特性：
                // 剛建立時的狀態是 null。第一次呼叫 play() 只會把音訊管線打開 (狀態變成 OPEN)
                // 必須要有這一次的 "暖機"，後續的播放指令才會真正發出聲音
                bgmPlayer.play();
            }
        }
    }

    /**
     * 播放背景音樂 (BGM)
     */
    public static void playBGM() {
        initBGMPlayer(); // 確保已建立並暖機 (null -> OPEN)
        if (bgmPlayer != null) {
            bgmLooping = true;
            try {
                // 非常關鍵：使用獨立執行緒給予 50 毫秒的系統管線準備時間
                // 然後強制使用 playOver() 來真正啟動音訊 (OPEN -> START)
                new Thread(() -> {
                    try {
                        Thread.sleep(50);
                        bgmPlayer.playOver();
                    } catch (Exception e) {
                    }
                }).start();
            } catch (Exception e) {
                System.err.println("[SoundManager] BGM 播放異常");
            }
        }
    }

    /**
     * 暫停背景音樂 (暫時停止循環)。
     */
    public static void pauseBGM() {
        if (bgmPlayer != null) {
            try {
                bgmLooping = false;
                bgmPlayer.pause();
            } catch (Exception e) {
                System.err.println("[SoundManager] BGM 暫停失敗");
            }
        }
    }

    /**
     * 停止背景音樂並重置循環狀態。
     */
    public static void stopBGM() {
        if (bgmPlayer != null) {
            try {
                bgmLooping = false;
                bgmPlayer.stop();
            } catch (Exception e) {
                System.err.println("[SoundManager] BGM 停止失敗");
            }
        }
    }

    /**
     * 播放射擊音效 (短音效)。
     */
    public static void playShootSound() {
        if (shootPlayer != null) {
            try {
                shootPlayer.playOver();
            } catch (Exception e) {
                System.err.println("[SoundManager] 射擊音效播放失敗");
            }
        }
    }

    /**
     * 播放爆炸音效 (短音效)。
     */
    public static void playExplosionSound() {
        if (explosionPlayer != null) {
            try {
                explosionPlayer.playOver();
            } catch (Exception e) {
                System.err.println("[SoundManager] 爆炸音效播放失敗");
            }
        }
    }
}
