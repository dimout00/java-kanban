package manager;

import com.sun.net.httpserver.HttpExchange;
import model.Subtask;

import java.io.IOException;
import java.util.Optional;

class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handleGetSubtasks(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            // GET /subtasks
            String response = gson.toJson(taskManager.getAllSubtasks());
            sendText(exchange, response);
        } else if (pathParts.length == 3) {
            // GET /subtasks/{id}
            try {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Subtask> subtask = taskManager.getSubtask(id);
                if (subtask.isPresent()) {
                    String response = gson.toJson(subtask.get());
                    sendText(exchange, response);
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID");
            }
        } else {
            sendNotFound(exchange);
        }
    }

    public void handlePostSubtask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                // Создание новой подзадачи
                Subtask createdSubtask = taskManager.createSubtask(subtask);
                String response = gson.toJson(createdSubtask);
                sendText(exchange, response, 201);
            } else {
                // Обновление существующей подзадачи
                taskManager.updateSubtask(subtask);
                sendCreated(exchange);
            }
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат данных");
        }
    }

    public void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteSubtask(id);
                sendSuccess(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}