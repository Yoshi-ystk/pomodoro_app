package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Displayクラスのテスト
 * 標準出力をキャプチャして検証します
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
    @DisplayName("Welcomeメッセージが正しく表示されること")
    void testShowWelcomeMessage() {
        display.showWelcomeMessage();
        // OSの改行コードに依存しないように\nを使う
        assertEquals("ポモドーロタイマーを開始します。" + System.lineSeparator(), outContent.toString());
    }

    @Test
    @DisplayName("フェーズ開始メッセージが正しく表示されること")
    void testShowPhaseStart() {
        display.showPhaseStart("テスト", 10);
        String expected = String.format("%nテストを開始します。（10分）%n");
        assertEquals(expected, outContent.toString());
    }

    @Test
    @DisplayName("タイマー表示が正しく更新されること")
    void testUpdateTimer() {
        display.updateTimer("12:34", "[###---]", 50);
        // \rとANSIエスケープシーケンスを含む出力を検証
        String expected = "\r\u001b[2K残り 12:34 [###---] 50%";
        assertEquals(expected, outContent.toString());
    }
}
