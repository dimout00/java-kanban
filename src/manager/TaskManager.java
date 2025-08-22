package manager;

import model.Task;
import model.Subtask;
import model.Epic;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    Task createTask(Task task);

    Subtask createSubtask(Subtask subtask);

    Epic createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}