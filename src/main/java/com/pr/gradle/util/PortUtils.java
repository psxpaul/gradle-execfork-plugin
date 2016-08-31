package com.pr.gradle.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
  
  private static boolean isPortOpen(int port) {
    try(Socket socket = new Socket()) {
      InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
      InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
      socket.connect(socketAddress);
      return true;
    } catch(UnknownHostException e) {
      throw new RuntimeException(e);
    } catch(Exception e) {
      return false;
    }
  }
  
  public static void waitForPortOpen(int port, int timeout, TimeUnit unit) {
      try {
        new FutureTask<Boolean>(() -> {
          while(true) {
            Thread.sleep(100);
            if (isPortOpen(port))
              return true;
          }
        }).get(timeout, unit);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        throw new RuntimeException(e);
      }
  }
}