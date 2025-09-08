package com.yoshitaka.pomodoro;

/**
 * ポモドーロタイマーアプリケーションのメインクラス
 */
public class PomodoroApp {

    private static final int WORK_MINUTES = 25;
    private static final int BREAK_MINUTES = 5;

    public static void main(String[] args) {
        Display display = new Display();
        TimerService timerService = new TimerService(display);

        display.showWelcomeMessage();

        // 無限にポモドーロサイクルを繰り返す
        while (true) {
            timerService.runTimer("作業", WORK_MINUTES);
            timerService.runTimer("休憩", BREAK_MINUTES);
        }
        // 現在のロジックでは到達しないが、将来的な拡張のために残しておく
        // display.showCompletionMessage();
    }
}
