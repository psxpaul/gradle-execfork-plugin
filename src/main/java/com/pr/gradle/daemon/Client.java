package com.pr.gradle.daemon;

import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
  private final int controlPort;

  public Client(int controlPort) {
    this.controlPort = controlPort;
  }

  public void sendStopCommand() {
    try (Socket socket = new Socket("localhost", controlPort);
         ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {
      outputStream.writeUTF(Command.STOP.name());
    } catch (ConnectException e) {
      if (e.getMessage().contains("Connection refused")) {
        //ignore, since the server was already stopped
      } else {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
