package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TimerServiceクラスのテスト
 */
class TimerServiceTest {

    /**
     * Displayクラスのメソッド呼び出しを記録するためのスパイ（テストダブル）
     */
    static class DisplaySpy extends Display {
        boolean showPhaseStartCalled = false;
        boolean updateTimerCalled = false;
        boolean showPhaseEndCalled = false;
        String lastPhaseName = "";

        @Override
        public void showPhaseStart(String phaseName, int durationMinutes) {
            this.showPhaseStartCalled = true;
            this.lastPhaseName = phaseName;
        }

        @Override
        public void updateTimer(String timeString, String progressBarString, int percentage) {
            this.updateTimerCalled = true;
        }

        @Override
        public void showPhaseEnd(String phaseName) {
            this.showPhaseEndCalled = true;
        }
    }

    @Test
    @DisplayName("runTimerが0分の時、開始と終了メソッドが呼ばれること")
    void testRunTimer_ZeroDuration() throws InterruptedException {
        DisplaySpy displaySpy = new DisplaySpy();
        TimerService timerService = new TimerService(displaySpy);

        // 0分のタイマーを実行
        timerService.runTimer("テストフェーズ", 0);

        // 各メソッドが1回は呼ばれたことを確認
        assertTrue(displaySpy.showPhaseStartCalled, "showPhaseStartが呼ばれていません");
        assertTrue(displaySpy.updateTimerCalled, "updateTimerが呼ばれていません");
        assertTrue(displaySpy.showPhaseEndCalled, "showPhaseEndが呼ばれていません");
        assertEquals("テストフェーズ", displaySpy.lastPhaseName, "フェーズ名が正しく渡されていません");
    }
}
