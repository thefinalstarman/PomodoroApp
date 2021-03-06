package org.team.app.contract;

import org.team.app.model.TimerType;

public interface TimerContract {
    interface View extends BaseView<Presenter> {
        /// Set the task name in response to model updates
        void setTaskName(String taskName);

        /// Set the task category in response to model updates
        void setTaskCategory(String taskCategory);

        /// Set the displayed timer title based on type
        void setTimerType(TimerType type);

        /// Start a new timer, updating the timer text every tick.
        /// Calls onTimerComplete() on the attached presenter when done.
        /// @param duration: the time in milliseconds to run before finished.
        void startTimer(long duration, long maxDuration);

        /// Stop the timer. Does not call onTimerComplete()
        /// @return the elapsed time in milliseconds
        long stopTimer();

        /// @return true if the timer is running, else false
        boolean running();
    }

    interface Presenter extends BasePresenter {
        /// Called when a started timer completes (time elapsed >= duration)
        void onTimerComplete();

        /// Called when a skip button is clicked
        void onTimerSkip();

        /// Called when the pause button is hit on the view
        void onPauseButton();

        /// Called when the play button is hit on the view
        void onPlayButton();
    }
}
