package com.rimidalv.priorityqueue.utility;

import com.rimidalv.priorityqueue.domain.QueueItem;
import com.rimidalv.priorityqueue.domain.QueueNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomPriorityQueue {
    private QueueNode<QueueItem> headerElement;
    private Map<Integer, FirstLastPriorityItem> availablePriorities;

    private static final Logger LOGGER = Logger.getLogger( CustomPriorityQueue.class.getName() );
    private Map<Integer, Integer> visitedPriorities;

    private int capacity;
    private int maximumCapacity;
    private boolean inDepthSearch = false;

    public CustomPriorityQueue() {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(("application.properties"));
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.maximumCapacity = Integer.valueOf(properties.getProperty("maximum-queue-capacity"));
            this.inDepthSearch = Boolean.valueOf(properties.getProperty("in-depth-search"));
        }
        else {
            this.maximumCapacity = 11;
        }

        this.availablePriorities = new ConcurrentHashMap<>(new TreeMap<>());
        this.visitedPriorities = new ConcurrentHashMap<>(new TreeMap<>());
    }

    public CustomPriorityQueue(int maximumCapacity, boolean inDepthSearch) {
        this.maximumCapacity = maximumCapacity;
        this.inDepthSearch = inDepthSearch;
        this.availablePriorities = new ConcurrentHashMap<>(new TreeMap<>());
        this.visitedPriorities = new ConcurrentHashMap<>(new TreeMap<>());
    }

    /**
     * Method to enqueue an item. Inserts the item to the queue
     * depending on the priority level. Higher priority items will be placed
     * at the beginning of the queue, lower priority at the end.
     * If the queue does not contain an item with the specified priority, it will be
     * inserted in required position, otherwise will be placed at the end of the same
     * priority level.
     *
     * @param newItem newItem
     */
    public synchronized void enqueue(QueueItem newItem) {
        if (capacity == maximumCapacity) return;
        if (this.isEmpty()) {
            this.addToFront(newItem);
        } else if (availablePriorities.keySet().contains(newItem.getPriority())) {
            this.addAfterNode(getLastNodeByPriority(newItem.getPriority()), newItem);
        } else {
            if (headerElement.getQueueItem().getPriority() > newItem.getPriority()) {
                this.addToFront(newItem);
            } else {
                QueueNode<QueueItem> insertBeforeNode = null;
                for (Map.Entry<Integer, FirstLastPriorityItem> property : availablePriorities.entrySet()) {
                    if (property.getKey() > newItem.getPriority()) {
                        insertBeforeNode = property.getValue().firstNodeElement;
                        break;
                    }
                }
                if (insertBeforeNode != null) {
                    this.addBeforeNode(insertBeforeNode, newItem);
                } else {
                    this.addToEnd(newItem);
                }
            }
        }
    }

    /**
     * Recursive method to trigger the burst approach. Called by dequeue method.
     * This method must be synchronized as it is recursively executed.
     * Returns dequeued queue item if exists, otherwise null.
     *
     */
    public synchronized QueueItem dequeue() {
        int priority = getNextPriority();

        QueueNode<QueueItem> firstPriorityNode =
                this.getFirstNodeByPriority(priority);

        if (firstPriorityNode == null) return null;

        QueueItem queueItem = firstPriorityNode.getQueueItem();
        LOGGER.log( Level.INFO, "Dequeuing item for priority {0}, item: {1}",
                new Object[] {priority, queueItem.getQueueItem()} );

        this.dequeue(firstPriorityNode);

        return queueItem;
    }

    /**
     * Method to dequeue a specified node.
     * Removed an element from the queue and changes the link of the previous
     * and next elements in the queue to refer to each other.
     * Updates the priorities list to have the new references of the nodes.
     *
     * @param removeNode
     */
    public synchronized void dequeue(QueueNode<QueueItem> removeNode) {
        if (this.isEmpty() || removeNode == null) return;

        if (headerElement.equals(removeNode)) {
            if (headerElement.getNextItem() != null) {
                headerElement = headerElement.getNextItem();
                headerElement.setPreviousItem(null);
            } else {
                headerElement = null;
            }
            this.updateAvailablePrioritiesForRemoveMethod(removeNode);
            capacity--;
            return;
        }

        if (removeNode.getNextItem() != null)
            removeNode.getNextItem().setPreviousItem(removeNode.getPreviousItem());
        removeNode.getPreviousItem().setNextItem(removeNode.getNextItem());

        this.updateAvailablePrioritiesForRemoveMethod(removeNode);
        capacity--;
    }

    /**
     * Method checks if the queue has a free space to put a new element.
     * If the queue is out of space returns false, otherwise true.
     *
     * @return boolean
     */
    public synchronized boolean hasFreeCapacity() {
        return maximumCapacity > capacity;
    }

    /**
     * Methods checks if the queue contains an element.
     * returns false if the queue contains element.
     *
     * @return boolean
     */
    public synchronized boolean isEmpty() {
        return capacity == 0;
    }

    /**
     * Methods returns the highest priority in the queue.
     * If the queue does not have an element in the queue returns null,
     * otherwise the highest priority in the queue.
     *
     * @return Integer
     */
    public synchronized Integer getHighestPriority() {
        if (this.isEmpty()) return null;
        return headerElement.getQueueItem().getPriority();
    }

    /**
     * Methods returns the first node of the specified priority.
     * If the queue does not have the element of the specified priority
     * returns null.
     *
     * @param priority
     * @return
     */
    public synchronized QueueNode<QueueItem> getFirstNodeByPriority(int priority) {
        if (availablePriorities.containsKey(priority)) {
            return availablePriorities.get(priority).firstNodeElement;
        } else {
            return null;
        }
    }

    /**
     * Method gets the next dequeueing priority with respect of the burst rate.
     * If there is no priority visited twice, takes the highest priority from the queue.
     * If there is an element in the queue ready to be processed, return its priority,
     * Otherwise -1.
     *
     * @return
     */
    private synchronized int getNextPriority() {
        int visitedTwiceElement = this.hasElementVisitedTwice();
        if (visitedTwiceElement != -1) {
            return this.getNextPriorityExecution(visitedTwiceElement);
        } else {
            Integer highestPriority = this.getHighestPriority();
            if (highestPriority == null) return -1;

            return this.getNextPriorityExecution(highestPriority);
        }
    }
    /**
     * This methods checks if the specified priority was called enough times to enable the burst rate condition.
     *
     * @param priority
     * @return
     */
    private synchronized boolean isBurstRate(int priority) {
        if (!this.visitedPriorities.containsKey(priority)) return false;

        return this.visitedPriorities.get(priority) == 2;
    }

    /**
     * Method recursively finds the lowest priority in the visited list,
     * updates the visited counter, and returns its value.
     *
     * @param priority
     * @return
     */
    private synchronized int getNextPriorityExecution(int priority) {
        if (this.isBurstRate(priority)) {
            this.incrementVisitedPriority(priority);
            if (!this.inDepthSearch) {
                priority = priority + 1;
                return this.getNextPriorityExecution(priority);
            } else {
                int nextPriority = this.getNextQueuePriority(priority);
                if (nextPriority > 0)
                    priority = nextPriority;
                return this.getNextPriorityExecution(priority);
            }
        } else {
            this.incrementVisitedPriority(priority);
            return priority;
        }
    }

    /**
     * Methods returns the next highest available priority in the queue.
     * If the specified priority is the lowest in the queue or the queue
     * is empty - returns null.
     *
     * @param priority
     * @return QueueNode<QueueItem>
     */
    synchronized int getNextQueuePriority(int priority) {
        if (this.isEmpty()) return -1;

        if (headerElement.getQueueItem().getPriority() > priority) {
            return headerElement.getQueueItem().getPriority();
        } else {
            if (availablePriorities.containsKey(priority + 1)) {
                return priority + 1;
            } else {
                for (int availablePriority: availablePriorities.keySet()) {
                    if (priority < availablePriority) {
                        return availablePriority;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Returns the set of the visited priorities.
     * @return
     */
    Map<Integer, Integer> getVisitedPrioritesList() {
        return this.visitedPriorities;
    }

    /**
     * This methods stores information about visited priorities of the queue.
     * Used to enable burst rate
     *
     * @param priority
     */
    private synchronized void incrementVisitedPriority(int priority) {
        if (this.visitedPriorities.containsKey(priority)) {
            int timesVisited = this.visitedPriorities.get(priority);
            if (timesVisited < 2) {
                this.visitedPriorities.put(priority, timesVisited + 1);
            } else {
                this.visitedPriorities.put(priority, 0);
            }
        } else {
            this.visitedPriorities.put(priority, 1);
        }
    }
    /**
     * Method checks if the visited priorities map contains a priority visited twice.
     * If finds the value, returns the key of priority, otherwise -1
     *
     * @return
     */
    private synchronized int hasElementVisitedTwice() {
        for (Map.Entry<Integer, Integer> prioritySet : this.visitedPriorities.entrySet()) {
            if (prioritySet.getValue() == 2)
                return prioritySet.getKey();
        }
        return -1;
    }


    /**
     * Methods adds the element to the beginning of the queue and updates the
     * available list to the inserted node.
     *
     * @param newItem
     */
    void addToFront(QueueItem newItem) {
        if (this.isEmpty())
            headerElement = new QueueNode<>(newItem);
        else {
            QueueNode<QueueItem> temp = headerElement;
            headerElement = new QueueNode<>(newItem, null, temp);
            headerElement.getNextItem().setPreviousItem(headerElement);
        }

        if (availablePriorities.containsKey(newItem.getPriority())) {
            availablePriorities.get(newItem.getPriority()).firstNodeElement = headerElement;
        } else {
            availablePriorities.put(newItem.getPriority(), new FirstLastPriorityItem(headerElement, headerElement));
        }

        capacity++;
    }

    /**
     * Methods adds the element to the end of the queue and updates the
     * available list to the inserted node.
     *
     * @param newItem
     */
    void addToEnd(QueueItem newItem) {
        QueueNode<QueueItem> temp = null;
        if (this.isEmpty())
            headerElement = new QueueNode<>(newItem);
        else {
            temp = headerElement;
            while (temp.getNextItem() != null) {
                temp = temp.getNextItem();
            }
            temp.setNextItem(new QueueNode<>(newItem, temp, null));
        }

        QueueNode<QueueItem> lastElement = (temp == null) ? headerElement : temp.getNextItem();
        if (availablePriorities.containsKey(newItem.getPriority())) {
            availablePriorities.get(newItem.getPriority()).lastNodeElement = lastElement;
        } else {
            availablePriorities.put(newItem.getPriority(), new FirstLastPriorityItem(lastElement, lastElement));
        }
        capacity++;

    }

    /**
     * Methods adds the element in front of the specified node and updates the
     * available list to the inserted node. Takes a node and a queue item as parameters.
     *
     * @param addBeforeNode
     * @param newItem
     */
    void addBeforeNode(QueueNode<QueueItem> addBeforeNode, QueueItem newItem) {
        if (this.isEmpty() || addBeforeNode == null || newItem == null || availablePriorities.containsKey(newItem.getPriority())) return;

        QueueNode<QueueItem> newNode = new QueueNode<>(newItem, addBeforeNode.getPreviousItem(), addBeforeNode);
        if (addBeforeNode.getPreviousItem() != null) {
            newNode.setPreviousItem(addBeforeNode.getPreviousItem());
            newNode.getPreviousItem().setNextItem(newNode);
        }
        else
            headerElement = newNode;

        addBeforeNode.setPreviousItem(newNode);

        availablePriorities.put(newItem.getPriority(), new FirstLastPriorityItem(newNode, newNode));
        capacity++;
    }

    /**
     * Methods adds the element after the specified node and updates the
     * available list to the inserted node. Takes a node and a queue item as parameters.
     *
     * @param addAfterNode
     * @param newItem
     */
    void addAfterNode(QueueNode<QueueItem> addAfterNode, QueueItem newItem) {
        if (this.isEmpty() || addAfterNode == null || newItem == null) return;

        QueueNode<QueueItem> newNode = new QueueNode<>(newItem, addAfterNode, addAfterNode.getNextItem());
        if (addAfterNode.getNextItem() != null)
            addAfterNode.getNextItem().setPreviousItem(newNode);
        addAfterNode.setNextItem(newNode);

        if (availablePriorities.containsKey(newItem.getPriority())) {
            availablePriorities.get(newItem.getPriority()).lastNodeElement = newNode;
        } else {
            availablePriorities.put(newItem.getPriority(), new FirstLastPriorityItem(newNode, newNode));
        }
        capacity++;
    }

    /**
     * Methods works with the dequeue method. Updates the list of the available list.
     *
     * @param removeNode
     */
    private void updateAvailablePrioritiesForRemoveMethod(QueueNode<QueueItem> removeNode) {
        FirstLastPriorityItem firstLastPriorityItem =
                availablePriorities.get(removeNode.getQueueItem().getPriority());
        if (firstLastPriorityItem.firstNodeElement.equals(firstLastPriorityItem.lastNodeElement)) {
            availablePriorities.remove(removeNode.getQueueItem().getPriority());
        } else {
            firstLastPriorityItem.firstNodeElement = removeNode.getNextItem();
        }
    }

    /**
     * Methods returns the last of the specified priority.
     * If the queue does not have the element of the specified priority
     * returns null.
     *
     * @param priority
     * @return QueueNode<QueueItem>
     */
    QueueNode<QueueItem> getLastNodeByPriority(int priority) {
        if (availablePriorities.containsKey(priority)) {
            return availablePriorities.get(priority).lastNodeElement;
        } else {
            return null;
        }
    }

    /**
     * Methods return the header element (first element) of the queue
     * @return
     */
    QueueNode<QueueItem> getHeaderNodeElement() {
        return this.headerElement;
    }

    /**
     * Inner class for the available list. Contains the first and the last
     * node of the queue for a given priority.
     */
    private class FirstLastPriorityItem {
        private QueueNode<QueueItem> firstNodeElement;
        private QueueNode<QueueItem> lastNodeElement;

        FirstLastPriorityItem(QueueNode<QueueItem> firstNodeElement, QueueNode<QueueItem> lastNodeElement) {
            this.firstNodeElement = firstNodeElement;
            this.lastNodeElement = lastNodeElement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FirstLastPriorityItem that = (FirstLastPriorityItem) o;
            return Objects.equals(firstNodeElement, that.firstNodeElement) &&
                    Objects.equals(lastNodeElement, that.lastNodeElement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstNodeElement, lastNodeElement);
        }
    }
}
