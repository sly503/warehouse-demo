package com.sample.demo.workflows;

import com.sample.demo.model.entity.Person;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    String sayHello(Person person);
}
