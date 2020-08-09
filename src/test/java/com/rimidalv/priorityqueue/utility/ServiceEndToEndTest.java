package com.rimidalv.priorityqueue.utility;

import com.rimidalv.priorityqueue.service.QueueService;
import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.domain.QueueNode;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class ServiceEndToEndTest {
    private QueueService queueService;
    private CustomPriorityQueue customPriorityQueue;
    private QueueItem<String> queueItemLevel1 = null;
    private QueueItem<String> secondQueueItemLevel1 = null;
    private QueueItem<String> queueItemLevel2 = null;
    private QueueItem<String> queueItemLevel3 = null;
    private QueueItem<String> secondQueueItemLevel3 = null;
    private QueueItem<String> queueItemLevel4 = null;

    @Before
    public void setUpTestClass() {
        customPriorityQueue = new CustomPriorityQueue(15, false);
        queueItemLevel1 = new QueueItem<>(1, "Queue item priority 1");
        secondQueueItemLevel1 = new QueueItem<>(1, "Queue second item for priority 1");
        queueItemLevel2 = new QueueItem<>(2, "Queue item priority 2");
        queueItemLevel3 = new QueueItem<>(3, "Queue item priority 3");
        secondQueueItemLevel3 = new QueueItem<>(3, "Queue second item for priority 3");
        queueItemLevel4 = new QueueItem<>(4, "Queue item priority 4");

        customPriorityQueue.enqueue(queueItemLevel1);
        customPriorityQueue.enqueue(secondQueueItemLevel1);
        customPriorityQueue.enqueue(queueItemLevel2);
        customPriorityQueue.enqueue(queueItemLevel3);
        customPriorityQueue.enqueue(secondQueueItemLevel3);
        customPriorityQueue.enqueue(queueItemLevel4);

        QueueService.deleteQueueService();
        queueService = QueueService.getInstance(customPriorityQueue);
    }

    @Test
    public void should_dequeue_element_with_priority_1() {
        queueService.dequeue();

        assertEquals(secondQueueItemLevel1, customPriorityQueue.getFirstNodeByPriority(1).getQueueItem());
        int priorityInSet =  customPriorityQueue.getVisitedPrioritesList().get(1);
        assertEquals(1, priorityInSet);
    }

    @Test
    public void should_dequeue_element_and_trigger_burst_rate_for_priority_2() throws InterruptedException {
        customPriorityQueue.getVisitedPrioritesList().put(1,2);

        queueService.dequeue();

        assertEquals(queueItemLevel1, customPriorityQueue.getFirstNodeByPriority(1).getQueueItem());
        assertNull(customPriorityQueue.getFirstNodeByPriority(2));

        Map<Integer, Integer> visitedList =  customPriorityQueue.getVisitedPrioritesList();
        assertTrue(visitedList.containsKey(2));
        int priorityCount = visitedList.get(1);
        assertEquals(0, priorityCount);
        priorityCount = visitedList.get(2);
        assertEquals(1, priorityCount);
    }

    @Test
    public void should_dequeue_element_and_trigger_burst_rate_for_multiple_priorities() throws InterruptedException {
        QueueItem<String> queueItemLevel5 = new QueueItem<>(5, "Queue item priority 5");
        customPriorityQueue.getVisitedPrioritesList().put(1, 2);
        customPriorityQueue.getVisitedPrioritesList().put(2, 2);
        customPriorityQueue.getVisitedPrioritesList().put(3, 2);
        customPriorityQueue.enqueue(queueItemLevel5);

        queueService.dequeue();

        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);
        assertEquals(queueItemLevel1, queueNode.getQueueItem());
        assertEquals(secondQueueItemLevel1, queueNode.getNextItem().getQueueItem());
        assertEquals(queueItemLevel2, customPriorityQueue.getFirstNodeByPriority(2).getQueueItem());
        assertEquals(queueItemLevel5, customPriorityQueue.getFirstNodeByPriority(5).getQueueItem());

        Map<Integer, Integer> visitedList =  customPriorityQueue.getVisitedPrioritesList();
        assertTrue( visitedList.containsKey(4));
        int priorityCount = visitedList.get(1);
        assertEquals(0, priorityCount);
        priorityCount = visitedList.get(2);
        assertEquals(0, priorityCount);
        priorityCount = visitedList.get(3);
        assertEquals(0, priorityCount);
        priorityCount = visitedList.get(4);
        assertEquals(1, priorityCount);
        assertNull(visitedList.get(5));
    }

    @Test
    public void should_wait_until_new_element_comes_queue_is_empty() throws InterruptedException {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue();
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);
        ExecutorService service = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicBoolean isFinished = new AtomicBoolean(false);
        service.submit(() -> {
            queueService.dequeue();
            latch.countDown();
            isFinished.set(true);
        });

        Thread.sleep(500);
        assertFalse(isFinished.get());

        queueService.enqueue(queueItemLevel1);

        latch.await();
        assertTrue(isFinished.get());
    }

    @Test
    public void should_wait_until_queue_gets_free_space_available() throws InterruptedException {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(1, false);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        ExecutorService service = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);
        queueService.enqueue(queueItemLevel1);
        assertFalse(customPriorityQueue.hasFreeCapacity());

        AtomicBoolean isFinished = new AtomicBoolean(false);
        service.submit(() -> {
            queueService.enqueue(queueItemLevel2);
            latch.countDown();
            isFinished.set(true);
        });

        Thread.sleep(500);
        assertFalse(isFinished.get());
        queueService.dequeue();

        latch.await();
        QueueNode<QueueItem> node = customPriorityQueue.getFirstNodeByPriority(2);
        assertTrue(isFinished.get());
        assertEquals(queueItemLevel2, node.getQueueItem());
    }

    @Test
    public void should_enqueue_and_dequeue_multiple_items_in_single_threaded_way_no_depth_search() {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(35, false);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        LinkedList<QueueItem> linkedList = this.populateQueueItemList();
        linkedList.forEach(customPriorityQueue::enqueue);

        int[] expectedArray = {1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 3, 4, 1, 1, 2, 1, 2, 3, 2, 2, 3, 4, 5, 2, 3, 3, 4, 3, 4, 5, 6, 4};
        for (int i = 0; i < 35; i++) {
            QueueItem integerQueueItem = queueService.dequeue();
            assertEquals(expectedArray[i], integerQueueItem.getPriority());
        }

        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_enqueue_and_dequeue_multiple_items_in_single_threaded_way_depth_search_enabled() {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(35, true);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        LinkedList<QueueItem> linkedList = this.populateQueueItemList();
        linkedList.forEach(customPriorityQueue::enqueue);

        int[] expectedArray = {1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 3, 4, 1, 1, 2, 1, 2, 3, 2, 2, 3, 4, 5, 2, 3, 3, 4, 3, 4, 5, 6, 4};
        for (int i = 0; i < 35; i++) {
            QueueItem integerQueueItem = queueService.dequeue();
            assertEquals(expectedArray[i], integerQueueItem.getPriority());
        }

        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_enqueue_and_dequeue_multiple_items_in_multi_threaded_way() throws InterruptedException {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(35, false);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        ExecutorService service = Executors.newFixedThreadPool(5);
        LinkedList<QueueItem> linkedList = this.populateQueueItemList();
        List<QueueItem> synchronizedList = Collections.synchronizedList(linkedList);

        for (int i = 0; i < 5; i++) {
            service.submit(() -> {
                while (!synchronizedList.isEmpty()) {
                    QueueItem item = null;
                    synchronized (synchronizedList) {
                        if (!synchronizedList.isEmpty()) {
                            item = synchronizedList.get(0);
                            synchronizedList.remove(0);
                        }
                    }
                    if (item != null) {
                        queueService.enqueue(item);
                    }
                }
            });
            service.submit(() -> {
                while (true) {
                    queueService.dequeue();
                }
            });
        }

        Thread.sleep(500);

        assertTrue(synchronizedList.isEmpty());
        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void use_case_randomly_inserted_items() {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(10, false);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        QueueItem<Integer> it1 = new QueueItem<>(4, 4);
        QueueItem<Integer> it2 = new QueueItem<>(1, 1);
        QueueItem<Integer> it3 = new QueueItem<>(3, 3);
        QueueItem<Integer> it4 = new QueueItem<>(2, 2);
        QueueItem<Integer> it5 = new QueueItem<>(1, 1);
        QueueItem<Integer> it6 = new QueueItem<>(2, 2);

        queueService.enqueue(it1);
        queueService.enqueue(it2);
        queueService.enqueue(it3);
        queueService.enqueue(it4);
        queueService.enqueue(it5);
        queueService.enqueue(it6);

        assertEquals(1, queueService.dequeue().getPriority());
        assertEquals(1, queueService.dequeue().getPriority());
        queueService.enqueue(it2);
        assertEquals(2, queueService.dequeue().getPriority());
        assertEquals(1, queueService.dequeue().getPriority());
        assertEquals(2, queueService.dequeue().getPriority());
        assertEquals(3, queueService.dequeue().getPriority());
        assertEquals(4, queueService.dequeue().getPriority());
        assertTrue(customPriorityQueue.isEmpty());
    }


    @Test
    public void use_case_test() {
        CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(10, false);
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);


        QueueItem<Integer> it1 = new QueueItem<>(4, 4);
        QueueItem<Integer> it2 = new QueueItem<>(1, 1);
        QueueItem<Integer> it3 = new QueueItem<>(3, 3);
        QueueItem<Integer> it4 = new QueueItem<>(2, 2);
        QueueItem<Integer> it5 = new QueueItem<>(2, 2);

        queueService.enqueue(it1);
        queueService.enqueue(it2);
        queueService.enqueue(it3);
        queueService.enqueue(it4);
        queueService.enqueue(it5);

        assertEquals(1, queueService.dequeue().getPriority());
        assertEquals(2, queueService.dequeue().getPriority());
        customPriorityQueue.enqueue(it2);
        assertEquals(1, queueService.dequeue().getPriority());
        assertEquals(2, queueService.dequeue().getPriority());
        assertEquals(3, queueService.dequeue().getPriority());
        assertEquals(4, queueService.dequeue().getPriority());
        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void multi_threaded_dequeue_should_not_return_null_objects() throws InterruptedException {
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(new CustomPriorityQueue());

        ThreadTest T1 = new ThreadTest( queueService, "Thread-1-enqueue", true);
        T1.start();

        ThreadTest T2 = new ThreadTest( queueService, "Thread-2-dequeue", false);
        T2.start();

        ThreadTest T3 = new ThreadTest( queueService, "Thread-3-enqueue", true);
        T3.start();

        ThreadTest T4 = new ThreadTest( queueService, "Thread-4-dequeue", false);
        T4.start();

        ThreadTest T5 = new ThreadTest( queueService, "Thread-3-enqueue", true);
        T5.start();

        ThreadTest T6 = new ThreadTest( queueService, "Thread-4-dequeue", false);
        T6.start();


        sleep(1000);

        while (T2.isAlive() || T4.isAlive() || T6.isAlive()) {
            queueService.enqueue(new QueueItem<>(1, 1));
        }

        assertFalse(T2.getThreadTestingFailed());
        assertFalse(T4.getThreadTestingFailed());
        assertFalse(T6.getThreadTestingFailed());
    }

    @Test
    public void use_case_parallel_enqueue_and_dequeue() throws Exception {
        final CustomPriorityQueue customPriorityQueue = new CustomPriorityQueue(2,false); // same result with True
        QueueService.deleteQueueService();
        QueueService queueService = QueueService.getInstance(customPriorityQueue);

        final int[] input = new int[]{1, 1, 1, 2, 2, 2, 3, 3, 3};
        final int[] output = new int[]{1, 1, 2, 1, 2, 3, 2, 3, 3};
        new Thread(() -> {
            for (int x : input) {
                queueService.enqueue(new QueueItem(x, x));

            }
        }).start();

        for (int x : output) {
            Thread.sleep(10);
            assertEquals(x, queueService.dequeue().getPriority());
        }
    }

    private LinkedList<QueueItem> populateQueueItemList() {
        LinkedList<QueueItem> linkedList = new LinkedList<>();

        QueueItem<String> firstQueueItemLevel4 =
                new QueueItem<>(4, "First queue item priority 4");
        QueueItem<String> firstQueueItemLevel1 =
                new QueueItem<>(1, "First queue item priority 1");
        QueueItem<String> firstQueueItemLevel3 =
                new QueueItem<>(3, "First queue item priority 3");
        QueueItem<String> firstQueueItemLevel2 =
                new QueueItem<>(2, "First queue item priority 2");

        QueueItem<String> secondQueueItemLevel1 =
                new QueueItem<>(1, "Second queue item priority 1");
        QueueItem<String> secondQueueItemLevel4 =
                new QueueItem<>(4, "Second queue item priority 4");
        QueueItem<String> secondQueueItemLevel2 =
                new QueueItem<>(2, "Second queue item priority 2");
        QueueItem<String> secondQueueItemLevel3 =
                new QueueItem<>(3, "Second queue item priority 3");

        QueueItem<String> thirdQueueItemLevel2 =
                new QueueItem<>(2, "Third queue item priority 2");
        QueueItem<String> thirdQueueItemLevel4 =
                new QueueItem<>(4, "Third queue item priority 4");
        QueueItem<String> thirdQueueItemLevel1 =
                new QueueItem<>(1, "Third queue item priority 1");
        QueueItem<String> thirdQueueItemLevel3 =
                new QueueItem<>(3, "Third queue item priority 3");

        QueueItem<String> fourthQueueItemLevel3 =
                new QueueItem<>(3, "Fourth queue item priority 3");
        QueueItem<String> firstQueueItemLevel5 =
                new QueueItem<>(5, "First queue item priority 5");
        QueueItem<String> fourthQueueItemLevel2 =
                new QueueItem<>(2, "Fourth queue item priority 2");
        QueueItem<String> fourthQueueItemLevel1 =
                new QueueItem<>(1, "Fourth queue item priority 1");

        QueueItem<String> fifthQueueItemLevel3 =
                new QueueItem<>(3, "Fifth queue item priority 3");
        QueueItem<String> firstQueueItemLevel6 =
                new QueueItem<>(6, "First queue item priority 6");
        QueueItem<String> fifthQueueItemLevel1 =
                new QueueItem<>(1, "Fifth queue item priority 1");
        QueueItem<String> fifthQueueItemLevel2 =
                new QueueItem<>(2, "Fifth queue item priority 2");

        QueueItem<String> fourthQueueItemLevel4 =
                new QueueItem<>(4, "Fourth queue item priority 4");
        QueueItem<String> sixthQueueItemLevel2 =
                new QueueItem<>(2, "Sixth queue item priority 2");
        QueueItem<String> fifthQueueItemLevel4 =
                new QueueItem<>(4, "Fifth queue item priority 4");
        QueueItem<String> sixthQueueItemLevel1 =
                new QueueItem<>(1, "Sixth queue item priority 1");

        QueueItem<String> sixthQueueItemLevel3 =
                new QueueItem<>(3, "Sixth queue item priority 3");
        QueueItem<String> seventhQueueItemLevel2 =
                new QueueItem<>(2, "Seventh queue item priority 2");
        QueueItem<String> seventhQueueItemLevel1 =
                new QueueItem<>(1, "Seventh queue item priority 1");
        QueueItem<String> secondQueueItemLevel5 =
                new QueueItem<>(5, "Second queue item priority 5");

        QueueItem<String> eighthQueueItemLevel2 =
                new QueueItem<>(2, "Eighth queue item priority 2");
        QueueItem<String> eighthQueueItemLevel1 =
                new QueueItem<>(1, "Eighth queue item priority 1");
        QueueItem<String> ninthQueueItemLevel1 =
                new QueueItem<>(1, "Ninth queue item priority 1");
        QueueItem<String> ninthQueueItemLevel2 =
                new QueueItem<>(2, "Ninth queue item priority 2");

        QueueItem<String> seventhQueueItemLevel3 =
                new QueueItem<>(3, "Seventh queue item priority 3");
        QueueItem<String> tenthQueueItemLevel1 =
                new QueueItem<>(1, "Sixth queue item priority 1");
        QueueItem<String> eleventhQueueItemLevel1 =
                new QueueItem<>(1, "Seventh queue item priority 1");

        linkedList.add(firstQueueItemLevel4);
        linkedList.add(firstQueueItemLevel1);
        linkedList.add(firstQueueItemLevel3);
        linkedList.add(firstQueueItemLevel2);
        linkedList.add(secondQueueItemLevel1);
        linkedList.add(secondQueueItemLevel4);
        linkedList.add(secondQueueItemLevel2);
        linkedList.add(secondQueueItemLevel3);
        linkedList.add(thirdQueueItemLevel2);
        linkedList.add(thirdQueueItemLevel4);
        linkedList.add(thirdQueueItemLevel1);
        linkedList.add(thirdQueueItemLevel3);
        linkedList.add(fourthQueueItemLevel3);
        linkedList.add(firstQueueItemLevel5);
        linkedList.add(fourthQueueItemLevel2);
        linkedList.add(fourthQueueItemLevel1);
        linkedList.add(fifthQueueItemLevel3);
        linkedList.add(firstQueueItemLevel6);
        linkedList.add(fifthQueueItemLevel1);
        linkedList.add(fifthQueueItemLevel2);
        linkedList.add(fourthQueueItemLevel4);
        linkedList.add(sixthQueueItemLevel2);
        linkedList.add(fifthQueueItemLevel4);
        linkedList.add(sixthQueueItemLevel1);
        linkedList.add(sixthQueueItemLevel3);
        linkedList.add(seventhQueueItemLevel2);
        linkedList.add(seventhQueueItemLevel1);
        linkedList.add(secondQueueItemLevel5);
        linkedList.add(eighthQueueItemLevel2);
        linkedList.add(eighthQueueItemLevel1);
        linkedList.add(ninthQueueItemLevel1);
        linkedList.add(ninthQueueItemLevel2);
        linkedList.add(seventhQueueItemLevel3);
        linkedList.add(tenthQueueItemLevel1);
        linkedList.add(eleventhQueueItemLevel1);

        return linkedList;
    }

    private class ThreadTest extends Thread {
        private QueueService queueService;
        private Thread t;
        private String threadName;
        private boolean isEnqueueMode;
        private boolean threadTestingFailed;

        public ThreadTest (QueueService queueService, String name, boolean isEnqueueMode) {
            this.queueService = queueService;
            this.threadName = name;
            this.isEnqueueMode = isEnqueueMode;
            this.threadTestingFailed = false;
        }

        public void run() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if (isEnqueueMode) {
                for (int i = 0; i < 50; i++) {
                    QueueItem<String> item = new QueueItem<>(random.nextInt(1,5), "Thread - " + threadName + " item - " + i);
                    queueService.enqueue(item);
                }
            } else {
                for (int i = 0; i < 50; i++) {
                    if (queueService.dequeue() == null) {
                        this.threadTestingFailed = true;
                    }
                }
            }
        }

        public void start () {
            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }

        public boolean getThreadTestingFailed() {
            return this.threadTestingFailed;
        }
    }

}
