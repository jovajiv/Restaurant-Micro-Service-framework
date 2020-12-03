package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class BookExists implements Event {
    private String name;

    public BookExists (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
