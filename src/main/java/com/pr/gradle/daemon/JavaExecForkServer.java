package com.pr.gradle.daemon;

import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.pr.gradle.util.ThrowableUtils;

public class JavaExecForkServer {
  protected static Consumer<Integer> EXIT = System::exit;

  public static void main(String[] args) throws Exception {
    assert args.length >= 2;

    String className = args[0];
    int controlPort = Integer.parseInt(args[1]);
    String[] classArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[]{};

    Class<?> mainClass = Class.forName(className);
    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
    final AtomicBoolean mainTerminated = new AtomicBoolean(false);

    try (ServerSocket serverSocket = new ServerSocket(controlPort, 1, InetAddress.getByName("127.0.0.1"))) {
      Thread mainThread = new Thread(() -> {
        try {
          mainMethod.invoke(mainClass, new Object[]{ classArgs });
          mainTerminated.set(true);
        } catch (InvocationTargetException e) {
          mainTerminated.set(true);
          if (ThrowableUtils.rootCauseOf(e) instanceof InterruptedException) {
            System.out.println(className + " stopped");
          } else {
            System.err.println("Error invoking main method " + className);
            e.printStackTrace(System.err);
          }
        } catch (Exception e) {
          mainTerminated.set(true);
          System.err.println("Error invoking main method " + className);
          e.printStackTrace(System.err);
        } finally {
          new Client(controlPort).sendStopCommand();
        }
      });
      mainThread.setDaemon(true);
      mainThread.start();

      Socket socket = serverSocket.accept();
      ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
      String commandString = inputStream.readUTF();
      if (commandString.equals(Command.STOP.name())) {
        mainThread.interrupt();
        mainThread.join(5000);
        if (!mainThread.isAlive()) {
          EXIT.accept(0);
        } else {
          System.err.println("Process did not end after 5 seconds, killing");
          EXIT.accept(-1);
        }
      } else {
        System.out.println("unknown control command received: " + commandString);
      }
    } catch (SocketException e) {
      if (mainTerminated.get() && e.getMessage().contains("Socket closed")) {
        System.out.println("Ignoring " + SocketException.class.getSimpleName() + 
            " with message " + e.getMessage() + ", because main class has already terminated");
        EXIT.accept(0);
      } else {
        System.err.println("Unexpected " + SocketException.class.getSimpleName());
        e.printStackTrace(System.err);
        EXIT.accept(-2);
      }
    }
    EXIT.accept(0);
  }
}