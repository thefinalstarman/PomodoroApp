import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.team.app.presenter.SetupTaskPresenter;

import org.team.app.contract.SetupTaskContract;
import org.team.app.model.TaskStore;
import org.team.app.model.TimerType;

import java.util.UUID;

class SetupTaskPresenterTest {
    static class MockView implements SetupTaskContract.View {
        public SetupTaskContract.Presenter mPresenter;
        public String name;
        public String category;

        @Override
        public void setPresenter(SetupTaskContract.Presenter presenter) {
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
        public void setTaskTime(TimerType type, long duration) {
            // TODO
        }
    }

    MockView view;
    TaskStore taskStore;
    SetupTaskPresenter presenter;

    @BeforeEach
    void setupTaskStoreAndView() {
        view = new MockView();
        taskStore = new TaskStore("default", "general");
        presenter = new SetupTaskPresenter(view, taskStore, taskStore.getCurrentTask().getUUID());
        presenter.start();
    }

    @Test
    // UID 001 RID 016 Presenters should be attached to views
    void presenterShouldAttachToProvidedView() {
        assertEquals(view.mPresenter, presenter);
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
}
