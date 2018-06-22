/*
 * Copyright 2015-2018 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.desktoppanefx.scene.layout;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class InternalWindowEvent extends Event {
    public static final EventType<InternalWindowEvent> EVENT_CLOSED = new EventType<>(ANY, "EVENT_CLOSED");
    public static final EventType<InternalWindowEvent> EVENT_MINIMIZED = new EventType<>(ANY, "EVENT_MINIMIZED");
    public static final EventType<InternalWindowEvent> EVENT_DETACHED = new EventType<>(ANY, "EVENT_DETACHED");
    public static final EventType<InternalWindowEvent> EVENT_ATTACHED = new EventType<>(ANY, "EVENT_ATTACHED");

    private final InternalWindow internalWindow;

    public InternalWindowEvent(InternalWindow internalWindow, EventType<? extends Event> eventType) {
        super(eventType);
        this.internalWindow = internalWindow;
    }

    public InternalWindow getInternalWindow() {
        return internalWindow;
    }
}
