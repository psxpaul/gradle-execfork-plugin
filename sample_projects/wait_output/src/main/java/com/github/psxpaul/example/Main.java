package com.github.psxpaul.example;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting the daemon!");
        Thread.sleep(100);
        System.out.println("Important work being done!");
        Thread.sleep(200);
        System.out.println("Daemon is up!");
        Thread.sleep(100);
        while(true) {
          System.out.println("PING");
          Thread.sleep(500);
        }
    }
}
