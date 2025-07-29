package manager;

import manager.*;
import model.*;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private final TaskManager manager = Managers.getDefault();

    @Test
    void addNewTask() {
        Task task = new Task(0, "Test task", "Test description", TaskStatus.NEW);
        manager.createTask(task);

        Task savedTask = manager.getTask(task.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void historyShouldSaveLast10Tasks() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Desc " + i, TaskStatus.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать 10 задач");
        assertEquals(6, history.get(0).getId(), "Первая задача в истории должна быть с ID=6");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic(0, "Epic", "Epic desc"));
        Subtask subtask1 = manager.createSubtask(new Subtask(0, "Sub 1", "Desc 1", TaskStatus.NEW, epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask(0, "Sub 2", "Desc 2", TaskStatus.NEW, epic.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE");
    }
}