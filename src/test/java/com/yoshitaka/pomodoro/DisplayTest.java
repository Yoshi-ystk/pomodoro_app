package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Displayクラスのテスト
 * 標準出力をキャプチャして検証
 */
class DisplayTest {

    // 標準出力をキャプチャするためのストリーム
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private Display display;

    @BeforeEach
    void setUp() {
        // テスト前に標準出力を自前のストリームに切り替える
        System.setOut(new PrintStream(outContent));
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        // テスト後に標準出力を元に戻す
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("メインメニューが正しく表示されること")
    void testShowMainMenu() {
        display.showMainMenu();
        String output = outContent.toString();
        assertTrue(output.contains("ポモドーロアプリを起動しました。"));
        assertTrue(output.contains("メニューを入力してください。"));
        assertTrue(output.contains("開始: start / 終了: end"));
    }

    @Test
    @DisplayName("タイマーの初期画面が正しく描画されること")
    void testDrawInitialTimerScreen() {
        display.drawInitialTimerScreen();
        String output = outContent.toString();
        // 画面クリアとカーソル非表示のコードが含まれているか
        assertTrue(output.contains("\033[H\033[2J"));
        assertTrue(output.contains("\033[?25l"));
        // プロンプトが表示されているか
        assertTrue(output.contains("> "));
    }

    @Test
    @DisplayName("タイマー実行中の画面が正しく更新されること")
    void testUpdateTimerScreen_Running() {
        display.updateTimerScreen(1499, 1500, TimerService.State.RUNNING);
        String output = outContent.toString();
        // キャリッジリターン（\r）が含まれていることを確認（改行は含まれない）
        assertTrue(output.contains("\r作業を開始します。（25分）"));
        assertTrue(output.contains("\r残り 24:59"));
        assertTrue(output.contains("\r停止: stop / リセット: reset / 終了: end"));
    }

    @Test
    @DisplayName("タイマー一時停止中の画面が正しく更新されること")
    void testUpdateTimerScreen_Paused() {
        display.updateTimerScreen(900, 1500, TimerService.State.PAUSED);
        String output = outContent.toString();
        // キャリッジリターン（\r）が含まれていることを確認（改行は含まれない）
        assertTrue(output.contains("\r作業を停止しました。（25分）"));
        assertTrue(output.contains("\r残り 15:00"));
        assertTrue(output.contains("\r開始: start / リセット: reset / 終了: end"));
    }

    @Test
    @DisplayName("完了メッセージが正しく表示されること")
    void testShowCompletionMessage() {
        display.showCompletionMessage();
        String output = outContent.toString();
        // キャリッジリターン（\r）が含まれていることを確認
        assertTrue(output.contains("\rポモドーロが完了しました!"));
    }

    @Test
    @DisplayName("リセットメッセージが正しく表示されること")
    void testShowResetMessage() {
        display.showResetMessage();
        String output = outContent.toString();
        // キャリッジリターン（\r）が含まれていることを確認
        assertTrue(output.contains("\rリセットしました。"));
    }

    @Test
    @DisplayName("無効なコマンドメッセージが正しく表示されること")
    void testShowInvalidCommand() {
        display.showInvalidCommand("invalid");
        String output = outContent.toString();
        // キャリッジリターン（\r）が含まれていることを確認
        assertTrue(output.contains("\r'invalid' は無効なコマンドです。"));
    }
}
