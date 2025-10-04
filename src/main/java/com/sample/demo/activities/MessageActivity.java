package com.sample.demo.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface MessageActivity {
    void sendMessage(String message);
}
