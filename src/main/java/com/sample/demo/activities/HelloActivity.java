package com.sample.demo.activities;

import com.sample.demo.model.entity.Person;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface HelloActivity {
    String sayHello(Person person);
}
