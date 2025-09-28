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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
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

    @Test
    void testCreateAndGetTask() throws IOException, InterruptedException {
        // Создаем задачу с простыми данными для тестирования
        Task task = new Task(0, "Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0));
        String taskJson = gson.toJson(task);

        System.out.println("Sending JSON: " + taskJson); // Для отладки

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response status: " + response.statusCode()); // Для отладки
        System.out.println("Response body: " + response.body()); // Для отладки

        assertEquals(201, response.statusCode(), "Expected 201 Created but got " + response.statusCode());

        // Получаем список задач
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    // Добавляем простой тест для проверки что сервер вообще работает
    @Test
    void testServerIsRunning() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }
}