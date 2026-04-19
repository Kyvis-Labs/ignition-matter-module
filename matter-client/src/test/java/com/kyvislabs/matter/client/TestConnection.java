package com.kyvislabs.matter.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kyvislabs.matter.client.model.EventType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TestConnection {
    static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: TestConnection <host:port>");
            System.exit(1);
        }
        MatterClient client = new MatterClient("ws://" + args[0] + "/ws");
        client.connect();
        client.startListening();

        // Print initial door state
        Object initial = client.getNode(1).getAttribute("2/69/0");
        System.out.println(time() + " Connected. Door is currently: " + (Boolean.TRUE.equals(initial) ? "CLOSED" : "OPEN"));
        System.out.println(time() + " Watching for changes... (Ctrl+C to stop)\n");

        client.addEventListener((eventType, data) -> {
            if (eventType == EventType.ATTRIBUTE_UPDATED && data != null && data.isJsonArray()) {
                JsonArray arr = data.getAsJsonArray();
                if (arr.size() >= 3) {
                    int nodeId = arr.get(0).getAsInt();
                    String attrPath = arr.get(1).getAsString();
                    JsonElement value = arr.get(2);

                    // Door sensor: BooleanState cluster (69), attr 0 on endpoint 2
                    if (attrPath.equals("2/69/0")) {
                        boolean closed = value.getAsBoolean();
                        System.out.println(time() + " Door " + (closed ? "CLOSED" : "OPENED") + "!");
                    }

                    // Also show battery changes
                    if (attrPath.equals("2/47/12")) {
                        int pct = value.getAsInt() / 2;
                        System.out.println(time() + " Battery: " + pct + "%");
                    }
                }
            } else if (eventType == EventType.NODE_EVENT) {
                System.out.println(time() + " Node event: " + data);
            }
        });

        // Keep running until Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n" + time() + " Disconnecting...");
            client.close();
        }));

        Thread.currentThread().join();
    }

    static String time() {
        return "[" + LocalTime.now().format(TIME_FMT) + "]";
    }
}
