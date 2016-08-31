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

import com.pr.gradle.util.PortUtils;

public class JavaExecForkServerTest {
  static String[] receivedArgs;
  static AtomicInteger receivedPings;
  static CountDownLatch serverStarted;
  static CountDownLatch serverStopped;

  Integer controlPort = PortUtils.findOpenPort();
  ExecutorService executor = Executors.newSingleThreadExecutor();
  Integer exitCode;
  
  @Before
  public void setup() {
    receivedArgs = null;
    receivedPings = new AtomicInteger(0);
    serverStarted = new CountDownLatch(1);
    serverStopped = new CountDownLatch(1);
    JavaExecForkServer.EXIT = status ->  {
      serverStopped.countDown();
      if (exitCode == null) // only care about the first call to System.exit()
        exitCode = status;
    };
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

    assertThat(serverStopped.getCount(), equalTo(0L));
    assertThat(exitCode, equalTo(0));
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
    serverStopped.await(10, TimeUnit.SECONDS);

    assertThat(serverStopped.getCount(), equalTo(0L));
    assertThat(exitCode, equalTo(0));
  }

  @Test
  public void shouldRunMainClassThatIgnoresInterrupts() throws Exception {
    assertThat(receivedPings.get(), equalTo(0));
    startMainClass(StubMainClassIgnoresInterrupts.class);

    serverStarted.await(1, TimeUnit.SECONDS);
    assertThat(receivedArgs, arrayWithSize(2));
    assertThat(receivedArgs[0], equalTo("arg1"));
    assertThat(receivedArgs[1], equalTo("arg2"));
    
    Thread.sleep(1000);
    assertThat(receivedPings.get(), greaterThanOrEqualTo(10));

    new Client(controlPort).sendStopCommand();
    serverStopped.await(10, TimeUnit.SECONDS);

    assertThat(serverStopped.getCount(), equalTo(0L));
    assertThat(exitCode, equalTo(-1));
  }
  
  public static class StubMainClass {
    public static void main(String[] args) throws InterruptedException {
      receivedArgs = args;
      serverStarted.countDown();

      while(true) {
        receivedPings.incrementAndGet();
        System.out.println("ping");
        Thread.sleep(100);
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
  
  public static class StubMainClassIgnoresInterrupts {
    public static void main(String[] args) {
      receivedArgs = args;
      serverStarted.countDown();

      while(true) {
        receivedPings.incrementAndGet();
        System.out.println("ping");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {}
      }
    }
  }
  
  private void startMainClass(Class<?> mainClass) {
    executor.execute(() -> {
      try {
        JavaExecForkServer.main(new String[]{ mainClass.getName(), controlPort.toString(), "arg1", "arg2" });
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
  
  @After
  public void cleanup() throws InterruptedException {
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
  }
}