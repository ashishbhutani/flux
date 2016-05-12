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

package com.flipkart.flux.controller;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.exception.IllegalStateMachineException;
import com.flipkart.flux.impl.RAMContext;

import javax.inject.Inject;
import java.util.*;

/**
 * <code>WorkFlowExecutionController</code> controls the execution flow of a given state machine
 * @author shyam.akirala
 */
public class WorkFlowExecutionController {

    @Inject
    StateMachinesDAO stateMachinesDAO;

    @Inject
    EventsDAO eventsDAO;

    /**
     * Perform initAndStart operations on a state machine.
     * This can include creating the Context for the first time and storing it.
     * Trigger state machine execution.
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    public void initAndStart(StateMachine stateMachine) {
        Context context = new RAMContext(System.currentTimeMillis(), null); //TODO: set context id
        stateMachine.setContext(context);
        context.buildDependencyMap(stateMachine.getStates());

        Set<State> dependantStates = context.getDependantStates(null);
        //TODO: Start execution of dependantStates
    }

    /**
     * Retrieves the states which are dependant on this event and starts the execution of the states whose dependencies are met.
     * @param eventData
     * @param stateMachineInstanceId
     */
    public Set<State> postEvent(EventData eventData, Long stateMachineInstanceId) {

        //retrieve the state machine
        StateMachine stateMachine = stateMachinesDAO.findById(stateMachineInstanceId);
        if(stateMachine == null)
            throw new IllegalStateMachineException("State machine with id: "+stateMachineInstanceId+ " not found");

        //update event's data and status
        Event event = eventsDAO.findBySMIdAndName(stateMachineInstanceId, eventData.getName());
        if(event == null)
            throw new IllegalEventException("Event with stateMachineId: "+stateMachineInstanceId+", event name: "+ eventData.getName()+" not found");
        event.setStatus(Event.EventStatus.triggered);
        event.setEventData(eventData.getData());
        event.setEventSource(eventData.getEventSource());
        eventsDAO.update(event);

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null); //TODO: set context id
        stateMachine.setContext(context);
        context.buildDependencyMap(stateMachine.getStates());

        //get the states whose dependencies are met
        Set<State> executableStates = getExecutableStates(context.getDependantStates(eventData.getName()), stateMachineInstanceId);

        //start execution of the above states
        //TODO: Start execution of executableStates

        return executableStates;
    }

    /**
     * Given states which are dependant on a particular event, returns which of them can be executable (states whose all dependencies are met)
     * @param dependantStates
     * @param stateMachineInstanceId
     * @return executableStates
     */
    private Set<State> getExecutableStates(Set<State> dependantStates, Long stateMachineInstanceId) {

        Set<State> executableStates = new HashSet<State>();

        //received events of a particular state machine by system so far
        Set<String> receivedEvents = null;

//      for each state
//        1. get the dependencies (events)
//        2. check whether all events are in triggered state
//        3. if all events are in triggered status, then add this state to executableStates
        for(State state : dependantStates) {
            Set<String> dependantEvents = state.getDependencies();
            if(dependantEvents.size() == 1) { //If state is dependant on only one event then that would be the current event
                executableStates.add(state);
            } else {
                if (receivedEvents == null)
                    receivedEvents = new HashSet<>(eventsDAO.findTriggeredEventsNamesBySMId(stateMachineInstanceId));
                boolean areAllEventsReceived = true;
                for(String dependantEvent : dependantEvents) {
                    if(!receivedEvents.contains(dependantEvent)) {
                        areAllEventsReceived = false;
                        break;
                    }
                }
                if(areAllEventsReceived)
                    executableStates.add(state);
            }
        }

        return executableStates;
    }

}
