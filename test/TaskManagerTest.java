package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.util.List;

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
    void shouldCreateTask() {
        Task task = new Task(0, "Test task", "Test description", TaskStatus.NEW);
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask.getId(), "Задача должна получить ID");
        assertEquals(task.getName(), createdTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), createdTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(task.getStatus(), createdTask.getStatus(), "Статус задачи не совпадает");
    }

    @Test
    void shouldGetTaskById() {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW));
        Task retrievedTask = taskManager.getTask(task.getId());

        assertEquals(task, retrievedTask, "Полученная задача не совпадает с созданной");
    }

    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW));
        Task updatedTask = new Task(task.getId(), "Updated", "Updated description", TaskStatus.DONE);

        taskManager.updateTask(updatedTask);
        Task savedTask = taskManager.getTask(task.getId());

        assertEquals(updatedTask.getName(), savedTask.getName(), "Название не обновилось");
        assertEquals(updatedTask.getDescription(), savedTask.getDescription(), "Описание не обновилось");
        assertEquals(updatedTask.getStatus(), savedTask.getStatus(), "Статус не обновился");
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW));
        taskManager.deleteTask(task.getId());

        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена");
    }

    @Test
    void shouldCreateEpic() {
        Epic epic = new Epic(0, "Test epic", "Test description");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId(), "Эпик должен получить ID");
        assertEquals(TaskStatus.NEW, createdEpic.getStatus(), "Статус нового эпика должен быть NEW");
    }

    @Test
    void shouldCreateSubtaskAndAddToEpic() {
        Epic epic = taskManager.createEpic(new Epic(0, "Test epic", "Test description"));
        Subtask subtask = new Subtask(0, "Test subtask", "Test description", TaskStatus.NEW, epic.getId());

        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask.getId(), "Подзадача должна получить ID");
        assertEquals(epic.getId(), createdSubtask.getEpicId(), "ID эпика в подзадаче не совпадает");

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());
        assertEquals(1, epicSubtasks.size(), "Эпик должен содержать подзадачу");
        assertEquals(createdSubtask, epicSubtasks.get(0), "Подзадача в эпике не совпадает");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtaskStatusChanged() {
        Epic epic = taskManager.createEpic(new Epic(0, "Test epic", "Test description"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask(0, "Test subtask", "Test description", TaskStatus.NEW, epic.getId())
        );

        // Меняем статус подзадачи на DONE
        Subtask updatedSubtask = new Subtask(
                subtask.getId(),
                subtask.getName(),
                subtask.getDescription(),
                TaskStatus.DONE,
                subtask.getEpicId()
        );
        taskManager.updateSubtask(updatedSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus(),
                "Статус эпика должен обновиться на DONE");
    }

    @Test
    void shouldAddTaskToHistoryWhenRetrieved() {
        Task task = taskManager.createTask(new Task(0, "Test task", "Test description", TaskStatus.NEW));

        // Получаем задачу, что должно добавить ее в историю
        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.get(0), "Задача в истории не совпадает");
    }

    @Test
    void shouldNotAddToHistoryWhenTaskNotFound() {
        taskManager.getTask(999); // Несуществующий ID

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой для несуществующей задачи");
    }
}