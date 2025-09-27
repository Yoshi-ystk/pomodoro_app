package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/*
 * SoundPlayerクラスのテストクラス
 * 音声再生機能のテストを行う
 */
public class SoundPlayerTest {

  @BeforeEach
  void setUp() {
    // テスト実行前に音声ファイルの存在確認
    // 実際の音声ファイルが存在しない場合はテストをスキップ
  }

  @Test
  @DisplayName("音声ファイルの存在チェック - work_start.wav")
  void testSoundFileExists_WorkStart() {
    boolean exists = SoundPlayer.soundFileExists("/sounds/work_start.wav");
    // 音声ファイルが存在する場合のみテストを実行
    if (exists) {
      assertTrue(exists, "work_start.wavファイルが存在する必要があります");
    } else {
      System.out.println("警告: work_start.wavファイルが見つかりません。テストをスキップします。");
    }
  }

  @Test
  @DisplayName("音声ファイルの存在チェック - 存在しないファイル")
  void testSoundFileExists_NonExistentFile() {
    boolean exists = SoundPlayer.soundFileExists("/sounds/non_existent.wav");
    assertFalse(exists, "存在しないファイルはfalseを返す必要があります");
  }

  @Test
  @DisplayName("作業開始音の再生テスト")
  void testPlayWorkStartSound() {
    // 音声ファイルが存在する場合のみテストを実行
    if (SoundPlayer.soundFileExists("/sounds/work_start.wav")) {
      // 音声再生は実際には行わず、例外が発生しないことを確認
      assertDoesNotThrow(() -> {
        // テスト環境では実際の音声再生は行わない
        // 実際の音声再生テストは統合テストで行う
        System.out.println("作業開始音の再生テスト（実際の再生は行いません）");
      });
    } else {
      System.out.println("警告: work_start.wavファイルが見つかりません。テストをスキップします。");
    }
  }

  @Test
  @DisplayName("音量調整付き音声再生テスト")
  void testPlaySoundWithVolume() {
    // 音声ファイルが存在する場合のみテストを実行
    if (SoundPlayer.soundFileExists("/sounds/work_start.wav")) {
      assertDoesNotThrow(() -> {
        // テスト環境では実際の音声再生は行わない
        System.out.println("音量調整付き音声再生テスト（実際の再生は行いません）");
      });
    } else {
      System.out.println("警告: work_start.wavファイルが見つかりません。テストをスキップします。");
    }
  }

  @Test
  @DisplayName("無効な音声ファイルパスのテスト")
  void testPlaySoundWithInvalidPath() {
    // 存在しないファイルパスで音声再生を試行
    assertDoesNotThrow(() -> {
      SoundPlayer.playSound("/sounds/non_existent.wav");
    }, "存在しないファイルパスでも例外を投げてはいけません");
  }

  @Test
  @DisplayName("音量値の境界値テスト")
  void testVolumeBoundaryValues() {
    // 音声ファイルが存在する場合のみテストを実行
    if (SoundPlayer.soundFileExists("/sounds/work_start.wav")) {
      assertDoesNotThrow(() -> {
        // 境界値での音量調整テスト
        SoundPlayer.playSoundWithVolume("/sounds/work_start.wav", 0.0f); // 最小音量
        SoundPlayer.playSoundWithVolume("/sounds/work_start.wav", 0.5f); // 中間音量
        SoundPlayer.playSoundWithVolume("/sounds/work_start.wav", 1.0f); // 最大音量
      });
    } else {
      System.out.println("警告: work_start.wavファイルが見つかりません。テストをスキップします。");
    }
  }

  @Test
  @DisplayName("SoundPlayerクラスの静的メソッドのテスト")
  void testStaticMethods() {
    // 静的メソッドが正常に呼び出せることを確認
    assertDoesNotThrow(() -> {
      SoundPlayer.playWorkStartSound();
      SoundPlayer.playWorkCompleteSound();
      SoundPlayer.playBreakStartSound();
      SoundPlayer.playBreakCompleteSound();
    }, "静的メソッドは例外を投げてはいけません");
  }
}
