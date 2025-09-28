package manager;

import com.sun.net.httpserver.HttpExchange;
import model.Task;

import java.io.IOException;
import java.util.Optional;

class TaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handleGetTasks(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            if (pathParts.length == 2) {
                // GET /tasks
                String response = gson.toJson(taskManager.getAllTasks());
                sendText(exchange, response);
            } else if (pathParts.length == 3) {
                // GET /tasks/{id}
                int id = Integer.parseInt(pathParts[2]);
                Optional<Task> task = taskManager.getTask(id);
                if (task.isPresent()) {
                    String response = gson.toJson(task.get());
                    sendText(exchange, response);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    public void handlePostTask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        System.out.println("Received body: " + body); // Для отладки

        try {
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendBadRequest(exchange, "Неверный формат данных задачи");
                return;
            }

            if (task.getId() == 0) {
                // Создание новой задачи
                Task createdTask = taskManager.createTask(task);
                String response = gson.toJson(createdTask);
                sendText(exchange, response, 201);
            } else {
                // Обновление существующей задачи
                taskManager.updateTask(task);
                sendSuccess(exchange);
            }
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Для отладки
            sendBadRequest(exchange, "Неверный формат данных: " + e.getMessage());
        }
    }

    public void handleDeleteTask(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            if (pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteTask(id);
                sendSuccess(exchange);
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}