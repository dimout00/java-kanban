package manager;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {

    private final LinkedList<Task> history = new LinkedList<>();
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private final Node head;
    private final Node tail;

    public InMemoryHistoryManager() {
        head = new Node(null, null, null);
        tail = new Node(null, null, null);

        head.next = tail;
        tail.prev = head;
    }

    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Node next, Task task, Node prev) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        int id = task.getId();

        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
        }
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head.next;

        while (current != tail) {
            result.add(current.task);
            current = current.next;
        }

        return result;
    }

    private void linkLast(Task task) {

        Node newNode = new Node(tail.prev, task, tail);

        tail.prev.next = newNode;
        tail.prev = newNode;

        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) return;

        node.prev.next = node.next;
        node.next.prev = node.prev;

        historyMap.remove(node.task.getId());
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
        }
    }
}