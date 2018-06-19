/*
 * Copyright 2015-2018 Andres Almiray
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
import javafx.scene.image.ImageView;

/**
 * @author Lincoln Minto
 */
public class MDIEvent extends Event {

    private static final long serialVersionUID = 2249682426993045124L;
    public static final EventType<MDIEvent> EVENT_CLOSED = new EventType<>(ANY, "EVENT_CLOSED");
    public static final EventType<MDIEvent> EVENT_MINIMIZED = new EventType<>(ANY, "EVENT_MINIMIZED");
    public ImageView imgLogo;

    public MDIEvent(ImageView logoImage, EventType<? extends Event> eventType) {
        super(eventType);
        imgLogo = logoImage;
    }

    public MDIEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
