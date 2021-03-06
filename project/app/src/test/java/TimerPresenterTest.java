import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.team.app.presenter.TimerPresenter;

import org.team.app.contract.TimerContract;
import org.team.app.model.TaskStore;
import org.team.app.model.TimerType;

import java.util.UUID;

class TimerPresenterTest {
    static class MockView implements TimerContract.View {
        public TimerContract.Presenter mPresenter;
        public String name;
        public String category;
        public TimerType type;

        public long duration;
        public long maxDuration;
        public long debugElapsed = 0;

        public long stopCount = 0;
        public boolean isRunning = false;

        @Override
        public void setPresenter(TimerContract.Presenter presenter) {
            this.mPresenter = presenter;
        }

        @Override
        public void setTaskName(String name) {
            this.name = name;
        }

        @Override
        public void setTaskCategory(String category) {
            this.category = category;
        }

        @Override
        public void setTimerType(TimerType type) {
            this.type = type;
        }

        @Override
        public void startTimer(long duration, long maxDuration) {
            this.duration = duration;
            this.maxDuration = maxDuration;
            this.isRunning = true;
        }

        @Override
        public long stopTimer() {
            this.stopCount++;
            this.isRunning = false;
            return debugElapsed;
        }

        @Override
        public boolean running() {
            return this.isRunning;
        }
    }

    MockView view;
    TaskStore taskStore;
    TimerPresenter presenter;

    @BeforeEach
    void setupTaskStoreAndView() {
        view = new MockView();
        taskStore = new TaskStore("default", "general");
        presenter = new TimerPresenter(view, taskStore);
        presenter.start();
    }

    @Test
    // UID 001 RID 016 Presenters should be attached to views
    void presenterShouldAttachToProvidedViewAndSetTimerDetails() {
        assertEquals(view.mPresenter, presenter);
        assertEquals(view.type, TimerType.WORK);
        assertEquals(view.duration, taskStore.getCurrentTask().getTimerDuration(TimerType.WORK));
    }

    @Test
    // UID 001 RID 015 Model updates should be propogated ...
    // UID 019 RID 030 Changing current task resets timer
    void changingCurrentTaskShouldUpdateView() {
        String newTaskName = UUID.randomUUID().toString();
        UUID task = taskStore.createTask(newTaskName, null);
        taskStore.setCurrentTask(task);
        assertEquals(newTaskName, view.name);
        assertEquals(TimerType.WORK, view.type);
        assertEquals(taskStore.getTaskByUUID(task).getTimerDuration(view.type), view.duration);
    }

    @Test
    // UID 001 RID 015 Model updates should be propogated ...
    void changingCurrentTaskNameShouldUpdateView() {
        String newTaskName = UUID.randomUUID().toString();
        taskStore.getCurrentTask().setName(newTaskName);
        assertEquals(newTaskName, view.name);
    }

    @Test
    // UID 001 RID 015 Model updates should be propogated ...
    void changingCurrentTaskCategoryShouldUpdateView() {
        String newTaskCategory = UUID.randomUUID().toString();
        taskStore.getCurrentTask().setCategory(newTaskCategory);
        assertEquals(newTaskCategory, view.category);
    }

    @Test
    // UID 001 RID 017 Pausing the timer should save the current time.
    void resumingTimerShouldAdjustDuration() {
        view.debugElapsed = taskStore.getCurrentTask().getTimerDuration(view.type) - 1;
        presenter.onPauseButton();
        presenter.onPlayButton();
        assertEquals(view.duration, 1);
    }

    @Test
    // UID 001 RID 002 When the working timer completes
    void onTimerCompleteShouldResetTimerDetails() {
        TimerType original = view.type;
        view.duration = -1;
        presenter.onTimerComplete();
        assertNotEquals(view.type, original);
        assertNotEquals(view.duration, -1);
    }

    @Test
    // UID 001 RID 002 When the working timer completes ...
    void resumingCompletedTimerShouldChangeTimerType() {
        TimerType original = view.type;
        view.debugElapsed = taskStore.getCurrentTask().getTimerDuration(view.type) + 1;
        presenter.onPauseButton();
        presenter.onPlayButton();
        assertNotEquals(view.type, original);
    }

    @Test
    void updatingTimerDurationOfCurrentTimerShouldRestartTimer() {
        long originalCount = view.stopCount;
        long debugDuration = view.duration / 2;

        taskStore.getCurrentTask().setTimerDuration(view.type, debugDuration);

        assertEquals(view.duration, debugDuration);
        assert(view.stopCount > originalCount);
    }

    @Test
    void updatingTimerDurationOfOtherTimerShouldNotRestartTimer() {
        long originalCount = view.stopCount;
        long originalDuration = view.duration;
        long debugDuration = view.duration / 2;

        taskStore.getCurrentTask().setTimerDuration(view.type == TimerType.WORK ? TimerType.BREAK : TimerType.WORK,
                                                    debugDuration);

        assertEquals(view.duration, originalDuration);
        assertEquals(view.stopCount, originalCount);
    }

    @Test
    void updatingTimerDurationOfPausedTimerShouldNotPlay() {
        long debugDuration = view.duration / 2;

        presenter.onPauseButton();
        taskStore.getCurrentTask().setTimerDuration(view.type, debugDuration);
        assert(!view.running());
    }
}
