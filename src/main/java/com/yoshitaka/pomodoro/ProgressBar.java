package com.yoshitaka.pomodoro;

/**
 * コンソール用の進捗バーを生成するユーティリティクラス
 *
 * このクラスは、タイマーの進捗を視覚的に表現するための
 * テキストベースのプログレスバーを生成する
 *
 * 使用例：
 * - 進捗率0.0の場合: "[------------------------------]"
 * - 進捗率0.5の場合: "[###############---------------]"
 * - 進捗率1.0の場合: "[##############################]"
 */
public class ProgressBar {

    // プログレスバーの長さ（文字数）
    private static final int BAR_LENGTH = 30;

    /**
     * 進捗率に基づいてプログレスバーの文字列を生成するメソッド
     *
     * このメソッドは、0.0から1.0の進捗率を受け取り、
     * 完了部分を'#'、未完了部分を'-'で表現した
     * プログレスバー文字列を返す
     *
     * @param progress 進捗率 (0.0 ~ 1.0)
     *                 - 0.0: 開始時（0%完了）
     *                 - 1.0: 完了時（100%完了）
     * @return プログレスバーの文字列 (例: "[##########----------]")
     */
    public static String generate(double progress) {
        // 進捗率に基づいて、完了部分の文字数を計算
        int completedChars = (int) (progress * BAR_LENGTH);
        // 残り部分の文字数を計算
        int remainingChars = BAR_LENGTH - completedChars;

        // プログレスバーの文字列を生成
        StringBuilder bar = new StringBuilder("[");
        bar.append("#".repeat(completedChars)); // 完了部分を'#'で埋める
        bar.append("-".repeat(remainingChars)); // 残り部分を'-'で埋める
        bar.append("]"); // 終了ブラケットを追加する

        return bar.toString(); // 完成したプログレスバー文字列を返す
    }
}
