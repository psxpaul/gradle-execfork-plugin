package com.pr.gradle.example;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Daemon is now running!");
        while(true) {
          System.out.println("PING");
          Thread.sleep(500);
        }
    }
}
