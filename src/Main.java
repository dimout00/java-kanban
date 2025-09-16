import util.TaskStatus;
import model.Epic;
import model.Task;
import model.Subtask;
import manager.TaskManager;
import manager.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(new Task(0, "Task 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now()));
        Task task2 = manager.createTask(new Task(0, "Task 2", "Description 2", TaskStatus.NEW,
                Duration.ofHours(2), LocalDateTime.now().plusHours(3)));

        Epic epic1 = manager.createEpic(new Epic(0, "Epic 1", "Epic description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask(0, "Subtask 1", "Sub desc 1", TaskStatus.NEW,
                epic1.getId(), Duration.ofMinutes(30), LocalDateTime.now().plusHours(5)));
        Subtask subtask2 = manager.createSubtask(new Subtask(0, "Subtask 2", "Sub desc 2", TaskStatus.NEW,
                epic1.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusHours(6)));

        System.out.println("Приоритетный список задач:");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task.getName() + " - " + task.getStartTime());
        }

        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());

        System.out.println("\nИстория:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getName());
        }
    }
}