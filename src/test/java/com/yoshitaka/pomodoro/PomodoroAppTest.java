package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroAppTest {

    @Mock
    private Display mockDisplay;

    @Mock
    private TimerService mockTimerService;

    private int workMinutes;
    private int breakMinutes;

    @BeforeEach
    void setUp() throws Exception {
        // PomodoroAppのprivate final定数をリフレクションで取得
        workMinutes = getStaticFinalField(PomodoroApp.class, "WORK_MINUTES");
        breakMinutes = getStaticFinalField(PomodoroApp.class, "BREAK_MINUTES");
    }

    @Test
    void testRunCycles_withFourSets() {
        final int totalSets = 4;

        // テスト対象のロジックを実行
        PomodoroApp.runCycles(mockDisplay, mockTimerService, totalSets);

        // メソッドが呼ばれた順序を記録
        InOrder inOrder = inOrder(mockDisplay, mockTimerService);

        // 開始メッセージが呼ばれたか
        inOrder.verify(mockDisplay).showWelcomeMessage();

        // ループ処理が期待通りか
        for (int set = 1; set <= totalSets; set++) {
            inOrder.verify(mockDisplay).showSetCount(set, totalSets);
            inOrder.verify(mockTimerService).runTimer("作業", workMinutes);

            if (set < totalSets) {
                inOrder.verify(mockTimerService).runTimer("休憩", breakMinutes);
            }
        }

        // 完了メッセージが呼ばれたか
        inOrder.verify(mockDisplay).showCompletionMessage();
        verifyNoMoreInteractions(mockDisplay, mockTimerService);
    }

    @Test
    void testRunCycles_withOneSet() {
        final int totalSets = 1;

        PomodoroApp.runCycles(mockDisplay, mockTimerService, totalSets);

        InOrder inOrder = inOrder(mockDisplay, mockTimerService);

        inOrder.verify(mockDisplay).showWelcomeMessage();
        inOrder.verify(mockDisplay).showSetCount(1, 1);
        inOrder.verify(mockTimerService).runTimer("作業", workMinutes);
        inOrder.verify(mockDisplay).showCompletionMessage();

        verify(mockTimerService, never()).runTimer(eq("休憩"), anyInt());
        verifyNoMoreInteractions(mockDisplay, mockTimerService);
    }

    @Test
    void testRunCycles_withZeroSets() {
        final int totalSets = 0;

        PomodoroApp.runCycles(mockDisplay, mockTimerService, totalSets);

        InOrder inOrder = inOrder(mockDisplay, mockTimerService);

        inOrder.verify(mockDisplay).showWelcomeMessage();
        inOrder.verify(mockDisplay).showCompletionMessage();

        verify(mockDisplay, never()).showSetCount(anyInt(), anyInt());
        verify(mockTimerService, never()).runTimer(anyString(), anyInt());
        verifyNoMoreInteractions(mockDisplay, mockTimerService);
    }

    // リフレクションを使ってprivate static finalなフィールドの値を取得するヘルパーメソッド
    private static int getStaticFinalField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (int) field.get(null);
    }
}
