package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    @Test
    void shouldCreateTask() throws TaskValidationException {
        Task task = new Task(0, "Test task", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask.getId(), "Задача должна получить ID");
        assertEquals(task.getName(), createdTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), createdTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(task.getStatus(), createdTask.getStatus(), "Статус задачи не совпадает");
    }

    @Test
    void shouldGetTaskById() throws TaskValidationException {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now()));
        Optional<Task> retrievedTask = taskManager.getTask(task.getId());

        assertTrue(retrievedTask.isPresent(), "Задача должна быть найдена");
        assertEquals(task, retrievedTask.get(), "Полученная задача не совпадает с созданной");
    }

    @Test
    void shouldUpdateTask() throws TaskValidationException {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now()));
        Task updatedTask = new Task(task.getId(), "Updated", "Updated description", TaskStatus.DONE,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        taskManager.updateTask(updatedTask);
        Optional<Task> savedTask = taskManager.getTask(task.getId());

        assertTrue(savedTask.isPresent(), "Задача должна быть найдена");
        assertEquals(updatedTask.getName(), savedTask.get().getName(), "Название не обновилось");
        assertEquals(updatedTask.getDescription(), savedTask.get().getDescription(), "Описание не обновилось");
        assertEquals(updatedTask.getStatus(), savedTask.get().getStatus(), "Статус не обновился");
    }

    @Test
    void shouldThrowExceptionForOverlappingTasks() throws TaskValidationException {
        Task task1 = taskManager.createTask(new Task(0, "Task 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now()));

        // Попытка создать задачу, которая пересекается по времени
        Task overlappingTask = new Task(0, "Task 2", "Description 2", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusMinutes(30));

        assertThrows(TaskValidationException.class, () -> {
            taskManager.createTask(overlappingTask);
        }, "Должно быть выброшено исключение при пересечении задач");
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