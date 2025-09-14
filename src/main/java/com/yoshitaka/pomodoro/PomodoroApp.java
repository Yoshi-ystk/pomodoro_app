package com.yoshitaka.pomodoro;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * ポモドーロタイマーアプリケーションのメインクラス
 * ユーザーインタラクションとアプリケーション全体のフローを管理する
 *
 * このクラスは以下の役割を担う：
 * 1. アプリケーションの起動と終了の制御
 * 2. ユーザーからのコマンド入力の処理
 * 3. タイマーの開始・停止・リセットの管理
 * 4. 画面表示の更新指示
 */
public class PomodoroApp implements Runnable, TimerListener {

    // ポモドーロの作業時間（25分）
    private static final int WORK_MINUTES = 25;

    // 画面表示を管理するオブジェクト
    private final Display display = new Display();
    // タイマーの動作を管理するオブジェクト
    private TimerService timerService;
    // バックグラウンドでタイマーを実行するためのスレッドプール
    private ExecutorService executorService;

    // アプリケーションの終了状態を管理する volatile 変数
    // volatileキーワードにより、複数のスレッドから安全にアクセスできる
    private volatile boolean shouldExit = false;
    // タイマーがアクティブかどうかを管理するフラグ
    // タイマーが動作中かどうかを追跡する
    private volatile boolean timerIsActive = false;

    /*
     * アプリケーションのエントリーポイント
     * プログラムが開始されると最初に呼び出されるメソッドで
     */
    public static void main(String[] args) {
        new PomodoroApp().run(); // 新しいPomodoroAppインスタンスを作成して実行
    }

    /*
     * アプリケーションのメイン実行メソッド
     * Runnableインターフェースを実装しているため、このメソッドが呼び出される
     */
    @Override
    public void run() {
        initialize(); // アプリケーションの初期化
        display.showMainMenu(); // メインメニューを表示

        // ユーザー入力を受け付けるスレッドを作成
        // 別スレッドでユーザー入力を待機することで、メインスレッドをブロックしない
        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) { // 標準入力からスキャナーを作成
                while (!shouldExit) { // アプリケーションが終了していない間はループ
                    if (scanner.hasNextLine()) { // 入力があるかチェック
                        handleCommand(scanner.nextLine().trim()); // 入力されたコマンドを処理
                    }
                }
            }
        });
        inputThread.setDaemon(true); // デーモンスレッドに設定（メインスレッド終了時に自動終了）
        inputThread.start(); // 入力スレッドを開始

        // shouldExitフラグが立つまでメインスレッドを維持
        // メインスレッドが終了するとアプリケーション全体が終了するため、ループで待機
        while (!shouldExit) {
            try {
                TimeUnit.MILLISECONDS.sleep(100); // 100ミリ秒待機（CPU使用率を下げる）
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 割り込みフラグを再設定
                break; // ループを抜けて終了処理へ
            }
        }
        shutdown(); // アプリケーションの終了処理
    }

    /*
     * ユーザーから入力されたコマンドを処理するメソッド
     * 複数のスレッドからアクセスされるため synchronized で同期を取る
     *
     * @param command ユーザーが入力したコマンド文字列
     */
    private synchronized void handleCommand(String command) {
        if (shouldExit) // アプリケーションが終了予定の場合は何もしない
            return;

        switch (command) {
            case "start":
                handleStart(); // タイマー開始処理
                break;
            case "stop":
                // タイマーがアクティブかつ実行中の場合のみ停止
                if (timerIsActive && timerService.getState() == TimerService.State.RUNNING) {
                    timerService.pause(); // タイマーを一時停止
                }
                break;
            case "reset":
                // タイマーがアクティブな場合のみリセット
                if (timerIsActive) {
                    timerIsActive = false; // タイマーを非アクティブに設定
                    resetTimer(); // タイマーをリセット
                    display.showResetMessage(); // リセットメッセージを表示
                    display.showMainMenu(); // メインメニューに戻る
                }
                break;
            case "end":
                shouldExit = true; // アプリケーション終了フラグを立てる
                break;
            default:
                // 無効なコマンドの場合の処理
                if (!command.isEmpty()) { // 空文字列でない場合のみ処理
                    if (timerIsActive) {
                        display.showInvalidCommand(command); // タイマー実行中は無効コマンドメッセージを表示
                    } else {
                        // メインメニュー表示中に無効なコマンドが打たれた場合、メニューを再描画
                        display.showMainMenu();
                    }
                }
                break;
        }
    }

    /*
     * タイマー開始処理を行うメソッド
     * タイマーの状態に応じて新規開始または再開を行う
     */
    private synchronized void handleStart() {
        TimerService.State currentState = timerService.getState(); // 現在のタイマー状態を取得
        if (currentState == TimerService.State.IDLE) {
            // タイマーが停止状態の場合：新規開始
            timerIsActive = true; // タイマーをアクティブに設定
            resetTimer(); // 新規開始のためにタイマーをリセット
            display.drawInitialTimerScreen(); // タイマー画面を初期描画
            executorService = Executors.newSingleThreadExecutor(); // 単一スレッドの実行サービスを作成
            executorService.submit(timerService); // タイマーをバックグラウンドで実行
        } else if (currentState == TimerService.State.PAUSED) {
            // タイマーが一時停止状態の場合：再開
            timerService.start(); // PAUSEDからの再開
        }
    }

    /*
     * アプリケーションの初期化を行うメソッド
     * タイマーサービスを初期状態に設定する
     */
    private synchronized void initialize() {
        resetTimer(); // タイマーをリセットして初期状態にする
    }

    /*
     * タイマーをリセットするメソッド
     * 既存の実行サービスを停止し、新しいタイマーサービスを作成する
     */
    private synchronized void resetTimer() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // 既存の実行サービスを強制終了
        }
        timerService = new TimerService(WORK_MINUTES, this); // 新しいタイマーサービスを作成
    }

    /*
     * アプリケーションの終了処理を行うメソッド
     * リソースのクリーンアップと終了メッセージの表示を行う
     */
    private void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // 実行サービスを強制終了
        }
        // アプリケーション終了時にクリーンな状態で終了メッセージを表示
        System.out.print("\n"); // 改行を出力
        System.out.println("アプリを終了しました。"); // 終了メッセージを表示
    }

    /*
     * 画面表示を更新するプライベートメソッド
     * タイマーがアクティブな場合のみ画面を更新する
     */
    private void updateDisplay() {
        if (timerService != null && timerIsActive) { // タイマーサービスが存在し、アクティブな場合
            display.updateTimerScreen(timerService.getRemainingSeconds(), timerService.getTotalSeconds(),
                    timerService.getState()); // 残り時間、総時間、状態を表示に反映
        }
    }

    /*
     * TimerListenerインターフェースの実装メソッド
     * タイマーが1秒経過するたびに呼び出される
     *
     * @param remainingSeconds 残り秒数
     *
     * @param totalSeconds 総秒数
     */
    @Override
    public synchronized void onTick(long remainingSeconds, long totalSeconds) {
        updateDisplay(); // 画面表示を更新
    }

    /*
     * TimerListenerインターフェースの実装メソッド
     * タイマーが完了した時に呼び出される
     */
    @Override
    public synchronized void onFinish() {
        timerIsActive = false; // タイマーを非アクティブに設定
        display.showCompletionMessage(); // 完了メッセージを表示
        resetTimer(); // タイマーをリセット
        display.showMainMenu(); // メインメニューに戻る
    }

    /*
     * TimerListenerインターフェースの実装メソッド
     * タイマーの状態が変更された時に呼び出される（開始・停止・一時停止など）
     */
    @Override
    public synchronized void onStateChange() {
        updateDisplay(); // 画面表示を更新
    }
}
