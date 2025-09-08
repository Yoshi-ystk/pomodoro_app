package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ProgressBarクラスのテスト
 */
class ProgressBarTest {

    @Test
    @DisplayName("進捗0%の場合、バーが空であること")
    void testGenerate_ZeroProgress() {
        // 30文字のバーを期待
        assertEquals("[" + "-".repeat(30) + "]", ProgressBar.generate(0.0));
    }

    @Test
    @DisplayName("進捗50%の場合、バーが半分満たされること")
    void testGenerate_HalfProgress() {
        // 15文字の#と15文字の-を期待
        assertEquals("[" + "#".repeat(15) + "-".repeat(15) + "]", ProgressBar.generate(0.5));
    }

    @Test
    @DisplayName("進捗100%の場合、バーが全て満たされること")
    void testGenerate_FullProgress() {
        // 30文字の#を期待
        assertEquals("[" + "#".repeat(30) + "]", ProgressBar.generate(1.0));
    }

    @Test
    @DisplayName("進捗33%の場合、バーが正しく計算されること")
    void testGenerate_ThirdProgress() {
        // 30 * 0.33 = 9.9 -> 9文字の#と21文字の-を期待
        assertEquals("[" + "#".repeat(9) + "-".repeat(21) + "]", ProgressBar.generate(0.33));
    }
}
