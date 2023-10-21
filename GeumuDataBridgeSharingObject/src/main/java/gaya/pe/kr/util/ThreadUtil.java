package gaya.pe.kr.util;

public class ThreadUtil {


    public static void schedule(Runnable runnable) {

        Thread thread = new Thread(runnable);
        thread.start();

    }

}
