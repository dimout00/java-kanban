package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    abstract void setUp();

    @Test
    void shouldCalculateEpicStatusCorrectly() {
        Epic epic = taskManager.createEpic(new Epic(0, "Epic", "Description"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask(0, "Subtask 1", "Desc", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask(0, "Subtask 2", "Desc", TaskStatus.NEW, epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1)));

        assertEquals(TaskStatus.NEW, epic.getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus());

        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldDetectTimeOverlaps() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = taskManager.createTask(new Task(0, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now));

        Task overlappingTask = new Task(0, "Task 2", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now.plusMinutes(30));

        assertThrows(IllegalStateException.class, () -> {
            taskManager.createTask(overlappingTask);
        });

        Task nonOverlappingTask = new Task(0, "Task 3", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now.plusHours(2));

        assertDoesNotThrow(() -> {
            taskManager.createTask(nonOverlappingTask);
        });
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = taskManager.createTask(new Task(0, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(2), now.plusHours(2)));
        Task task2 = taskManager.createTask(new Task(0, "Task 2", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now));
        Task task3 = taskManager.createTask(new Task(0, "Task 3", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now.plusHours(1)));

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(3, prioritized.size());
        assertEquals(task2, prioritized.get(0)); // Самая ранняя
        assertEquals(task3, prioritized.get(1));
        assertEquals(task1, prioritized.get(2)); // Самая поздняя
    }


    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        Task task = taskManager.createTask(new Task(0, "Task", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now()));

        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());
        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void shouldRemoveFromHistory() {
        Task task1 = taskManager.createTask(new Task(0, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now()));
        Task task2 = taskManager.createTask(new Task(0, "Task 2", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(2)));
        Task task3 = taskManager.createTask(new Task(0, "Task 3", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(4)));

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());

        taskManager.deleteTask(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task1));

        taskManager.deleteTask(task2.getId());
        history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertFalse(history.contains(task2));
        
        taskManager.deleteTask(task3.getId());
        history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }
}