package com.yoshitaka.pomodoro;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;

/*
 * 音声ファイルの再生を管理するクラス
 * WAV形式の音声ファイルを再生する機能を提供
 *
 * このクラスは以下の機能を提供：
 * 1. resourcesディレクトリ内の音声ファイルの再生
 * 2. ファイルパスを指定した音声再生
 * 3. 再生エラーの処理
 */
public class SoundPlayer {

  /*
   * 指定されたファイルパスの音声を再生するメソッド
   * resourcesディレクトリ内のファイルを対象とする
   *
   * @param soundPath resourcesディレクトリからの相対パス（例: "/sounds/work_start.wav"）
   */
  public static void playSound(String soundPath) {
    try {
      System.out.println("[DEBUG] 音声ファイルを読み込み中: " + soundPath);
      // resourcesディレクトリから音声ファイルを取得
      InputStream audioInputStream = SoundPlayer.class.getResourceAsStream(soundPath);

      if (audioInputStream == null) {
        System.err.println("音声ファイルが見つかりません: " + soundPath);
        return;
      }
      System.out.println("[DEBUG] 音声ファイルの読み込み成功");

      // macOSではJavaのAudioSystemに問題があるようなので最初からフォールバックを使用
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("mac")) {
        System.out.println("[DEBUG] macOS検出: フォールバック機能を使用");
        playSoundWithSystemCall(soundPath);
        return;
      }

      // 一時ファイルを作成してAudioSystemの問題を回避
      File tempFile = File.createTempFile("sound_", ".wav");
      FileOutputStream fos = new FileOutputStream(tempFile);

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = audioInputStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
      }

      fos.close();
      audioInputStream.close();

      // 一時ファイルからAudioInputStreamを作成
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(tempFile);

      // Clipを作成して音声を開く
      Clip clip = AudioSystem.getClip();
      clip.open(audioStream);

      // 音量を調整（最大音量に設定）
      if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float maxVolume = volumeControl.getMaximum();
        volumeControl.setValue(maxVolume);
        System.out.println("[DEBUG] 音量を最大に設定: " + maxVolume + " dB");
      }

      // 音声を再生
      System.out.println("[DEBUG] 音声を再生開始");
      clip.start();

      // 再生完了まで待機（非同期で再生されるため）
      // 短い音声ファイルなので、再生完了を待つことで適切にリソースが解放される
      while (clip.isRunning()) {
        Thread.sleep(10);
      }
      System.out.println("[DEBUG] 音声再生終了");

      // リソースを解放
      clip.close();
      audioStream.close();
      tempFile.delete(); // 一時ファイルを削除

    } catch (UnsupportedAudioFileException e) {
      System.err.println("サポートされていない音声ファイル形式です: " + soundPath);
      System.err.println("WAV形式のファイルを使用してください。");
      // フォールバックとしてシステムコールを試行
      playSoundWithSystemCall(soundPath);
    } catch (LineUnavailableException e) {
      System.err.println("音声デバイスが利用できません: " + e.getMessage());
      // フォールバックとしてシステムコールを試行
      playSoundWithSystemCall(soundPath);
    } catch (IOException e) {
      System.err.println("音声ファイルの読み込みエラー: " + e.getMessage());
      // フォールバックとしてシステムコールを試行
      playSoundWithSystemCall(soundPath);
    } catch (InterruptedException e) {
      System.err.println("音声再生が中断されました");
      Thread.currentThread().interrupt();
    }
  }

  /*
   * システムコールを使用して音声を再生するフォールバックメソッド
   * JavaのAudioSystemで問題が発生した場合に使用
   */
  private static void playSoundWithSystemCall(String soundPath) {
    try {
      System.out.println("[DEBUG] システムコールで音声再生を試行中...");

      // 一時ファイル作成
      InputStream audioInputStream = SoundPlayer.class.getResourceAsStream(soundPath);
      if (audioInputStream == null) {
        System.err.println("フォールバック: 音声ファイルが見つかりません");
        return;
      }

      File tempFile = File.createTempFile("sound_", ".wav");
      FileOutputStream fos = new FileOutputStream(tempFile);

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = audioInputStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
      }

      fos.close();
      audioInputStream.close();

      // macOSのafplayコマンドを使用して音声を再生（音量を上げるオプション付き）
      ProcessBuilder pb = new ProcessBuilder("afplay", "-v", "2.0", tempFile.getAbsolutePath());
      Process process = pb.start();

      // プロセスの完了を待機
      int exitCode = process.waitFor();

      // 一時ファイルを削除
      tempFile.delete();

      if (exitCode == 0) {
        System.out.println("[DEBUG] システムコール音声再生成功");
      } else {
        System.err.println("[DEBUG] システムコール音声再生失敗: 終了コード " + exitCode);
      }

    } catch (Exception e) {
      System.err.println("フォールバック音声再生エラー: " + e.getMessage());
    }
  }

  /*
   * 作業開始時の通知音を再生するメソッド
   * work_start.wavファイルを再生
   */
  public static void playWorkStartSound() {
    System.out.println("[DEBUG] 作業開始音を再生中...");
    playSound("/sounds/work_start.wav");
    System.out.println("[DEBUG] 作業開始音の再生完了");
  }

  /*
   * 作業完了時の通知音を再生するメソッド
   * work_complete.wavファイルを再生
   */
  public static void playWorkCompleteSound() {
    playSound("/sounds/work_complete.wav");
  }

  /*
   * 休憩開始時の通知音を再生するメソッド
   * break_start.wavファイルを再生
   */
  public static void playBreakStartSound() {
    playSound("/sounds/break_start.wav");
  }

  /*
   * 休憩完了時の通知音を再生するメソッド
   * break_complete.wavファイルを再生
   */
  public static void playBreakCompleteSound() {
    playSound("/sounds/break_complete.wav");
  }

  /*
   * 音量を調整して音声を再生するメソッド
   *
   * @param soundPath 音声ファイルのパス
   *
   * @param volume 音量（0.0〜1.0、1.0が最大音量）
   */
  public static void playSoundWithVolume(String soundPath, float volume) {
    try {
      InputStream audioInputStream = SoundPlayer.class.getResourceAsStream(soundPath);

      if (audioInputStream == null) {
        System.err.println("音声ファイルが見つかりません: " + soundPath);
        return;
      }

      AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioInputStream);
      Clip clip = AudioSystem.getClip();
      clip.open(audioStream);

      // 音量を調整
      FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
      volumeControl.setValue(dB);

      clip.start();

      // 再生完了まで待機
      while (clip.isRunning()) {
        Thread.sleep(10);
      }

      clip.close();
      audioStream.close();
      audioInputStream.close();

    } catch (Exception e) {
      System.err.println("音量調整付き音声再生エラー: " + e.getMessage());
    }
  }

  /*
   * 音声ファイルが存在するかチェックするメソッド
   *
   * @param soundPath チェックする音声ファイルのパス
   *
   * @return ファイルが存在する場合はtrue、そうでなければfalse
   */
  public static boolean soundFileExists(String soundPath) {
    InputStream audioInputStream = SoundPlayer.class.getResourceAsStream(soundPath);
    if (audioInputStream != null) {
      try {
        audioInputStream.close();
      } catch (IOException e) {
        // クローズエラーは無視
      }
      return true;
    }
    return false;
  }
}
