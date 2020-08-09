package com.rimidalv.priorityqueue.domain;

import java.util.Objects;

public class QueueItem<T> {
    private T queueItem;
    private int priority;

    public QueueItem(int priority, T queueItem) {
        this.queueItem = queueItem;
        this.priority = priority;
    }

    public T getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(T queueItem) {
        this.queueItem = queueItem;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueItem<?> queueItem1 = (QueueItem<?>) o;
        return priority == queueItem1.priority &&
                Objects.equals(queueItem, queueItem1.queueItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueItem, priority);
    }
}
