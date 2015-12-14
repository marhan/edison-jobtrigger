package de.otto.edison.jobtrigger.trigger;

import com.google.common.collect.ImmutableList;
import de.otto.edison.jobtrigger.definition.JobDefinition;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

public class JobSchedulerTest {

    @Mock
    private ThreadPoolTaskScheduler scheduler;

    @InjectMocks
    private JobScheduler testee;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testShouldScheduleAllTriggers() {
        Runnable myRunnable = () -> {};
        Trigger myTrigger = mock(Trigger.class);
        JobDefinition definition = mock(JobDefinition.class);
        JobTrigger parameter = new JobTrigger(definition, myTrigger, myRunnable);

        testee.updateTriggers(ImmutableList.of(parameter, parameter));

        verify(scheduler, times(2)).schedule(myRunnable, myTrigger);
    }
}