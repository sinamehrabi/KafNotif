package com.kafnotif.notifier;

import com.kafnotif.model.Event;

public interface Notifier {
    void send(Event event);
}
