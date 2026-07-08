package com.dungeon.game;

import java.util.*;

public class MessageLog {
    private LinkedList<String> messages = new LinkedList<>();
    private static final int MAX = 20;

    public void add(String msg) {
        messages.addFirst(msg);
        if (messages.size() > MAX) messages.removeLast();
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public String getLatest() {
        return messages.isEmpty() ? "" : messages.getFirst();
    }
}
