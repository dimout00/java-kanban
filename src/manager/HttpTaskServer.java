package manager;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики
        server.createContext("/tasks", this::handleTasks);
        server.createContext("/subtasks", this::handleSubtasks);
        server.createContext("/epics", this::handleEpics);
        server.createContext("/history", this::handleHistory);
        server.createContext("/prioritized", this::handlePrioritized);
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    private void handleTasks(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        TaskHandler handler = new TaskHandler(taskManager);
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handler.handleGetTasks(exchange);
                    break;
                case "POST":
                    handler.handlePostTask(exchange);
                    break;
                case "DELETE":
                    handler.handleDeleteTask(exchange);
                    break;
                default:
                    handler.sendNotFound(exchange);
            }
        } catch (Exception e) {
            handler.sendInternalError(exchange);
        }
    }

    private void handleSubtasks(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        SubtaskHandler handler = new SubtaskHandler(taskManager);
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handler.handleGetSubtasks(exchange);
                    break;
                case "POST":
                    handler.handlePostSubtask(exchange);
                    break;
                case "DELETE":
                    handler.handleDeleteSubtask(exchange);
                    break;
                default:
                    handler.sendNotFound(exchange);
            }
        } catch (Exception e) {
            handler.sendInternalError(exchange);
        }
    }

    private void handleEpics(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        EpicHandler handler = new EpicHandler(taskManager);
        try {
            String path = exchange.getRequestURI().getPath();

            if (path.matches("/epics/\\d+/subtasks")) {
                handler.handleGetEpicSubtasks(exchange);
                return;
            }

            switch (exchange.getRequestMethod()) {
                case "GET":
                    handler.handleGetEpics(exchange);
                    break;
                case "POST":
                    handler.handlePostEpic(exchange);
                    break;
                case "DELETE":
                    handler.handleDeleteEpic(exchange);
                    break;
                default:
                    handler.sendNotFound(exchange);
            }
        } catch (Exception e) {
            handler.sendInternalError(exchange);
        }
    }

    private void handleHistory(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        HistoryHandler handler = new HistoryHandler(taskManager);
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handler.handleGetHistory(exchange);
            } else {
                handler.sendNotFound(exchange);
            }
        } catch (Exception e) {
            handler.sendInternalError(exchange);
        }
    }

    private void handlePrioritized(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        PrioritizedHandler handler = new PrioritizedHandler(taskManager);
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handler.handleGetPrioritized(exchange);
            } else {
                handler.sendNotFound(exchange);
            }
        } catch (Exception e) {
            handler.sendInternalError(exchange);
        }
    }
}