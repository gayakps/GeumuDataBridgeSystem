package com.velocitypowered.proxy.connection.player;

public class TestObj {

    String data;

    public String get() {

        try {
            System.out.printf("[%s] 데이터에 접근합니다.\n", Thread.currentThread().getName());
            synchronized ( this ) {
                wait();
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }

        return data;

    }

    public void startNotify(String data) {
        try {
            System.out.printf("[%s] 수정 접근 깨웁니다\n", Thread.currentThread().getName());
            this.data = data;
            synchronized (this) {
                notify();
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

}
