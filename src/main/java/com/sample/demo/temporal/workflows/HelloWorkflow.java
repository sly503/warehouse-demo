package com.sample.demo.temporal.workflows;

import com.sample.demo.temporal.model.Person;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    String sayHello(Person person);
}
