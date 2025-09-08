package com.yoshitaka.pomodoro;

import java.util.concurrent.TimeUnit;

/**
 * 時間管理とカウントダウンのロジックを担当するクラス
 */
public class TimerService {

    private final Display display;

    public TimerService(Display display) {
        this.display = display;
    }

    /**
     * 指定された時間でタイマーを実行し、コンソールに進捗を表示します
     *
     * @param phaseName       フェーズ名（例: "作業"）
     * @param durationMinutes タイマーの時間（分）
     */
    public void runTimer(String phaseName, int durationMinutes) {
        display.showPhaseStart(phaseName, durationMinutes);
        long totalSeconds = TimeUnit.MINUTES.toSeconds(durationMinutes);

        try {
            for (long elapsedSeconds = 0; elapsedSeconds <= totalSeconds; elapsedSeconds++) {
                updateTimerDisplay(elapsedSeconds, totalSeconds);
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            display.showError("タイマーが中断されました。");
            Thread.currentThread().interrupt();
        }
        display.showPhaseEnd(phaseName);
    }

    /**
     * コンソールのタイマー表示を更新します
     *
     * @param elapsedSeconds 経過時間（秒）
     * @param totalSeconds   合計時間（秒）
     */
    private void updateTimerDisplay(long elapsedSeconds, long totalSeconds) {
        long remainingSeconds = totalSeconds - elapsedSeconds;
        long minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds);
        long seconds = remainingSeconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);

        double progress = (double) elapsedSeconds / totalSeconds;
        int percentage = (int) (progress * 100);
        String progressBarString = ProgressBar.generate(progress);

        display.updateTimer(timeString, progressBarString, percentage);
    }
}
