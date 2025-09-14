package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroAppTest {

    @Mock
    private Display mockDisplay;

    @Mock
    private TimerService mockTimerService;

    private PomodoroApp app;

    @BeforeEach
    void setUp() throws Exception {
        app = new PomodoroApp();

        // リフレクションを使ってモックを注入
        setField(app, "display", mockDisplay);
        setField(app, "timerService", mockTimerService);
    }

    @Test
    @DisplayName("startコマンド（PAUSED時）でタイマーが再開されること")
    void testHandleCommand_start_from_paused() throws Exception {
        when(mockTimerService.getState()).thenReturn(TimerService.State.PAUSED);
        setField(app, "timerIsActive", true);

        callHandleCommand("start");

        verify(mockTimerService).start();
    }

    @Test
    @DisplayName("stopコマンドでタイマーが一時停止されること")
    void testHandleCommand_stop() throws Exception {
        when(mockTimerService.getState()).thenReturn(TimerService.State.RUNNING);
        setField(app, "timerIsActive", true);

        callHandleCommand("stop");

        verify(mockTimerService).pause();
    }

    @Test
    @DisplayName("resetコマンドでリセットメッセージが表示されること")
    void testHandleCommand_reset() throws Exception {
        setField(app, "timerIsActive", true);

        callHandleCommand("reset");

        verify(mockDisplay).showResetMessage();
        verify(mockDisplay).showMainMenu();
    }

    @Test
    @DisplayName("無効なコマンドでエラーメッセージが表示されること")
    void testHandleCommand_invalid() throws Exception {
        setField(app, "timerIsActive", true);

        callHandleCommand("invalid_command");

        verify(mockDisplay).showInvalidCommand("invalid_command");
    }

    // privateメソッドをリフレクションで呼び出すヘルパー
    private void callHandleCommand(String command) throws Exception {
        Method method = PomodoroApp.class.getDeclaredMethod("handleCommand", String.class);
        method.setAccessible(true);
        method.invoke(app, command);
    }

    // privateフィールドに値を設定するヘルパー
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
