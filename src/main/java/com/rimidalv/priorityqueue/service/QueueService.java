package com.rimidalv.priorityqueue.service;

import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.utility.CustomPriorityQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class QueueService {
    private static final Logger LOGGER = Logger.getLogger( QueueService.class.getName() );

    private static QueueService queueService;
    private CustomPriorityQueue customPriorityQueue;

    /**
     * Singleton to create a new instance of the queue access class
     * with the default queue capacity
     */
    private QueueService() {
        customPriorityQueue = new CustomPriorityQueue();
    }

    /**
     * Singleton to create a new instance of the queue access class
     * with the specified queue capacity
     */
    private QueueService(CustomPriorityQueue customPriorityQueue) {
        this.customPriorityQueue = customPriorityQueue;
    }

    /**
     * Returns instance of the queue if exists, otherwise creates a new instance
     * with default capacity.
     *
     * @return
     */
    public static QueueService getInstance() {
        if (queueService == null)
            queueService = new QueueService();

        return queueService;
    }

    /**
     * Returns instance of the queue if exists, otherwise creates a new instance
     * with specified capacity.
     *
     * @return
     */
    public static QueueService getInstance(CustomPriorityQueue customPriorityQueue) {
        if (queueService == null)
            queueService = new QueueService(customPriorityQueue);

        return queueService;
    }

    /**
     * Deletes the instance of the queue.
     */
     public static void deleteQueueService()
    {
        queueService = null;
    }

    /**
     * Method to dequeue an element from the queue. Block the thread if the queue is empty until a new
     * element arrives.
     * Returns dequeued queue item if exists, otherwise will call the dequeue method recursively.
     *
     */
    public QueueItem dequeue() {
        while (customPriorityQueue.isEmpty()) {
            LOGGER.log(Level.INFO, "Waiting for a new item to process");
            try {
                sleep(0);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }

        QueueItem queueItem = customPriorityQueue.dequeue();
        if (queueItem == null) {
            queueItem = this.dequeue();
        }

        return queueItem;
    }

    /**
     * Enqueue method to insert a new element to the queue.
     * If the queue is out of capacity, will block the thread until the queue gets a free space.
     * Will resume the execution on the FIFO basis.
     *
     */
    public void enqueue(QueueItem queueItem) {
        while (!customPriorityQueue.hasFreeCapacity()) {
            LOGGER.log(Level.INFO, "Queue is out of capacity, waiting for free space");
            try {
                sleep(0);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
        customPriorityQueue.enqueue(queueItem);
        LOGGER.log( Level.INFO, "Equeuing item with priority {0}, and value: {1}",
                new Object[] {queueItem.getPriority(), queueItem.getQueueItem()} );
    }

}
