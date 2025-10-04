package com.sample.demo.activities;

import com.sample.demo.model.entity.Person;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "demoqueue")
public class HelloActivityImpl implements HelloActivity {
    @Override
    public String sayHello(Person person) {
        return "Hello " + person.getFirstName() + " " + person.getLastName();
    }
}
