package com.sample.demo.workflows;

import com.sample.demo.activities.HelloActivity;
import com.sample.demo.model.entity.Person;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = "demoqueue")
public class HelloWorkflowImpl implements HelloWorkflow {
    @Override
    public String sayHello(Person person) {
        HelloActivity activity = Workflow.newActivityStub(HelloActivity.class, ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(2))
                .build());
        Workflow.sleep(Duration.ofSeconds(2));
        return activity.sayHello(person);
    }
}
