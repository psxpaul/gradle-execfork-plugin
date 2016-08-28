package com.pr.gradle.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  private static final Logger log = LoggerFactory.getLogger(Server.class);

  public static void main(String[] args) throws Exception {
    assert args.length >= 2;

    int controlPort = Integer.parseInt(args[0]);
    String className = args[1];
    String[] classArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[]{};

    Class<?> mainClass = Class.forName(className);
    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
    final AtomicBoolean mainTerminated = new AtomicBoolean(false);

    try (ServerSocket serverSocket = new ServerSocket(controlPort)) {
      Thread mainThread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            mainMethod.invoke(mainClass, new Object[]{ classArgs });
            mainTerminated.set(true);
          } catch (Exception e) {
            mainTerminated.set(true);
            log.error("Error invoking main method {}", className, e);
            throw new RuntimeException(e);
          } finally {
            try {
              serverSocket.close();
            } catch (IOException e) {
              log.error("Error closing server socket", e);
              throw new RuntimeException(e);
            }
          }
        }
      });
      mainThread.start();

      Socket socket = serverSocket.accept();
      ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
      String commandString = inputStream.readUTF();
      if (commandString.equals(Command.STOP.name())) {
        mainThread.interrupt();
        mainThread.join();
      }
    } catch (SocketException e) {
      if (mainTerminated.get() && e.getMessage().contains("Socket closed")) {
        log.info("Ignoring {} with message {}, because main class has already terminated",
            SocketException.class.getSimpleName(), e.getMessage());
      } else {
        log.error("Unexpected {}", SocketException.class.getSimpleName(), e);
        throw e;
      }
    }
  }
  
  public static int findOpenPort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}