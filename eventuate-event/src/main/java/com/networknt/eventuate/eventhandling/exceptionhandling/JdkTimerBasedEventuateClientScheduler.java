package com.networknt.eventuate.eventhandling.exceptionhandling;

import com.networknt.eventuate.eventhandling.exceptionhandling.EventuateClientScheduler;

import java.util.Timer;
import java.util.TimerTask;

public class JdkTimerBasedEventuateClientScheduler implements EventuateClientScheduler {

  private Timer timer = new Timer();

  @Override
  public void setTimer(long delayInMilliseconds, Runnable callback) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        callback.run();
      }
    }, delayInMilliseconds);
  }
}
