package com.yoshitaka.pomodoro;

/*
 * TimerServiceの状態変化を通知するためのリスナーインターフェース
 *
 * このインターフェースを実装することで、タイマーの状態変化を監視できる
 * TimerServiceから以下のタイミングで通知を受け取ることができる：
 * 1. タイマーが1秒進むたび
 * 2. タイマーが完了した時
 * 3. タイマーの状態が変更された時（開始・停止・一時停止・再開など）
 *
 * オブザーバーパターンの実装例として使用されている
 */
public interface TimerListener {

    /*
     * タイマーが1秒進むごとに呼び出されるメソッド
     * 画面の更新やプログレスバーの更新などに使用する
     *
     * @param remainingSeconds 残り時間（秒）
     *
     * @param totalSeconds 合計時間（秒）
     */
    void onTick(long remainingSeconds, long totalSeconds);

    /*
     * タイマーが完了したときに呼び出されるメソッド
     * 完了メッセージの表示や次の処理への移行などに使用する
     */
    void onFinish();

    /*
     * タイマーの状態が変更されたときに呼び出されるメソッド
     * 開始・停止・一時停止・再開などの状態変化を検知できる
     */
    void onStateChange();
}
