package com.yoshitaka.pomodoro;

/**
 * コンソールへの表示を担当するクラス
 */
public class Display {

    /**
     * Welcomeメッセージを表示します
     */
    public void showWelcomeMessage() {
        System.out.println("ポモドーロタイマーを開始します。");
    }

    /**
     * 現在のセット数を表示します
     *
     * @param currentSet 現在のセット数
     * @param totalSets  合計セット数
     */
    public void showSetCount(int currentSet, int totalSets) {
        System.out.printf("%n--- セット %d / %d ---%n", currentSet, totalSets);
    }

    /**
     * 指定されたフェーズの開始メッセージを表示します
     *
     * @param phaseName       フェーズ名（例: "作業"）
     * @param durationMinutes 時間（分）
     */
    public void showPhaseStart(String phaseName, int durationMinutes) {
        System.out.printf("%n%sを開始します。（%d分）%n", phaseName, durationMinutes);
    }

    /**
     * タイマーの表示を更新します
     * ANSIエスケープシーケンスを使用して、行を上書きします。
     *
     * @param timeString        表示する時間文字列 (例: "24:59")
     * @param progressBarString 表示する進捗バー文字列 (例: "[##--]")
     * @param percentage        進捗率 (例: 10)
     */
    public void updateTimer(String timeString, String progressBarString, int percentage) {
        String output = String.format("残り %s %s %d%%", timeString, progressBarString, percentage);
        // カーソルを行頭に戻して行をクリア
        System.out.print("\r\u001b[2K");
        System.out.print(output);
        System.out.flush();
    }

    /**
     * フェーズの終了メッセージを表示します
     *
     * @param phaseName フェーズ名（例: "作業"）
     */
    public void showPhaseEnd(String phaseName) {
        System.out.printf("%n%s終了！%n", phaseName);
    }

    /**
     * 完了メッセージを表示します
     */
    public void showCompletionMessage() {
        System.out.println("ポモドーロセッションが完了しました。お疲れ様でした！");
    }

    /**
     * エラーメッセージを表示します。
     *
     * @param message エラーメッセージ
     */
    public void showError(String message) {
        System.err.println(message);
    }
}
