package manager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task1 = new Task(1, "Task 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(2, "Task 2", "Desc 2", TaskStatus.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldNotHaveSizeLimit() {
        // Добавляем 15 задач
        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Desc", TaskStatus.NEW,
                    Duration.ofMinutes(30), LocalDateTime.now().plusHours(i));
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(15, history.size(), "История должна содержать все 15 задач");
        assertEquals(1, history.get(0).getId(), "Первая задача должна быть с ID=1");
        assertEquals(15, history.get(14).getId(), "Последняя задача должна быть с ID=15");
    }

    @Test
    void shouldHandleDuplicateAdds() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликаты");
    }
}