package com.yoshitaka.pomodoro;

/**
 * コンソール用の進捗バーを生成するクラス
 */
public class ProgressBar {

    private static final int BAR_LENGTH = 30;

    /**
     * 進捗率に基づいて進捗バーの文字列を生成します
     *
     * @param progress 進捗率 (0.0 ~ 1.0)
     * @return 進捗バーの文字列 (例: "[##########----------]")
     */
    public static String generate(double progress) {
        int completedChars = (int) (progress * BAR_LENGTH);
        int remainingChars = BAR_LENGTH - completedChars;

        StringBuilder bar = new StringBuilder("[");
        bar.append("#".repeat(completedChars));
        bar.append("-".repeat(remainingChars));
        bar.append("]");

        return bar.toString();
    }
}
