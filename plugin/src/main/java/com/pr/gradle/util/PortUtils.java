package com.pr.gradle.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.gradle.api.GradleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortUtils {
  protected static final Logger log = LoggerFactory.getLogger(PortUtils.class);

  public static int findOpenPort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static boolean isPortOpen(int port) throws IOException {
    log.info("checking port " + port);

    try (Socket socket = new Socket()) {
      InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
      InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
      socket.connect(socketAddress);
      return true;
    } catch(ConnectException e) {
      return false;
    }
  }
  
  public static void waitForPortOpen(int port, int timeout, TimeUnit unit, Process process) throws IOException, InterruptedException {
    log.info("waiting for port " + port);
    
    long millisToWait = unit.toMillis(timeout);
    long waitUntil = System.currentTimeMillis() + millisToWait;

    while (System.currentTimeMillis() < waitUntil) {
      Thread.sleep(100);
      if (!process.isAlive()) throw new GradleException("Process died before port " + port + " was opened");
      if (isPortOpen(port)) return;
    }
    
    throw new GradleException("Timed out waiting for port " + port + " to be opened");
  }
}