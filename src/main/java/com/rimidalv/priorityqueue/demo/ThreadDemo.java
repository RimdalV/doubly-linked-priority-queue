package com.rimidalv.priorityqueue.demo;


import com.rimidalv.priorityqueue.PriorityQueueApplication;
import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.service.QueueService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadDemo extends Thread {
    private static final Logger LOGGER = Logger.getLogger( PriorityQueueApplication.class.getName() );

    private QueueService queueService;
    private Thread t;
    private String threadName;
    private boolean isEnqueueMode;

    public ThreadDemo (QueueService queueService, String name, boolean isEnqueueMode) {
        this.queueService = queueService;
        threadName = name;
        this.isEnqueueMode = isEnqueueMode;
        LOGGER.log( Level.INFO, "Creating thread {0}", threadName);
    }

    public void run() {
        LOGGER.log(Level.INFO, "Running {0}", threadName);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (isEnqueueMode) {
            for (int i = 0; i < 15; i++) {
                QueueItem<String> item = new QueueItem<>(random.nextInt(5), "Thread - " + threadName + " item - " + i);
                queueService.enqueue(item);
            }
        } else {
            for (int i = 0; i < 15; i++) {
                queueService.dequeue();
            }
        }
        LOGGER.log(Level.INFO, "Exiting {0}", threadName);
    }

    public void start () {
        LOGGER.log(Level.INFO, "Starting {0}",  threadName);
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}