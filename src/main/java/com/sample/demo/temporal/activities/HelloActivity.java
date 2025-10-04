package com.sample.demo.temporal.activities;

import com.sample.demo.temporal.model.Person;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface HelloActivity {
    String sayHello(Person person);
}
