package com.rimidalv.priorityqueue.service;

import com.rimidalv.priorityqueue.utility.CustomPriorityQueue;
import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.domain.QueueNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueServiceTest {
    @Mock
    private CustomPriorityQueue customPriorityQueue;
    @InjectMocks
    private QueueService queueService;
    private QueueItem<String> queueItemLevel1 = null;
    private QueueItem<String> queueItemLevel2 = null;
    private QueueItem<String> queueItemLevel3 = null;

    @Before
    public void setUpTestClass() {
        MockitoAnnotations.openMocks(this);
        queueItemLevel1 = new QueueItem<>(1, "Queue item priority 1");
        queueItemLevel2 = new QueueItem<>(2, "Queue item priority 2");
        queueItemLevel3 = new QueueItem<>(3, "Queue item priority 3");

        QueueService.deleteQueueService();
        queueService = QueueService.getInstance(customPriorityQueue);
    }

    @Test
    public void should_dequeue_element_once_() {
        when(customPriorityQueue.isEmpty()).thenReturn(false);
        when(customPriorityQueue.dequeue()).thenReturn(queueItemLevel1);

        QueueItem item = queueService.dequeue();

        assertEquals(queueItemLevel1, item);
        verify(customPriorityQueue, times(1)).isEmpty();
        verify(customPriorityQueue, times(1)).dequeue();
    }

    @Test
    public void should_dequeue_wait_no_empty_queue_simple_search() {
        when(customPriorityQueue.isEmpty()).thenReturn(true).thenReturn(false);
        when(customPriorityQueue.dequeue()).thenReturn(queueItemLevel1);

        queueService.dequeue();

        verify(customPriorityQueue, times(2)).isEmpty();
        verify(customPriorityQueue, times(1)).dequeue();
    }

    @Test
    public void should_enqueue_wait_no_free_space() {
        when(customPriorityQueue.hasFreeCapacity()).thenReturn(false).thenReturn(true);

        queueService.enqueue(queueItemLevel1);

        verify(customPriorityQueue, times(2)).hasFreeCapacity();
        verify(customPriorityQueue, times(1)).enqueue(queueItemLevel1);
    }
}
