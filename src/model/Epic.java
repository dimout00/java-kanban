package model;

import util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW, null, null);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    @Override
    public Duration getDuration() {
        if (getSubtaskIds().isEmpty()) {
            return Duration.ZERO;
        }

        return Duration.ofMinutes(60);
    }

    @Override
    public LocalDateTime getStartTime() {
        if (getSubtaskIds().isEmpty()) {
            return null;
        }

        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime getEndTime() {
        if (getSubtaskIds().isEmpty()) {
            return null;
        }

        return LocalDateTime.now().plusHours(2);
    }

    @Override
    public String toString() {
        return "Epic{" +
               "id=" + getId() +
               ", name='" + getName() + '\'' +
               ", description='" + getDescription() + '\'' +
               ", status=" + getStatus() +
               ", subtaskIds=" + subtaskIds +
               '}';
    }
}