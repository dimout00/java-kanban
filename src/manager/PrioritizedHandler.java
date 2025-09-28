package manager;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handleGetPrioritized(HttpExchange exchange) throws IOException {
        String response = gson.toJson(taskManager.getPrioritizedTasks());
        sendText(exchange, response);
    }
}