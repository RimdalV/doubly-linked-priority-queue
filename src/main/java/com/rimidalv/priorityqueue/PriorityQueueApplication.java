package com.rimidalv.priorityqueue;

import com.rimidalv.priorityqueue.demo.ThreadDemo;
import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.service.QueueService;

public class PriorityQueueApplication {


    public static void main(String[] args) throws InterruptedException {
        QueueService queueService = QueueService.getInstance();

        ThreadDemo T1 = new ThreadDemo( queueService, "Thread-1-enqueue", true);
        T1.start();

        ThreadDemo T2 = new ThreadDemo( queueService, "Thread-2-dequeue", false);
        T2.start();

        ThreadDemo T3 = new ThreadDemo( queueService, "Thread-3-enqueue", true);
        T3.start();

        ThreadDemo T4 = new ThreadDemo( queueService, "Thread-4-dequeue", false);
        T4.start();

        Thread.sleep(2000);

        while (T2.isAlive() || T4.isAlive()) {
            queueService.enqueue(new QueueItem<>(1, 1));
        }
    }


}
