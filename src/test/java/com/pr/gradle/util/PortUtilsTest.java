package com.pr.gradle.util;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.gradle.api.GradleException;
import org.junit.Test;

public class PortUtilsTest {

  @Test
  public void testFindOpenPort() throws Exception {
    int port = PortUtils.findOpenPort();
    assertThat(port, greaterThanOrEqualTo(1024));
    assertThat(port, lessThanOrEqualTo(65535));
    
    try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
      fail("Socket should not have been in use already!");
    } catch (ConnectException e) {
      assertThat(e.getMessage(), containsString("Connection refused"));
    }
  }

  @Test(timeout=2000)
  public void testWaitForPortOpen_timeout() {
    Process stubProcess = new StubProcess();
    int port = PortUtils.findOpenPort();
    
    try {
      PortUtils.waitForPortOpen(port, 1, TimeUnit.SECONDS, stubProcess);
    } catch (Exception e) {
      assertThat(e, instanceOf(GradleException.class));
      assertThat(e.getMessage(), equalTo("Timed out waiting for port " + port + " to be opened"));
    }
  }

  @Test(timeout=2000)
  public void testWaitForPortOpen_processDied() throws Exception {
    StubProcess stubProcess = new StubProcess();
    stubProcess.alive = false;
    int port = PortUtils.findOpenPort();
    
    try {
      PortUtils.waitForPortOpen(port, 1, TimeUnit.MINUTES, stubProcess);
    } catch (Exception e) {
      assertThat(e, instanceOf(GradleException.class));
      assertThat(e.getMessage(), equalTo("Process died before port " + port + " was opened"));
    }
  }

  @Test(timeout=2000)
  public void testWaitForPortOpen_success() throws Exception {
    StubProcess stubProcess = new StubProcess();
    int port = PortUtils.findOpenPort();
    CountDownLatch latch = new CountDownLatch(1);
    
    new Thread(() -> {
      try (ServerSocket ss = new ServerSocket(port, 1, InetAddress.getLoopbackAddress())) {
        ss.accept();
        latch.countDown();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).start();
    
    PortUtils.waitForPortOpen(port, 1, TimeUnit.MINUTES, stubProcess);
    latch.await(1, TimeUnit.SECONDS);
    assertThat(latch.getCount(), equalTo(0L));
  }
  
  private static class StubProcess extends Process {
    private boolean alive = true;

    @Override
    public int waitFor() throws InterruptedException {
      return 0;
    }

    @Override
    public OutputStream getOutputStream() {
      return null;
    }

    @Override
    public InputStream getInputStream() {
      return null;
    }

    @Override
    public InputStream getErrorStream() {
      return null;
    }

    @Override
    public int exitValue() {
      return 0;
    }

    @Override
    public void destroy() {}

    @Override
    public boolean isAlive() {
      return alive;
    }
  }
}