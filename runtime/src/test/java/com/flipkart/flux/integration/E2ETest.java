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
 *
 */

package com.flipkart.flux.integration;

import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.initializer.OrderedComponentBooter;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
public class E2ETest {

    @Inject
    StateMachinesDAO stateMachinesDAO;

    @Inject
    EventsDAO eventsDAO;

    @Rule
    @Inject
    public DbClearRule dbClearRule;

    @Inject
    SimpleWorkflow simpleWorkflow;

    @Inject
    OrderedComponentBooter orderedComponentBooter;

    @Test
    public void testSimpleWorkflowE2E() throws Exception {
        /* Invocation */
        simpleWorkflow.simpleDummyWorkflow(new StringEvent("startingEvent"));
        
        // sleep for a while to let things complete and then eval results and shutdown
        Thread.sleep(2000L);

        /* Asserts*/
        final Set<StateMachine> smInDb = stateMachinesDAO.findByNameAndVersion("com.flipkart.flux.integration.SimpleWorkflow_simpleDummyWorkflow_void_com.flipkart.flux.integration.StringEvent", 1l);
        final Long smId = smInDb.stream().findFirst().get().getId();
        assertThat(smInDb).hasSize(1);
        assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(3);
        /* The following will return true only if the latch has been counted down as part of execution in another thread */
        assertThat(simpleWorkflow.getCountDownLatchForSimpleIntegerReturnTask().await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(simpleWorkflow.getCountDownLatchForSimpleStringReturnTask().await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(simpleWorkflow.getCountDownLatchForSimpleIntegerAndStringTask().await(1, TimeUnit.SECONDS)).isTrue();

        /* See if Akka Test suite can be used to verify akka interactions, not needed though */
    }
}
