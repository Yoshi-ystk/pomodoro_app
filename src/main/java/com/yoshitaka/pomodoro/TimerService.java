package com.yoshitaka.pomodoro;

import java.util.concurrent.TimeUnit;

/*
 * ポモドーロタイマーの動作を管理するクラス
 * 指定された時間（通常25分）のカウントダウンタイマーを提供する
 *
 * このクラスは以下の機能を提供する：
 * 1. タイマーの開始・一時停止・再開・リセット
 * 2. 1秒ごとのカウントダウン処理
 * 3. タイマー完了時の通知
 * 4. 状態変更時の通知
 */
public class TimerService implements Runnable {

    /*
     * タイマーの状態を表す列挙型
     */
    public enum State {
        IDLE, // 停止状態（初期状態）
        RUNNING, // 実行中
        PAUSED // 一時停止中
    }

    // タイマーの総時間（秒）
    private final long totalSeconds;
    // 残り時間（秒）
    private long remainingSeconds;
    // 現在のタイマー状態（volatileで複数スレッドからの安全なアクセスを保証）
    private volatile State state;
    // タイマーの状態変化を通知するためのリスナー
    private final TimerListener listener;

    /*
     * タイマーサービスのコンストラクタ
     *
     * @param durationMinutes タイマーの継続時間（分）
     *
     * @param listener タイマーの状態変化を通知するリスナー
     */
    public TimerService(int durationMinutes, TimerListener listener) {
        this.totalSeconds = TimeUnit.MINUTES.toSeconds(durationMinutes); // 分を秒に変換
        this.listener = listener; // リスナーを設定
        this.state = State.IDLE; // 初期状態をIDLEに設定
        this.remainingSeconds = this.totalSeconds; // 残り時間を総時間で初期化
    }

    /*
     * Runnableインターフェースの実装メソッド
     * タイマーのカウントダウン処理を実行する
     * このメソッドは別スレッドで実行される
     */
    @Override
    public void run() {
        // タイマーが開始されると、まず状態をRUNNINGにする
        if (state == State.IDLE) {
            state = State.RUNNING; // 状態を実行中に変更
            if (listener != null) {
                listener.onStateChange(); // 状態変更をリスナーに通知
            }
        }

        // 残り時間が0になるまでループ
        while (remainingSeconds > 0) {
            if (Thread.currentThread().isInterrupted()) { // スレッドが中断された場合
                break; // ループを抜ける
            }

            if (state == State.RUNNING) { // タイマーが実行中の場合
                try {
                    TimeUnit.SECONDS.sleep(1); // 1秒待機
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 中断状態を再設定
                    break; // ループを抜ける
                }
                remainingSeconds--; // 残り時間を1秒減らす
                if (listener != null) {
                    listener.onTick(remainingSeconds, totalSeconds); // 1秒経過をリスナーに通知
                }
            } else {
                // PAUSED状態の場合、スレッドをブロックせずに待機
                try {
                    TimeUnit.MILLISECONDS.sleep(100); // 100ミリ秒待機
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 中断状態を再設定
                    break; // ループを抜ける
                }
            }
        }

        // タイマーが完了した場合（中断されていない場合のみ）
        if (remainingSeconds <= 0 && !Thread.currentThread().isInterrupted()) {
            state = State.IDLE; // 状態をIDLEに戻す
            if (listener != null) {
                listener.onFinish(); // 完了をリスナーに通知
            }
        }
    }

    /*
     * タイマーを開始または再開するメソッド
     * PAUSED状態からRUNNING状態に変更する
     */
    public void start() {
        if (state == State.PAUSED) { // 一時停止中の場合のみ再開
            state = State.RUNNING; // 状態を実行中に変更
            if (listener != null) {
                listener.onStateChange(); // 状態変更をリスナーに通知
            }
        }
    }

    /*
     * タイマーを一時停止するメソッド
     * RUNNING状態からPAUSED状態に変更する
     */
    public void pause() {
        if (state == State.RUNNING) { // 実行中の場合のみ一時停止
            state = State.PAUSED; // 状態を一時停止中に変更
            if (listener != null) {
                listener.onStateChange(); // 状態変更をリスナーに通知
            }
        }
    }

    /*
     * 現在のタイマー状態を取得するメソッド
     *
     * @return 現在の状態（IDLE, RUNNING, PAUSED）
     */
    public State getState() {
        return state;
    }

    /*
     * 残り時間を取得するメソッド
     *
     * @return 残り時間（秒）
     */
    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    /*
     * 総時間を取得するメソッド
     *
     * @return 総時間（秒）
     */
    public long getTotalSeconds() {
        return totalSeconds;
    }
}
