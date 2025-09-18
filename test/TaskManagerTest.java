package manager;

import model.Epic;
import model.Subtask;
import model.Task;
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
        Task task = new Task(0, "Test task", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task createdTask = taskManager.createTask(task);
        Optional<Task> retrievedTask = taskManager.getTask(createdTask.getId());

        assertTrue(retrievedTask.isPresent(), "Задача должна быть найдена");
        assertEquals(createdTask, retrievedTask.get(), "Полученная задача не совпадает с созданной");
    }

    @Test
    void shouldUpdateTask() throws TaskValidationException {
        Task task = new Task(0, "Test task", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task createdTask = taskManager.createTask(task);

        Task updatedTask = new Task(createdTask.getId(), "Updated", "Updated description", TaskStatus.DONE,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        taskManager.updateTask(updatedTask);
        Optional<Task> savedTask = taskManager.getTask(createdTask.getId());

        assertTrue(savedTask.isPresent(), "Задача должна быть найдена");
        assertEquals(updatedTask.getName(), savedTask.get().getName(), "Название не обновилось");
        assertEquals(updatedTask.getDescription(), savedTask.get().getDescription(), "Описание не обновилось");
        assertEquals(updatedTask.getStatus(), savedTask.get().getStatus(), "Статус не обновился");
    }

    @Test
    void shouldThrowExceptionForOverlappingTasks() throws TaskValidationException {
        Task task1 = new Task(0, "Task 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.createTask(task1);

        // Попытка создать задачу, которая пересекается по времени
        Task overlappingTask = new Task(0, "Task 2", "Description 2", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusMinutes(30));

        assertThrows(TaskValidationException.class, () -> {
            taskManager.createTask(overlappingTask);
        }, "Должно быть выброшено исключение при пересечении задач");
    }

    @Test
    void shouldReturnPrioritizedTasks() throws TaskValidationException {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(0, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(2), now.plusHours(2));
        Task task2 = new Task(0, "Task 2", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now);
        Task task3 = new Task(0, "Task 3", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), now.plusHours(1));

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(3, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId()); // Самая ранняя
        assertEquals(task3.getId(), prioritized.get(1).getId());
        assertEquals(task1.getId(), prioritized.get(2).getId()); // Самая поздняя
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldNotDuplicateTasksInHistory() throws TaskValidationException {
        Task task = new Task(0, "Task", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task createdTask = taskManager.createTask(task);

        taskManager.getTask(createdTask.getId());
        taskManager.getTask(createdTask.getId());
        taskManager.getTask(createdTask.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void shouldRemoveFromHistory() throws TaskValidationException {
        Task task1 = new Task(0, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task(0, "Task 2", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        Task task3 = new Task(0, "Task 3", "Desc", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(4));

        Task createdTask1 = taskManager.createTask(task1);
        Task createdTask2 = taskManager.createTask(task2);
        Task createdTask3 = taskManager.createTask(task3);

        taskManager.getTask(createdTask1.getId());
        taskManager.getTask(createdTask2.getId());
        taskManager.getTask(createdTask3.getId());

        taskManager.deleteTask(createdTask1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.stream().anyMatch(t -> t.getId() == createdTask1.getId()));

        taskManager.deleteTask(createdTask2.getId());
        history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertFalse(history.stream().anyMatch(t -> t.getId() == createdTask2.getId()));

        taskManager.deleteTask(createdTask3.getId());
        history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldCreateEpicAndSubtask() throws TaskValidationException {
        Epic epic = new Epic(0, "Test Epic", "Test Epic Description");
        Epic createdEpic = taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Subtask Description",
                TaskStatus.NEW, createdEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdEpic.getId(), "Эпик должен получить ID");
        assertNotNull(createdSubtask.getId(), "Подзадача должна получить ID");
        assertEquals(createdEpic.getId(), createdSubtask.getEpicId(), "ID эпика должен совпадать в подзадаче");

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(createdEpic.getId());
        assertEquals(1, epicSubtasks.size(), "Эпик должен содержать одну подзадачу");
        assertEquals(createdSubtask.getId(), epicSubtasks.get(0).getId(), "Подзадача в эпике должна совпадать с созданной");
    }
}