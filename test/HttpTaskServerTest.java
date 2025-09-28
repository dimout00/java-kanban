import com.google.gson.Gson;
import manager.HttpTaskServer;
import manager.Managers;
import manager.TaskManager;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerTest {
    private static final String BASE_URL = "http://localhost:8080";
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        gson = Managers.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    //Методы для улучшения читаемости тестов
    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testCreateAndGetTask() throws IOException, InterruptedException {
        // Создаем задачу с простыми данными для тестирования
        Task task = new Task(0, "Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0));
        String taskJson = gson.toJson(task);

        System.out.println("Sending JSON: " + taskJson);

        // Создание задачи
        HttpResponse<String> response = sendPost("/tasks", taskJson);
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        assertEquals(201, response.statusCode(), "Expected 201 Created but got " + response.statusCode());

        // Получаем список задач
        HttpResponse<String> getResponse = sendGet("/tasks");
        assertEquals(200, getResponse.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    void testServerIsRunning() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/tasks");
        assertEquals(200, response.statusCode());
    }
}