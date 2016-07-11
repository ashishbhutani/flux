/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.examples.decision;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.Event;

/**
 * Represents a given user's current stats
 */
public class UserData implements Event {

    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    private UserId userId;

    /* For Jackson & cglib */
    UserData() {
    }

    public UserData(String email, String name, UserId userId) {
        this.email = email;
        this.name = name;
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }
}
