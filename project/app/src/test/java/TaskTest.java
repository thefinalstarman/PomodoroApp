import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.team.app.model.Task;
import org.team.app.model.TaskStore;

import java.util.UUID;
import java.lang.ref.WeakReference;

class TaskTest {
    static class MockSubscriber implements Task.Listener, TaskStore.Listener {
        public String name = null;

        @Override
        public void onCurrentTaskUpdate(Task newTask) {
            this.name = newTask.getName();
        }

        @Override
        public void onTaskNameUpdate(Task task, String name) {
            this.name = name;
        }
    }

    @Test
    void createTaskWithName() {
        String testName = "TEST";
        Task task = new Task(testName);
        assertEquals(task.getName(), testName);
    }

    @Test
    void taskShouldHaveUniqueUUID() {
        Task task = new Task("A");
        Task task2 = new Task("A");

        assertNotEquals(task.getUUID(), task2.getUUID());
    }

    @Test
    void updatingTaskNameShouldUpdateSubscribers() {
        MockSubscriber sub = new MockSubscriber();

        Task task = new Task("A");
        task.subscribe(sub);

        String updatedTaskName = UUID.randomUUID().toString();
        task.setName(updatedTaskName);

        assertEquals(sub.name, updatedTaskName);
    }

    @Test
    // Listener maps in Task/TaskStore should hold weak references to
    // listeners so that they can be GC'd.
    void listenerMapsShouldHoldWeakReferences() {
        TaskStore store = new TaskStore("A");

        final int M = 1000;
        MockSubscriber[] subs = new MockSubscriber[M];

        // Setup the first mock subscribe with a weak reference
        // If the object eventually gets GC'd, the reference should
        // become null.
        subs[0] = new MockSubscriber();
        store.subscribe(subs[0]);
        store.getCurrentTask().subscribe(subs[0]);
        WeakReference shouldDie = new WeakReference(subs[0]);

        for (int i = 0; i < 10000 && shouldDie.get() != null; i++) {
            subs[i % M] = new MockSubscriber();
            store.subscribe(subs[i % M]);
            store.getCurrentTask().subscribe(subs[i % M]);

            if(i == 0)
                System.gc();
        }

        assert(shouldDie.get() == null);
    }

    @Test
    void taskStoreShouldStartWithDefaultTask() {
        String defaultName = "default";
        TaskStore store = new TaskStore(defaultName);
        assertEquals(store.getCurrentTask().getName(), defaultName);
    }

    @Test
    void taskStoreCreateTaskShouldSetCurrentTask() {
        TaskStore store = new TaskStore("default");

        String taskName = "TEST";
        store.createTask(taskName);

        assertEquals(store.getCurrentTask().getName(), taskName);
    }

    @Test
    void updateCurrentTaskShouldUpdateSubscribers() {
        MockSubscriber sub = new MockSubscriber();

        TaskStore store = new TaskStore("default");
        store.subscribe(sub);

        String updatedTaskName = UUID.randomUUID().toString();
        store.createTask(updatedTaskName);

        assertEquals(sub.name, updatedTaskName);
    }
}
