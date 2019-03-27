package com.github.psxpaul.example;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Daemon started with args: " + String.join(", ", args));
        System.out.println("Daemon is now running!");
        while(true) {
          System.out.println("PING");
          Thread.sleep(500);
        }
    }
}
