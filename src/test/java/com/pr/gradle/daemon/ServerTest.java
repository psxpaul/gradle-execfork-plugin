package com.pr.gradle.daemon;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {
  static String[] receivedArgs;
  static AtomicInteger receivedPings;
  static CountDownLatch serverStarted;
  static CountDownLatch serverStopped;

  Integer controlPort = Server.findOpenPort();
  ExecutorService executor = Executors.newSingleThreadExecutor();
  
  @Before
  public void setup() {
    receivedArgs = null;
    receivedPings = new AtomicInteger(0);
    serverStarted = new CountDownLatch(1);
    serverStopped = new CountDownLatch(1);
  }

  @Test
  public void shouldRunMainClassAsDaemon() throws Exception {
    assertThat(receivedPings.get(), equalTo(0));
    startMainClass(StubMainClass.class);

    serverStarted.await(5, TimeUnit.SECONDS);
    assertThat(receivedArgs, arrayWithSize(2));
    assertThat(receivedArgs[0], equalTo("arg1"));
    assertThat(receivedArgs[1], equalTo("arg2"));
    
    Thread.sleep(1000);
    assertThat(receivedPings.get(), greaterThanOrEqualTo(10));

    new Client(controlPort).sendStopCommand();
    serverStopped.await(5, TimeUnit.SECONDS);
  }

  @Test
  public void shouldRunMainClassThatReturnsEarly() throws Exception {
    assertThat(receivedPings.get(), equalTo(0));
    startMainClass(StubMainClassFinishesEarly.class);

    serverStarted.await(1, TimeUnit.SECONDS);
    assertThat(receivedArgs, arrayWithSize(2));
    assertThat(receivedArgs[0], equalTo("arg1"));
    assertThat(receivedArgs[1], equalTo("arg2"));
    
    Thread.sleep(50);
    assertThat(receivedPings.get(), greaterThanOrEqualTo(10));
    serverStopped.await(5, TimeUnit.SECONDS);
  }
  
  public static class StubMainClass {
    public static void main(String[] args) {
      receivedArgs = args;
      serverStarted.countDown();

      try {
        while(true) {
          receivedPings.incrementAndGet();
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
        serverStopped.countDown();
      }
    }
  }
  
  public static class StubMainClassFinishesEarly {
    public static void main(String[] args) {
      receivedArgs = args;
      serverStarted.countDown();
      receivedPings.set(10);
      serverStopped.countDown();
    }
  }
  
  private void startMainClass(Class<?> mainClass) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Server.main(new String[]{ controlPort.toString(), mainClass.getName(), "arg1", "arg2" });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
  
  @After
  public void cleanup() throws InterruptedException {
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
  }
}