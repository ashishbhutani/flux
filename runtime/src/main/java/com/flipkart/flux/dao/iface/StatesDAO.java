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

package com.flipkart.flux.dao.iface;

import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.Status;

/**
 * <code>StatesDAO</code> interface provides methods to perform CR operations on {@link State}
 * @author shyam.akirala
 */
public interface StatesDAO {

    /** Creates a state in db and returns the saved object*/
    State create(State state);

    /** Updates a state in db */
    void updateState(State state);

    /** Updates status of a state*/
    public void updateStatus(Long stateId, Long stateMachineId, Status status);

    /** Updates rollback status of a state */
    public void updateRollbackStatus(Long stateId, Long stateMachineId, Status rollbackStatus);

    /** Increments the attempted no.of retries of a state by 1 */
    void incrementRetryCount(Long stateId, Long stateMachineId);

    /** Retrieves a state by it's unique identifier*/
    State findById(Long id);
}
