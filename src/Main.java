import util.TaskStatus;

import model.Epic;
import model.Task;
import model.Subtask;
import manager.TaskManager;
import manager.Managers;


public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = manager.createTask(new Task(0, "Task 1", "Description 1", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task(0, "Task 2", "Description 2", TaskStatus.NEW));

        // Создаем эпик с двумя подзадачами
        Epic epic1 = manager.createEpic(new Epic(0, "Epic 1", "Epic description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask(0, "Subtask 1", "Sub desc 1", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask(0, "Subtask 2", "Sub desc 2", TaskStatus.NEW, epic1.getId()));

        // Создаем эпик с одной подзадачей
        Epic epic2 = manager.createEpic(new Epic(0, "Epic 2", "Epic description 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask(0, "Subtask 3", "Sub desc 3", TaskStatus.NEW, epic2.getId()));

        // Выводим списки
        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nВсе эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Меняем статусы
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);

        // Выводим обновленные списки
        System.out.println("\nПосле изменения статусов:");
        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nПодзадачи эпика 1:");
        for (Subtask subtask : manager.getEpicSubtasks(epic1.getId())) {
            System.out.println(subtask);
        }

        // Получаем подзадачи эпика
        System.out.println("\nПодзадачи эпика 1:");
        for (Subtask subtask : manager.getEpicSubtasks(epic1.getId())) {
            System.out.println(subtask);
        }

        // Удаляем задачу и эпик
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic2.getId());

        // Выводим финальные списки
        System.out.println("\nПосле удаления:");
        System.out.println("Оставшиеся задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
    }
}