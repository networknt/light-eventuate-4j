package com.networknt.eventuate.test.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class EventTracker<T> {

  private ReplaySubject<T> events = ReplaySubject.create();
  private Logger logger = LoggerFactory.getLogger(getClass());

  public static <T> EventTracker<T> create() {
    return new EventTracker<>();
  }

  public synchronized void onNext(T item) {
    events.onNext(item);
  }

  public T eventuallyContains(Predicate<T> pred) {
    try {
      return events.timeout(30, TimeUnit.SECONDS)
              .onErrorResumeNext(t -> Observable.error(new RuntimeException("Presumably first timeout failed", t)))
              .filter(pred::test)
              .take(1)
              .timeout(720, TimeUnit.SECONDS).toBlocking().first();
    } catch (Throwable t) {
      logger.error("Failure", t);
      throw new RuntimeException(t);
    }
  }

  public ReplaySubject<T> getEvents() {
    return events;
  }
}
