package com.pr.gradle.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
  public static void main(String[] args) throws Exception {
    assert args.length >= 2;

    String className = args[0];
    int controlPort = Integer.parseInt(args[1]);
    String[] classArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[]{};

    Class<?> mainClass = Class.forName(className);
    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
    final AtomicBoolean mainTerminated = new AtomicBoolean(false);

    try (ServerSocket serverSocket = new ServerSocket(controlPort)) {
      Thread mainThread = new Thread(() -> {
        try {
          mainMethod.invoke(mainClass, new Object[]{ classArgs });
          mainTerminated.set(true);
        } catch (InvocationTargetException e) {
          mainTerminated.set(true);
          if (e.getCause() instanceof InterruptedException) {
            System.out.println(className + " stopped");
          } else {
            System.err.println("Error invoking main method " + className);
            throw new RuntimeException(e);
          }
        } catch (Exception e) {
          mainTerminated.set(true);
          System.err.println("Error invoking main method " + className);
          throw new RuntimeException(e);
        } finally {
          try {
            serverSocket.close();
          } catch (IOException e) {
            System.err.println("Error closing server socket");
            throw new RuntimeException(e);
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
        System.out.println("Ignoring " + SocketException.class.getSimpleName() + 
            " with message " + e.getMessage() + ", because main class has already terminated");
      } else {
        System.err.println("Unexpected " + SocketException.class.getSimpleName());
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