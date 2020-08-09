package com.rimidalv.priorityqueue.domain;

public class QueueNode<QueueItem> {
    private QueueItem queueItem;
    private QueueNode<QueueItem> previousItem;
    private QueueNode<QueueItem> nextItem;

    public QueueNode(QueueItem queueNode) {
        this(queueNode, null, null);
    }

    public QueueNode(QueueItem queueNode, QueueNode<QueueItem> previousItem, QueueNode<QueueItem> nextItem) {
        this.queueItem = queueNode;
        this.previousItem = previousItem;
        this.nextItem = nextItem;
    }

    public QueueItem getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(QueueItem queueItem) {
        this.queueItem = queueItem;
    }

    public QueueNode<QueueItem> getPreviousItem() {
        return previousItem;
    }

    public void setPreviousItem(QueueNode<QueueItem> previousItem) {
        this.previousItem = previousItem;
    }

    public QueueNode<QueueItem> getNextItem() {
        return nextItem;
    }

    public void setNextItem(QueueNode<QueueItem> nextItem) {
        this.nextItem = nextItem;
    }

}
