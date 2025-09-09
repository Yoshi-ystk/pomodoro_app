package com.yoshitaka.pomodoro;

/**
 * ポモドーロタイマーアプリケーションのメインクラス
 */
public class PomodoroApp {

    private static final int WORK_MINUTES = 25;
    private static final int BREAK_MINUTES = 5;
    private static final int TOTAL_SETS = 4;

    public static void main(String[] args) {
        // 依存関係のインスタンス化
        Display display = new Display();
        TimerService timerService = new TimerService(display);

        // アプリケーションロジックの実行
        runCycles(display, timerService, TOTAL_SETS);
    }

    /**
     * 指定されたセット数だけポモドーロサイクルを実行します。
     * このメソッドはテストから呼び出しやすいようにロジックを分離したものです。
     *
     * @param display      表示用コンポーネント
     * @param timerService タイマーサービスコンポーネント
     * @param totalSets    実行する合計セット数
     */
    public static void runCycles(Display display, TimerService timerService, int totalSets) {
        display.showWelcomeMessage();

        for (int set = 1; set <= totalSets; set++) {
            display.showSetCount(set, totalSets);
            timerService.runTimer("作業", WORK_MINUTES);

            // 最後のセットの後には短い休憩は入れない
            if (set < totalSets) {
                timerService.runTimer("休憩", BREAK_MINUTES);
            }
        }

        display.showCompletionMessage();
    }
}
