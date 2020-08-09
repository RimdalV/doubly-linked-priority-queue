package com.rimidalv.priorityqueue.utility;

import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.domain.QueueNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CustomPriorityQueueTest {

    private QueueItem<String> queueItem = new QueueItem<>(1, "This is a new queue");
    private CustomPriorityQueue customPriorityQueue;

    @Before
    public void setUpTestClass() {
        customPriorityQueue = new CustomPriorityQueue();
    }


    @Test
    public void should_not_enqueue_item_node_queue_at_max_capacity() {
        customPriorityQueue = new CustomPriorityQueue(1, true);
        QueueItem<String> lowerQueueItem =
                new QueueItem<>(2, "This is newer queue item with lower priority");
        customPriorityQueue.addToEnd(queueItem);

        customPriorityQueue.enqueue(lowerQueueItem);

        assertNull(customPriorityQueue.getLastNodeByPriority(lowerQueueItem.getPriority()));
    }

    @Test
    public void should_enqueue_item_node_to_front() {
        assertTrue(customPriorityQueue.isEmpty());

        customPriorityQueue.enqueue(queueItem);

        assertFalse(customPriorityQueue.isEmpty());
        assertEquals(queueItem, customPriorityQueue.getHeaderNodeElement().getQueueItem());
    }

    @Test
    public void should_enqueue_item_node_after_existing() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is newer queue item");
        QueueItem<String> lowerQueueItem =
                new QueueItem<>(2, "This is newer queue item with lower priority");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(lowerQueueItem);

        QueueItem<String> newestQueueItem = new QueueItem<>(1, "This is the newest queue item");

        customPriorityQueue.enqueue(newestQueueItem);

        QueueNode<QueueItem> insertedNode = customPriorityQueue.getLastNodeByPriority(newestQueueItem.getPriority());
        assertEquals(newestQueueItem, insertedNode.getQueueItem());
        assertEquals(newerQueueItem, insertedNode.getPreviousItem().getQueueItem());
        assertEquals(lowerQueueItem, insertedNode.getNextItem().getQueueItem());
        assertEquals(insertedNode, customPriorityQueue.getLastNodeByPriority(newestQueueItem.getPriority()));
    }

    @Test
    public void should_enqueue_item_to_the_end_of_the_queue() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is newer queue item");
        QueueItem<String> lowerQueueItem =
                new QueueItem<>(2, "This is newer queue item with lower priority");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(lowerQueueItem);

        QueueItem<String> lowestQueueItem =
                new QueueItem<>(3, "This is the lowest priority queue item");

        customPriorityQueue.enqueue(lowestQueueItem);

        QueueNode<QueueItem> insertedNode = customPriorityQueue.getLastNodeByPriority(lowestQueueItem.getPriority());
        assertEquals(lowestQueueItem, insertedNode.getQueueItem());
        assertEquals(lowerQueueItem, insertedNode.getPreviousItem().getQueueItem());
        assertNull(insertedNode.getNextItem());
        assertEquals(insertedNode, customPriorityQueue.getLastNodeByPriority(lowestQueueItem.getPriority()));
        assertEquals(insertedNode, customPriorityQueue.getFirstNodeByPriority(lowestQueueItem.getPriority()));
    }

    @Test
    public void should_enqueue_item_to_the_front_of_the_queue() {
        QueueItem<String> newerQueueItem = new QueueItem<>(2, "This is newer queue item");
        QueueItem<String> newestQueueItem =
                new QueueItem<>(3, "This is newer queue item with lower priority");
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(newestQueueItem);

        QueueItem<String> highestQueueItem =
                new QueueItem<>(1, "This is the highest priority queue item");

        customPriorityQueue.enqueue(highestQueueItem);

        QueueNode<QueueItem> insertedNode = customPriorityQueue.getLastNodeByPriority(highestQueueItem.getPriority());
        assertEquals(highestQueueItem, insertedNode.getQueueItem());
        assertEquals(newerQueueItem, insertedNode.getNextItem().getQueueItem());
        assertNull(insertedNode.getPreviousItem());
        assertEquals(insertedNode, customPriorityQueue.getLastNodeByPriority(highestQueueItem.getPriority()));
        assertEquals(insertedNode, customPriorityQueue.getFirstNodeByPriority(highestQueueItem.getPriority()));
    }

    @Test
    public void should_enqueue_item_before_existing_node_for_lower_priority() {
        QueueItem<String> queueItemLevel2 =
                new QueueItem<>(2, "This is 2nd level priority queue item");
        QueueItem<String> queueItemLevel4 =
                new QueueItem<>(4, "This is 4th level priority queue item with lower priority");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(queueItemLevel2);
        customPriorityQueue.addToEnd(queueItemLevel4);

        QueueItem<String> queueItemLevel3 =
                new QueueItem<>(3, "This is the 3rd level priority queue item");

        customPriorityQueue.enqueue(queueItemLevel3);

        QueueNode<QueueItem> insertedNode = customPriorityQueue.getLastNodeByPriority(queueItemLevel3.getPriority());
        assertEquals(queueItemLevel3, insertedNode.getQueueItem());
        assertEquals(queueItemLevel2, insertedNode.getPreviousItem().getQueueItem());
        assertEquals(queueItemLevel4, insertedNode.getNextItem().getQueueItem());
        assertEquals(insertedNode, insertedNode.getPreviousItem().getNextItem());
        assertEquals(insertedNode, insertedNode.getNextItem().getPreviousItem());
        assertEquals(insertedNode, customPriorityQueue.getLastNodeByPriority(queueItemLevel3.getPriority()));
        assertEquals(insertedNode, customPriorityQueue.getFirstNodeByPriority(queueItemLevel3.getPriority()));
    }

    @Test
    public void should_add_to_the_front_of_the_empty_queue() {
        assertTrue(customPriorityQueue.isEmpty());

        customPriorityQueue.addToFront(queueItem);

        QueueNode<QueueItem> headerElement = customPriorityQueue.getHeaderNodeElement();
        assertEquals(queueItem, headerElement.getQueueItem());
        assertNull(headerElement.getNextItem());
        assertNull(headerElement.getPreviousItem());
        assertEquals(headerElement, customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(headerElement, customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }

    @Test
    public void should_add_to_the_front_of_the_existing_queue() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is the newest queue item");
        customPriorityQueue.addToFront(queueItem);

        assertFalse(customPriorityQueue.isEmpty());

        customPriorityQueue.addToFront(newerQueueItem);

        QueueNode<QueueItem> headerElement = customPriorityQueue.getHeaderNodeElement();
        assertEquals(newerQueueItem, headerElement.getQueueItem());
        assertEquals(queueItem, headerElement.getNextItem().getQueueItem());
        assertNull(headerElement.getPreviousItem());
        assertEquals(headerElement, customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(headerElement.getNextItem(), customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }

    @Test
    public void should_add_to_the_end_of_the_empty_queue() {
        assertTrue(customPriorityQueue.isEmpty());

        customPriorityQueue.addToEnd(queueItem);

        QueueNode<QueueItem> headerElement = customPriorityQueue.getHeaderNodeElement();
        assertEquals(queueItem, headerElement.getQueueItem());
        assertNull(headerElement.getNextItem());
        assertNull(headerElement.getPreviousItem());
        assertEquals(headerElement, customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(headerElement, customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }

    @Test
    public void should_add_to_the_end_of_the_existing_queue() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is the newest queue item");
        customPriorityQueue.addToEnd(queueItem);

        assertFalse(customPriorityQueue.isEmpty());

        customPriorityQueue.addToEnd(newerQueueItem);

        QueueNode<QueueItem> headerElement = customPriorityQueue.getHeaderNodeElement();
        assertEquals(queueItem, headerElement.getQueueItem());
        assertEquals(newerQueueItem, headerElement.getNextItem().getQueueItem());
        assertNull(headerElement.getPreviousItem());
        assertEquals(headerElement, customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(headerElement.getNextItem(), customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }

    @Test
    public void should_add_to_the_end_of_the_existing_queue_with_multiple_elements() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is newer queue item");
        QueueItem<String> newestQueueItem = new QueueItem<>(1, "This is the newest queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(newestQueueItem);

        assertFalse(customPriorityQueue.isEmpty());

        QueueItem<String> brandNewQueueItem = new QueueItem<>(1, "This is a brand new queue item");

        customPriorityQueue.addToEnd(brandNewQueueItem);

        QueueNode<QueueItem> brandNewNode = customPriorityQueue.getLastNodeByPriority(queueItem.getPriority());
        assertEquals(brandNewQueueItem, brandNewNode.getQueueItem());
        assertNull(brandNewNode.getNextItem());
        assertEquals(newestQueueItem, brandNewNode.getPreviousItem().getQueueItem());
        assertEquals(customPriorityQueue.getHeaderNodeElement(), customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(brandNewNode, customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }

    @Test
    public void should_add_before_existing_node() {
        QueueItem<String> newQueueItem =
                new QueueItem<>(2, "This is a new higher priority queue item");
        QueueItem<String> newerQueueItem =
                new QueueItem<>(4, "This is newer higher priority queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newQueueItem);
        customPriorityQueue.addToEnd(newerQueueItem);

        QueueItem<String> newestQueueItem = new QueueItem<>(3, "This is newest queue item");
        customPriorityQueue.addBeforeNode(customPriorityQueue.getFirstNodeByPriority(4), newestQueueItem);

        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(3);

        assertEquals(newestQueueItem, queueNode.getQueueItem());
        assertEquals(newQueueItem, queueNode.getPreviousItem().getQueueItem());
        assertEquals(newerQueueItem, queueNode.getNextItem().getQueueItem());
        assertEquals(queueNode, queueNode.getPreviousItem().getNextItem());
        assertEquals(queueNode, queueNode.getNextItem().getPreviousItem());
    }

    @Test
    public void should_add_before_header_node() {
        customPriorityQueue.addToFront(queueItem);
        assertFalse(customPriorityQueue.isEmpty());

        QueueItem<String> newerQueueItem = new QueueItem<>(2, "This is newer queue item");
        customPriorityQueue.addBeforeNode(customPriorityQueue.getHeaderNodeElement(), newerQueueItem);

        QueueNode<QueueItem> queueNode = customPriorityQueue.getHeaderNodeElement();
        assertEquals(newerQueueItem, queueNode.getQueueItem());
        assertEquals(queueItem, queueNode.getNextItem().getQueueItem());
        assertNull(queueNode.getPreviousItem());
    }

    @Test
    public void should_do_nothing_no_elements_in_queue_for_add_before_node_method() {
        QueueNode<QueueItem> queueNode = new QueueNode<>(queueItem);
        QueueItem<String> addAfterItem = new QueueItem<>(1, "This is the newest queue item");

        customPriorityQueue.addBeforeNode(queueNode, addAfterItem);

        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_do_nothing_no_node_to_add_for_add_before_node_method() {
        customPriorityQueue.addToFront(queueItem);
        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);

        customPriorityQueue.addBeforeNode(queueNode, null);

        assertNull(queueNode.getNextItem());
        assertNull(queueNode.getPreviousItem());
    }

    @Test
    public void should_do_nothing_existing_elements_not_passed_for_add_before_node_method() {
        customPriorityQueue.addToFront(queueItem);
        QueueItem<String> addAfterItem = new QueueItem<>(1, "This is the newest queue item");

        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);

        customPriorityQueue.addBeforeNode(null, addAfterItem);

        assertNull(queueNode.getNextItem());
        assertNull(queueNode.getPreviousItem());
    }

    @Test
    public void should_add_after_existing_node() {
        customPriorityQueue.addToFront(queueItem);
        QueueItem<String> newerQueueItem = new QueueItem<>(2, "This is newer queue item");
        customPriorityQueue.addToEnd(newerQueueItem);

        QueueItem<String> addAfterItem = new QueueItem<>(1, "This is the newest queue item");
        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);
        customPriorityQueue.addAfterNode(queueNode, addAfterItem);

        QueueNode<QueueItem> addedAfterHeader = customPriorityQueue.getHeaderNodeElement().getNextItem();
        assertEquals(addAfterItem, addedAfterHeader.getQueueItem());
        assertEquals(queueItem, addedAfterHeader.getPreviousItem().getQueueItem());
        assertEquals(newerQueueItem, addedAfterHeader.getNextItem().getQueueItem());
        assertEquals(addedAfterHeader, addedAfterHeader.getPreviousItem().getNextItem());
        assertEquals(addedAfterHeader, addedAfterHeader.getNextItem().getPreviousItem());
        assertEquals(addedAfterHeader.getPreviousItem(), customPriorityQueue.getFirstNodeByPriority(queueItem.getPriority()));
        assertEquals(addedAfterHeader, customPriorityQueue.getLastNodeByPriority(queueItem.getPriority()));
    }


    @Test
    public void should_add_after_node_with_a_new_priority() {
        customPriorityQueue.addToFront(queueItem);
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is newer queue item");
        customPriorityQueue.addToEnd(newerQueueItem);

        QueueItem<String> addAfterItem = new QueueItem<>(2, "This is the newest queue item");
        QueueNode<QueueItem> queueNode = customPriorityQueue.getLastNodeByPriority(1);
        customPriorityQueue.addAfterNode(queueNode, addAfterItem);

        QueueNode<QueueItem> addedAfterHeader = queueNode.getNextItem();
        assertEquals(addAfterItem, addedAfterHeader.getQueueItem());
        assertEquals(newerQueueItem, addedAfterHeader.getPreviousItem().getQueueItem());
        assertNull(addedAfterHeader.getNextItem());
        assertEquals(addedAfterHeader, customPriorityQueue.getFirstNodeByPriority(addAfterItem.getPriority()));
        assertEquals(addedAfterHeader, customPriorityQueue.getLastNodeByPriority(addAfterItem.getPriority()));
    }

    @Test
    public void should_do_nothing_no_elements_in_queue_for_add_after_node_method() {
        QueueNode<QueueItem> queueNode = new QueueNode<>(queueItem);
        QueueItem<String> addAfterItem = new QueueItem<>(1, "This is the newest queue item");

        customPriorityQueue.addAfterNode(queueNode, addAfterItem);

        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_do_nothing_no_node_to_add_for_add_after_node_method() {
        customPriorityQueue.addToFront(queueItem);
        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);

        customPriorityQueue.addAfterNode(queueNode, null);

        assertNull(queueNode.getNextItem());
        assertNull(queueNode.getPreviousItem());
    }

    @Test
    public void should_do_nothing_existing_elements_not_passed_for_add_after_node_method() {
        customPriorityQueue.addToFront(queueItem);
        QueueItem<String> addAfterItem = new QueueItem<>(1, "This is the newest queue item");

        QueueNode<QueueItem> queueNode = customPriorityQueue.getFirstNodeByPriority(1);

        customPriorityQueue.addAfterNode(null, addAfterItem);

        assertNull(queueNode.getNextItem());
        assertNull(queueNode.getPreviousItem());
    }

    @Test
    public void should_remove_header_node() {
        QueueItem<String> newerQueueItem = new QueueItem<>(1, "This is newer queue item");
        QueueItem<String> newestQueueItem = new QueueItem<>(1, "This is the newest queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(newestQueueItem);
        assertFalse(customPriorityQueue.isEmpty());

        QueueNode<QueueItem> headerNode = customPriorityQueue.getHeaderNodeElement();
        customPriorityQueue.dequeue(headerNode);

        QueueNode<QueueItem> newHeaderNode = customPriorityQueue.getHeaderNodeElement();
        assertNull(newHeaderNode.getPreviousItem());
        assertEquals(newerQueueItem, newHeaderNode.getQueueItem());
        assertEquals(newHeaderNode, customPriorityQueue.getFirstNodeByPriority(1));
    }

    @Test
    public void should_remove_last_node_and_enqueueItem_a_new_node() {
        customPriorityQueue.addToEnd(queueItem);
        assertFalse(customPriorityQueue.isEmpty());

        QueueNode<QueueItem> headerNode = customPriorityQueue.getHeaderNodeElement();
        customPriorityQueue.dequeue(headerNode);

        assertTrue(customPriorityQueue.isEmpty());
        assertNull(customPriorityQueue.getHeaderNodeElement());
        assertNull(customPriorityQueue.getHighestPriority());

        customPriorityQueue.enqueue(queueItem);
        assertFalse(customPriorityQueue.isEmpty());
        assertEquals(queueItem, customPriorityQueue.getHeaderNodeElement().getQueueItem());
        assertEquals(customPriorityQueue.getHeaderNodeElement(), customPriorityQueue.getFirstNodeByPriority(1));
    }

    @Test
    public void should_remove_node_with_multiple_elements() {
        QueueItem<String> newQueueItem =
                new QueueItem<>(2, "This is a new higher priority queue item");
        QueueItem<String> newerQueueItem =
                new QueueItem<>(2, "This is newer higher priority queue item");
        QueueItem<String> newestQueueItem =
                new QueueItem<>(2, "This is the newest higher priority queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newQueueItem);
        customPriorityQueue.addToEnd(newerQueueItem);
        customPriorityQueue.addToEnd(newestQueueItem);
        assertFalse(customPriorityQueue.isEmpty());

        QueueNode<QueueItem> firstElementForSecondPrio = customPriorityQueue.getFirstNodeByPriority(2);
        customPriorityQueue.dequeue(firstElementForSecondPrio);

        QueueNode<QueueItem> newFirstElementForSecondPrio =
                customPriorityQueue.getFirstNodeByPriority(2);

        assertEquals(queueItem, newFirstElementForSecondPrio.getPreviousItem().getQueueItem());
        assertEquals(newestQueueItem, newFirstElementForSecondPrio.getNextItem().getQueueItem());
        assertEquals(newerQueueItem, newFirstElementForSecondPrio.getQueueItem());
        assertEquals(newFirstElementForSecondPrio, customPriorityQueue.getFirstNodeByPriority(2));
        assertEquals(newFirstElementForSecondPrio.getNextItem(), customPriorityQueue.getLastNodeByPriority(2));
    }

    @Test
    public void should_remove_node_with_single_element() {
        QueueItem<String> newQueueItem =
                new QueueItem<>(2, "This is a new higher priority queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(newQueueItem);
        assertFalse(customPriorityQueue.isEmpty());

        QueueNode<QueueItem> firstElementForSecondPrio = customPriorityQueue.getFirstNodeByPriority(2);
        customPriorityQueue.dequeue(firstElementForSecondPrio);

        QueueNode<QueueItem> newFirstElementForSecondPrio =
                customPriorityQueue.getFirstNodeByPriority(2);

        assertNull(newFirstElementForSecondPrio);
        assertNull(customPriorityQueue.getHeaderNodeElement().getNextItem());
        assertNull(customPriorityQueue.getFirstNodeByPriority(2));
        assertNull(customPriorityQueue.getLastNodeByPriority(2));
    }

    @Test
    public void should_do_nothing_no_elements_in_queue_for_remove_node_method() {
        QueueNode<QueueItem> queueNode = new QueueNode<>(queueItem);

        customPriorityQueue.dequeue(queueNode);

        assertTrue(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_do_nothing_no_node_to_remove_for_remove_node_method() {
        customPriorityQueue.addToFront(queueItem);

        customPriorityQueue.dequeue(null);

        assertFalse(customPriorityQueue.isEmpty());
    }

    @Test
    public void should_return_null_for_get_highest_priority_method() {
        assertNull(customPriorityQueue.getHighestPriority());
    }

    @Test
    public void should_return_highest_priority_get_highest_priority_method() {
        QueueItem<String> queueItemLevel2 =
                new QueueItem<>(2, "This is a new higher priority queue item");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(queueItemLevel2);

        int highestPriority = customPriorityQueue.getHighestPriority();
        assertEquals(1, highestPriority);
    }

    @Test
    public void should_return_true_has_free_capacity_method() {
        customPriorityQueue.addToFront(queueItem);

        assertTrue(customPriorityQueue.hasFreeCapacity());
    }

    @Test
    public void should_return_false_has_free_capacity_method() {
        customPriorityQueue = new CustomPriorityQueue(1, false);

        customPriorityQueue.addToFront(queueItem);

        assertFalse(customPriorityQueue.hasFreeCapacity());
    }

    @Test
    public void should_return_next_available_priority() {
        QueueItem<String> queueItemLevel3 =
                new QueueItem<>(3, "This is a new higher priority queue item level 3");
        QueueItem<String> queueItemLevel5 =
                new QueueItem<>(5, "This is a new higher priority queue item level 5");
        QueueItem<String> queueItemLevel6 =
                new QueueItem<>(6, "This is a new higher priority queue item level 6");
        customPriorityQueue.addToEnd(queueItem);
        customPriorityQueue.addToEnd(queueItemLevel3);
        customPriorityQueue.addToEnd(queueItemLevel5);
        customPriorityQueue.addToEnd(queueItemLevel6);

        int nextPriority = customPriorityQueue.getNextQueuePriority(queueItem.getPriority());
        assertEquals(3, nextPriority);

        nextPriority = customPriorityQueue.getNextQueuePriority(queueItemLevel3.getPriority());
        assertEquals(5, nextPriority);

        nextPriority = customPriorityQueue.getNextQueuePriority(queueItemLevel5.getPriority());
        assertEquals(6, nextPriority);

        nextPriority = customPriorityQueue.getNextQueuePriority(queueItemLevel6.getPriority());
        assertEquals(-1, nextPriority);
    }

    @Test
    public void should_return_next_available_priority_without_list_looping() {
        QueueItem<String> queueItemLevel3 =
                new QueueItem<>(3, "This is a new higher priority queue item level 3");
        QueueItem<String> queueItemLevel5 =
                new QueueItem<>(5, "This is a new higher priority queue item level 5");
        QueueItem<String> queueItemLevel6 =
                new QueueItem<>(6, "This is a new higher priority queue item level 6");
        customPriorityQueue.addToEnd(queueItemLevel3);
        customPriorityQueue.addToEnd(queueItemLevel5);
        customPriorityQueue.addToEnd(queueItemLevel6);

        int nextPriority = customPriorityQueue.getNextQueuePriority(queueItem.getPriority());
        assertEquals(3, nextPriority);
    }

    @Test
    public void should_return_next_null_as_available_priority_queue_is_empty() {
        int nextPriority = customPriorityQueue.getNextQueuePriority(1);
        assertEquals(-1, nextPriority);
    }
}
