package com.yoshitaka.pomodoro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * TimerServiceクラスのテスト
 */
@ExtendWith(MockitoExtension.class)
class TimerServiceTest {

    @Mock
    private TimerListener listenerMock;

    @Test
    @DisplayName("タイマーが開始されるとonTickが呼ばれること")
    void testTimerRun_ticks() throws InterruptedException {
        TimerService timerService = new TimerService(1, listenerMock); // 1-minute timer

        Thread thread = new Thread(timerService);
        thread.start();

        // onStateChangeが呼ばれてRUNNING状態になるのを待つ
        verify(listenerMock, timeout(100).atLeastOnce()).onStateChange();
        assertEquals(TimerService.State.RUNNING, timerService.getState());

        // onTickが少なくとも1回呼ばれるのを待つ (1秒待つ)
        verify(listenerMock, timeout(1100).atLeastOnce()).onTick(anyLong(), anyLong());

        // スレッドを中断してテストを終了
        thread.interrupt();
        thread.join();

        // onFinishが呼ばれていないことを確認
        verify(listenerMock, never()).onFinish();
    }

    @Test
    @DisplayName("タイマーが0分の時、onFinishのみが呼ばれること")
    void testTimerRun_ZeroDuration() throws InterruptedException {
        TimerService timerService = new TimerService(0, listenerMock);

        Thread thread = new Thread(timerService);
        thread.start();
        thread.join(); // すぐに終了するはず

        // onTickは一度も呼ばれない
        verify(listenerMock, never()).onTick(anyLong(), anyLong());
        // onFinishが1回呼ばれる
        verify(listenerMock, times(1)).onFinish();
    }

    @Test
    @DisplayName("pauseとstartで状態が変化し、リスナーが呼ばれること")
    void testPauseAndStart() throws InterruptedException {
        TimerService timerService = new TimerService(1, listenerMock);

        // 初期状態はIDLE
        assertEquals(TimerService.State.IDLE, timerService.getState());

        Thread thread = new Thread(timerService);
        thread.start();

        // onStateChangeが呼ばれてRUNNING状態になるのを待つ
        verify(listenerMock, timeout(100).atLeastOnce()).onStateChange();
        assertEquals(TimerService.State.RUNNING, timerService.getState());

        timerService.pause();
        assertEquals(TimerService.State.PAUSED, timerService.getState());

        timerService.start();
        assertEquals(TimerService.State.RUNNING, timerService.getState());

        // 状態変化のたびにonStateChangeが呼ばれることを確認
        // start(IDLE->RUNNING), pause(RUNNING->PAUSED), start(PAUSED->RUNNING) の3回
        verify(listenerMock, timeout(100).times(3)).onStateChange();

        // スレッドを停止
        thread.interrupt();
        thread.join();
    }
}
