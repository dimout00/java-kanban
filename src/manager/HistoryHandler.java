package manager;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handleGetHistory(HttpExchange exchange) throws IOException {
        String response = gson.toJson(taskManager.getHistory());
        sendText(exchange, response);
    }
}