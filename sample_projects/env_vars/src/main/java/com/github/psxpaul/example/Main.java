package com.github.psxpaul.example;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Daemon is now running!");

        System.out.println("VAR_ONE = " + System.getenv("VAR_ONE"));
        System.out.println("VAR_TWO = " + System.getenv("VAR_TWO"));
        System.out.println("VAR_THREE = " + System.getenv("VAR_THREE"));

        while(true) {
          System.out.println("PING");
          Thread.sleep(500);
        }
    }
}
