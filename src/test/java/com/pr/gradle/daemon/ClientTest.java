package com.pr.gradle.daemon;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ClientTest {
  int controlPort = Server.findOpenPort();

  @Test
  public void shouldSendStop() throws Exception {
    final List<String> requests = new ArrayList<>();
    Thread thread = new Thread(() -> {
      try (ServerSocket serverSocket = new ServerSocket(controlPort)) {
        try (Socket socket = serverSocket.accept();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
          requests.add(inputStream.readUTF());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    thread.start();
    Thread.sleep(100); //wait for server to start
    
    new Client(controlPort).sendStopCommand();
    thread.join(5000);
    assertThat(requests, hasSize(1));
    assertThat(requests.get(0), equalTo(Command.STOP.name()));
  }

  @Test
  public void shouldSendStop_serverAlreadyStopped() {
    new Client(controlPort).sendStopCommand();
  }
}