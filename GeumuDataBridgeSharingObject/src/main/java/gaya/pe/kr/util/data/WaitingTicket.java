package gaya.pe.kr.util.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class WaitingTicket<T> {

    long ticketNumber = new Random().nextLong();
    private T responseObject = null;

    private @Nullable Consumer<T> resultConsumer;
    private final Class<?> expectResponseClazz;

    boolean dateSetUp = false;

    public WaitingTicket(@Nullable Consumer<T> resultConsumer, Class<?> expectResponseClazz) {
        this.resultConsumer = resultConsumer;
        this.expectResponseClazz = expectResponseClazz;
        System.out.printf("%d : %s Ticket Created\n",ticketNumber , expectResponseClazz.getSimpleName());
    }

    public WaitingTicket(Class<?> expectResponseClazz) {
        this.expectResponseClazz = expectResponseClazz;
    }

    public synchronized void executeWaitingTicket() {
        try {
            System.out.printf("[%d] [%s] 접근(Consumer) Waiting\n",ticketNumber , Thread.currentThread().getName());
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert resultConsumer != null;
        resultConsumer.accept(this.responseObject);
    }

    public synchronized T getResult() {
        try {

            if ( !dateSetUp ) {
                System.out.printf("[%d] [%s] 접근 Waiting\n",ticketNumber , Thread.currentThread().getName());
                wait();
            } else {
                System.out.printf("[%d] [%s] 접근 Object 있음 바로 리턴\n",ticketNumber , Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responseObject;
    }
    
    public synchronized void setResult(T response) {

        this.responseObject = response;

        System.out.printf("[%d] [%s] Result-Set 설정 notify all\n", ticketNumber, Thread.currentThread().getName());

        notifyAll();

        dateSetUp = true;

        if (response != null) {

            if (!response.getClass().getTypeName().equals(expectResponseClazz.getTypeName())) {
                throw new RuntimeException(String.format("기대 값 : %s 현재 값 : %s 이 서로 일치하지 않습니다", this.expectResponseClazz.getSimpleName(), response.getClass().getSimpleName()));
            }

        }

    }


}
