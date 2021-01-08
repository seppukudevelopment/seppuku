package me.rigamortis.seppuku.api.task.basic;

import me.rigamortis.seppuku.api.task.TaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BasicTaskFactory<T extends BasicTask> implements TaskFactory<T> {

    private final List<T> tasks = new ArrayList<>();

    private BasicTask currentTask = null;

    public void addTask(T task) {
        this.tasks.add(task);
    }

    public void removeTask(String name) {
        this.tasks.remove(getTask(name));
    }

    public void removeTask(T task) {
        this.tasks.remove(task);
    }

    public List<T> getTasks() {
        return this.tasks;
    }

    public boolean startTask(T task) {
        if (this.currentTask == task) {
            this.currentTask.setOnline(true);
            return true;
        }
        if (isCurrentlyTasking()) {
            if (this.currentTask.getPriority() < task.getPriority()) {
                this.currentTask.setOnline(false);
                this.currentTask = task;
                this.currentTask.setOnline(true);
                return true;
            }
            return false;
        }
        if (!isCurrentlyTasking()) {
            this.currentTask = task;
            this.currentTask.setOnline(true);
        }
        return true;
    }

    public void finishTask(T task) {
        if (this.currentTask == task) {
            this.currentTask.setOnline(false);
            this.currentTask = null;
        }
    }

    public T getTask(String taskName) {
        BasicTask basicTask = null;
        for (BasicTask basicTask1 : getTasks()) {
            if (basicTask1.getName().equalsIgnoreCase(taskName)) {
                basicTask = basicTask1;
                break;
            }
        }
        return (T) basicTask;
    }

    public boolean comparePriority(T task) {
        if (task.getPriority() < this.currentTask.getPriority())
            return false;
        return true;
    }

    public boolean isCurrentlyTasking() {
        return Objects.nonNull(this.currentTask);
    }
}

