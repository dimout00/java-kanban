package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
    protected int nextId = 1;
    protected HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return nextId++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.keySet().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(prioritizedTasks::remove);
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteAllEpics() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(prioritizedTasks::remove);
        epics.keySet().forEach(historyManager::remove);

        subtasks.clear();
        epics.clear();
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return Optional.of(subtask);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return Optional.of(epic);
        }
        return Optional.empty();
    }

    @Override
    public Task createTask(Task task) {
        try {
            validateNoTimeOverlap(task);
        } catch (TaskValidationException e) {
            throw new RuntimeException("Ошибка валидации задачи", e);
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }

        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        try {
            validateNoTimeOverlap(subtask);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка валидации подзадачи", e);
        }
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic.getId());
        }

        return subtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        try {
            validateNoTimeOverlap(task);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка валидации задачи", e);
        }
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask);

            tasks.put(task.getId(), task);

            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        try {
            validateNoTimeOverlap(subtask);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка валидации подзадачи", e);
        }
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            prioritizedTasks.remove(oldSubtask);

            subtasks.put(subtask.getId(), subtask);

            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }

            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic savedEpic = epics.get(epic.getId());
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);

            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);

                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
            }
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        return epic.getSubtaskIds().stream().map(subtasks::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private boolean hasTimeOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null || task1.getEndTime() == null || task2.getEndTime() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private void validateNoTimeOverlap(Task newTask) throws TaskValidationException {
        if (newTask.getStartTime() == null) {
            return;
        }

        boolean hasOverlap = prioritizedTasks.stream().filter(task -> !task.equals(newTask)).anyMatch(existingTask -> hasTimeOverlap(newTask, existingTask));

        if (hasOverlap) {
            throw new TaskValidationException("Задача пересекается по времени с существующей задачей");
        }
    }
}