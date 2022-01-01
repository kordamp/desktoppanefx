/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2015-2022 The original authors
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
    public static final EventType<InternalWindowEvent> WINDOW_SHOWING = new EventType<>(ANY, "WINDOW_SHOWING");
    public static final EventType<InternalWindowEvent> WINDOW_SHOWN = new EventType<>(ANY, "WINDOW_SHOWN");
    public static final EventType<InternalWindowEvent> WINDOW_HIDING = new EventType<>(ANY, "WINDOW_HIDING");
    public static final EventType<InternalWindowEvent> WINDOW_HIDDEN = new EventType<>(ANY, "WINDOW_HIDDEN");
    public static final EventType<InternalWindowEvent> WINDOW_CLOSE_REQUEST = new EventType<>(ANY, "WINDOW_CLOSE_REQUEST");

    public static final EventType<InternalWindowEvent> WINDOW_MINIMIZING = new EventType<>(ANY, "WINDOW_MINIMIZING");
    public static final EventType<InternalWindowEvent> WINDOW_MINIMIZED = new EventType<>(ANY, "WINDOW_MINIMIZED");
    public static final EventType<InternalWindowEvent> WINDOW_MAXIMIZING = new EventType<>(ANY, "WINDOW_MAXIMIZING");
    public static final EventType<InternalWindowEvent> WINDOW_MAXIMIZED = new EventType<>(ANY, "WINDOW_MAXIMIZED");
    public static final EventType<InternalWindowEvent> WINDOW_RESTORING = new EventType<>(ANY, "WINDOW_RESTORING");
    public static final EventType<InternalWindowEvent> WINDOW_RESTORED = new EventType<>(ANY, "WINDOW_RESTORED");
    public static final EventType<InternalWindowEvent> WINDOW_DETACHING = new EventType<>(ANY, "WINDOW_DETACHING");
    public static final EventType<InternalWindowEvent> WINDOW_DETACHED = new EventType<>(ANY, "WINDOW_DETACHED");
    public static final EventType<InternalWindowEvent> WINDOW_ATTACHING = new EventType<>(ANY, "WINDOW_ATTACHING");
    public static final EventType<InternalWindowEvent> WINDOW_ATTACHED = new EventType<>(ANY, "WINDOW_ATTACHED");
    public static final EventType<InternalWindowEvent> WINDOW_ACTIVATED = new EventType<>(ANY, "WINDOW_ACTIVATED");
    public static final EventType<InternalWindowEvent> WINDOW_DEACTIVATED = new EventType<>(ANY, "WINDOW_DEACTIVATED");

    private final InternalWindow internalWindow;

    public InternalWindowEvent(InternalWindow internalWindow, EventType<? extends Event> eventType) {
        super(internalWindow, internalWindow, eventType);
        this.internalWindow = internalWindow;
    }

    public InternalWindow getInternalWindow() {
        return internalWindow;
    }
}
