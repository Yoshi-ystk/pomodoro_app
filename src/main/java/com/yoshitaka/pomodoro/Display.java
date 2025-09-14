package com.yoshitaka.pomodoro;

import java.util.concurrent.TimeUnit;

/*
 * コンソールへの表示を管理するクラス
 * ANSIエスケープシーケンスを使用して、画面の部分的な更新やカーソル制御を行う
 *
 * このクラスは、ターミナル上でポモドーロタイマーの画面を表示する役割を担う
 * 画面全体をクリアせずに、必要な部分だけを更新することで、
 * ユーザーが入力中のコマンドを消してしまうことなく、リアルタイムで情報を更新できるようにする
 */
public class Display {

    /*
     * ANSIエスケープコード - ターミナルの表示制御に使用する特殊な文字列
     * これらの文字列を出力することで、カーソルの位置を移動したり、画面をクリアしたりできるようにする
     */
    private static final String CURSOR_UP = "\u001b[A"; // カーソルを1行上に移動
    private static final String CURSOR_DOWN = "\u001b[B"; // カーソルを1行下に移動
    private static final String CLEAR_LINE = "\u001b[2K"; // 現在の行をクリア
    private static final String SAVE_CURSOR = "\u001b[s"; // 現在のカーソル位置を保存
    private static final String RESTORE_CURSOR = "\u001b[u"; // 保存したカーソル位置に戻る
    private static final String HIDE_CURSOR = "\u001b[?25l"; // カーソルを非表示にする
    private static final String SHOW_CURSOR = "\u001b[?25h"; // カーソルを表示する
    private static final String CURSOR_HOME = "\u001b[H"; // カーソルを画面の左上に移動

    /*
     * Phase Areaの現在の状態を追跡するためのフィールド
     * 同じメッセージを重複して表示しないように、前回表示したメッセージを記録する
     */
    private String currentPhaseMessage = "";

    /*
     * 画面全体をクリアするメソッド
     * アプリケーション開始時や画面切り替え時に使用する
     */
    private void clearConsole() {
        System.out.print("\u001b[H\u001b[2J"); // カーソルを左上に移動して画面全体をクリア
        System.out.flush(); // 出力バッファを強制的にフラッシュ
    }

    /*
     * 待機状態のメインメニューを表示するメソッド
     * アプリケーション起動時やタイマー終了後に表示される
     */
    public synchronized void showMainMenu() {
        clearConsole(); // 画面をクリア
        System.out.print(SHOW_CURSOR); // カーソルを再表示（ユーザーが入力できるように）
        System.out.println("--------------------------------------------------");
        System.out.println("ポモドーロアプリを起動しました。");
        System.out.println("メニューを入力してください。");
        System.out.println("開始: start / 終了: end");
        System.out.println("--------------------------------------------------");
        System.out.print("> "); // コマンド入力プロンプト
        System.out.flush(); // 出力バッファを強制的にフラッシュ
    }

    /*
     * タイマー実行中の画面を初めて描画するメソッド
     * このメソッドは、後続の updateTimerScreen のために描画領域を確保する
     * 画面のレイアウトを決めて、各エリアの位置を固定する
     */
    public synchronized void drawInitialTimerScreen() {
        clearConsole(); // 画面をクリア
        System.out.print(HIDE_CURSOR); // カーソルを非表示（点滅を防ぐため）
        System.out.println("--------------------------------------------------");
        System.out.println("作業を開始します。（25分）"); // Phase Area - 初期メッセージを表示
        System.out.println(""); // Timer Area - 残り時間とプログレスバーを表示する領域
        System.out.println(""); // Menu Area - 操作メニューを表示する領域
        System.out.println("--------------------------------------------------");
        System.out.print(SHOW_CURSOR + "> "); // カーソルを再表示してプロンプトを出力
        System.out.flush(); // 出力バッファを強制的にフラッシュ
        currentPhaseMessage = "作業を開始します。（25分）"; // 初期状態を設定（重複表示を防ぐため）
    }

    /*
     * タイマー画面を更新するメソッド
     * フェーズ、残り時間、メニューなどを表示
     * カーソル制御により、ユーザーの入力を妨げずに画面を更新する
     *
     * このメソッドの重要なポイント：
     * 1. カーソル位置を保存してから画面を更新
     * 2. 各エリアを個別に更新（重複表示を防ぐ）
     * 3. 更新後にカーソル位置を復元
     *
     * @param remainingSeconds 残り秒数
     *
     * @param totalSeconds 総秒数（通常は1500秒 = 25分）
     *
     * @param state タイマーの現在の状態 (RUNNING=実行中, PAUSED=一時停止中)
     */
    public synchronized void updateTimerScreen(long remainingSeconds, long totalSeconds, TimerService.State state) {
        System.out.print(SAVE_CURSOR); // 現在のカーソル位置を保存
        System.out.print(HIDE_CURSOR); // カーソルを非表示（更新中のちらつきを防ぐ）

        // Phase Areaのメッセージを生成
        long totalMinutes = TimeUnit.SECONDS.toMinutes(totalSeconds); // 秒数を分に変換
        String phaseLabel;
        if (state == TimerService.State.RUNNING) {
            phaseLabel = String.format("作業を開始します。（%d分）", totalMinutes); // 実行中メッセージ
        } else { // PAUSED
            phaseLabel = String.format("作業を停止しました。（%d分）", totalMinutes); // 一時停止メッセージ
        }

        // Phase Areaのメッセージが変更された場合のみ更新（重複表示を防ぐ）
        if (!phaseLabel.equals(currentPhaseMessage)) {
            // 画面の2行目（Phase Area）に直接移動
            System.out.print(CURSOR_HOME); // 画面の左上に移動
            System.out.print(CURSOR_DOWN); // 1行目（区切り線）をスキップして2行目に移動
            System.out.print(CLEAR_LINE + "\r" + phaseLabel); // 行をクリアしてから新しいメッセージを表示
            currentPhaseMessage = phaseLabel; // 現在のメッセージを記録
        }

        // Timer Areaを更新（残り時間とプログレスバーを表示）
        System.out.print(CURSOR_HOME); // 画面の左上に移動
        System.out.print(CURSOR_DOWN + CURSOR_DOWN); // Phase Areaの次の行（3行目）に移動
        String timeString = String.format("%02d:%02d", TimeUnit.SECONDS.toMinutes(remainingSeconds),
                remainingSeconds % 60); // 残り時間を MM:SS 形式でフォーマット
        double progress = (totalSeconds > 0) ? (double) (totalSeconds - remainingSeconds) / totalSeconds : 0; // 進捗率を計算
        String progressBarString = ProgressBar.generate(progress); // プログレスバーを生成
        String timerLine = String.format("残り %s [%s] %d%%", timeString, progressBarString, (int) (progress * 100)); // タイマー行を組み立て
        System.out.print(CLEAR_LINE + "\r" + timerLine); // 行をクリアしてから新しいタイマー情報を表示

        // Menu Areaを更新（操作メニューを表示）
        System.out.print(CURSOR_HOME); // 画面の左上に移動
        System.out.print(CURSOR_DOWN + CURSOR_DOWN + CURSOR_DOWN); // Timer Areaの次の行（4行目）に移動
        String menuLine;
        if (state == TimerService.State.RUNNING) {
            menuLine = "停止: stop / リセット: reset / 終了: end"; // 実行中のメニュー
        } else { // PAUSED
            menuLine = "開始: start / リセット: reset / 終了: end"; // 一時停止中のメニュー
        }
        System.out.print(CLEAR_LINE + "\r" + menuLine); // 行をクリアしてから新しいメニューを表示

        System.out.print(RESTORE_CURSOR); // 保存したカーソル位置に戻る
        System.out.print(SHOW_CURSOR); // カーソルを再表示
        System.out.flush(); // 出力バッファを強制的にフラッシュ
    }

    /*
     * 画面上部のメッセージエリアに一時的なメッセージを表示するプライベートメソッド
     * 完了メッセージやリセットメッセージなど、短時間表示するメッセージに使用する
     *
     * @param message 表示するメッセージ
     *
     * @param seconds 表示する秒数。0以下の場合は消去しない
     */
    private synchronized void showMessage(String message, int seconds) {
        System.out.print(SAVE_CURSOR); // 現在のカーソル位置を保存
        System.out.print(HIDE_CURSOR); // カーソルを非表示
        // メッセージエリアに移動（プロンプトから5行上）
        System.out.print(CURSOR_UP + CURSOR_UP + CURSOR_UP + CURSOR_UP + CURSOR_UP);
        System.out.print(CLEAR_LINE + "\r" + message); // 行をクリアしてからメッセージを表示
        System.out.print(RESTORE_CURSOR); // カーソル位置を復元
        System.out.print(SHOW_CURSOR); // カーソルを再表示
        System.out.flush(); // 出力バッファを強制的にフラッシュ

        if (seconds > 0) { // 指定された秒数後にメッセージを消去
            sleep(seconds); // 指定された秒数だけ待機
            // メッセージを消去
            System.out.print(SAVE_CURSOR); // カーソル位置を保存
            System.out.print(HIDE_CURSOR); // カーソルを非表示
            System.out.print(CURSOR_UP + CURSOR_UP + CURSOR_UP + CURSOR_UP + CURSOR_UP); // メッセージエリアに移動
            System.out.print(CLEAR_LINE + "\r"); // 行をクリア（メッセージを消去）
            System.out.print(RESTORE_CURSOR); // カーソル位置を復元
            System.out.print(SHOW_CURSOR); // カーソルを再表示
            System.out.flush(); // 出力バッファを強制的にフラッシュ
        }
    }

    /*
     * ポモドーロ完了メッセージを表示するメソッド
     * 25分の作業が完了した時に呼び出される
     */
    public void showCompletionMessage() {
        showMessage("ポモドーロが完了しました！", 2); // 2秒間表示してから消去
    }

    /*
     * リセットメッセージを表示するメソッド
     * タイマーがリセットされた時に呼び出される
     */
    public void showResetMessage() {
        showMessage("リセットしました。", 1); // 1秒間表示してから消去
    }

    /*
     * 無効なコマンドが入力されたことを示すメッセージを表示するメソッド
     * ユーザーが存在しないコマンドを入力した時に呼び出される
     *
     * @param command 入力された無効なコマンド
     */
    public void showInvalidCommand(String command) {
        showMessage(String.format("'%s' は無効なコマンドです。", command), 1); // 1秒間表示してから消去
    }

    /*
     * 指定された秒数だけ処理を停止するプライベートメソッド
     * メッセージ表示時の待機時間に使用する
     *
     * @param seconds 停止する秒数
     */
    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds); // 指定された秒数だけ待機
        } catch (InterruptedException e) {
            // スレッドが割り込まれた場合は、割り込みフラグを再設定
            Thread.currentThread().interrupt();
        }
    }
}
