
package org.team.app.model;

import org.team.app.view.ActivityListener;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ConcurrentModificationException;
import java.util.TreeSet;
import java.util.Collection;

import java.io.Serializable;

/// The TaskStore representation
public class TaskStore implements Serializable {
    // TODO support multiple tasks
    // TODO serialize and store in bundle

    protected Task currentTask = null;
    protected final String defaultTaskName;
    protected final String defaultTaskCategory;

    /// Listener for Task Store Updates
    public static interface Listener {
        /// Called when the current task is changed
        public void onCurrentTaskUpdate(Task newTask);

        public void onTaskAdded(Task newTask);
    }

    protected transient Set<Listener> listeners;
    protected Map<UUID, Task> list;

    private Set<Listener> getListeners() {
        synchronized (listeners) {
            Set<Listener> ret = null;
            while (ret == null) {
                try {
                    ret = new HashSet<Listener>(listeners);
                } catch (ConcurrentModificationException e) {
                    ret = null;
                }
            }
            return ret;
        }
    }

    /// Add a subscriber
    public void subscribe(Listener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /// Construct an in-memory Task Store
    /// @param defaultTaskName: the name for the default task
    public TaskStore(String defaultTaskName, String defaultTaskCategory) {
        this.defaultTaskName = defaultTaskName;
        this.defaultTaskCategory = defaultTaskCategory;
        // weak hash map so old listeners that have been GC'd will be removed
        this.listeners = Collections.newSetFromMap(new WeakHashMap<Listener,Boolean>());
        this.list = new HashMap<UUID, Task>();
    }

    private Object readResolve() {
        this.listeners = Collections.newSetFromMap(new WeakHashMap<Listener,Boolean>());
        return this;
    }

    /// Create a task
    /// @return the UUID/handle for the Task
    public UUID createTask(String taskName, String category) {
        Task task = new Task(taskName == null ? defaultTaskName : taskName,
                             category == null ? defaultTaskCategory : category);
        list.put(task.getUUID(), task);

        if(this.currentTask == null)
            this.currentTask = task;

        for (Listener listener : getListeners())
            listener.onTaskAdded(task);

        return task.getUUID();
    }

    /// Set the currently selected task
    public void setCurrentTask(UUID task) {
        Task target = list.get(task);
        if(target == null)
            return;

        this.currentTask = target;

        for (Listener listener : getListeners())
            listener.onCurrentTaskUpdate(currentTask);
    }

    /// Get a reference to the current task
    public Task getCurrentTask() {
        if(currentTask == null) {
            setCurrentTask(createTask(null, null));
        }
        return currentTask;
    }

    /// @return A collection of the tasks that match the filter
    /// @param filter: Empty string to get all the tasks,
    //    else only tasks with substring (case-insensitive matches)
    public Collection<Task> getTasks(String filter) {
        filter = filter.toLowerCase();

        TreeSet<Task> ret = new TreeSet<Task>();
        for(Map.Entry<UUID, Task> task: list.entrySet()) {
            // TODO: this is definitely slow, probably KMP for contains in the future
            if(filter.length() == 0 || task.getValue().getName().toLowerCase().contains(filter)) {
                ret.add(task.getValue());
            }
        }

        return (Collection<Task>) ret;
    }

    /// @return the Task object for a given UUID
    public Task getTaskByUUID(UUID uuid) {
        return list.get(uuid);
    }
}
