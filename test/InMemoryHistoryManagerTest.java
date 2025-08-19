package manager;

import model.Task;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void shouldAddTasksToHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task(1, "Task 1", "Desc 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Desc 2", TaskStatus.IN_PROGRESS);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldKeepOnlyLast10Tasks() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        // Добавляем 15 задач
        for (int i = 1; i <= 15; i++) {
            historyManager.add(new Task(i, "Task " + i, "Desc", TaskStatus.NEW));
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(6, history.get(0).getId(), "Первая задача должна быть с ID=6");
        assertEquals(15, history.get(9).getId(), "Последняя задача должна быть с ID=15");
    }

    @Test
    void shouldHandleDuplicateAdds() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликаты");
    }
}