package com.pr.gradle.daemon;

import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
  private static final Logger log = LoggerFactory.getLogger(Client.class);
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
        log.info("Server already stopped");
        //ignore, since the server was already stopped
      } else {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}