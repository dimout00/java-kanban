package manager;

import com.sun.net.httpserver.HttpExchange;
import model.Epic;

import java.io.IOException;
import java.util.Optional;

class EpicHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handleGetEpics(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            // GET /epics
            String response = gson.toJson(taskManager.getAllEpics());
            sendText(exchange, response);
        } else if (pathParts.length == 3) {
            // GET /epics/{id}
            try {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Epic> epic = taskManager.getEpic(id);
                if (epic.isPresent()) {
                    String response = gson.toJson(epic.get());
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

    public void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
            try {
                int epicId = Integer.parseInt(pathParts[2]);
                String response = gson.toJson(taskManager.getEpicSubtasks(epicId));
                sendText(exchange, response);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID эпика");
            }
        } else {
            sendNotFound(exchange);
        }
    }

    public void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                // Создание нового эпика
                Epic createdEpic = taskManager.createEpic(epic);
                String response = gson.toJson(createdEpic);
                sendText(exchange, response, 201);
            } else {
                // Обновление существующего эпика
                taskManager.updateEpic(epic);
                sendCreated(exchange);
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат данных");
        }
    }

    public void handleDeleteEpic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteEpic(id);
                sendSuccess(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}