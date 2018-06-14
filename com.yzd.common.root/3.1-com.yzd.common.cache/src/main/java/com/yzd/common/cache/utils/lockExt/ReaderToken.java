package com.yzd.common.cache.utils.lockExt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class ReaderToken {

    private CountDownLatch downLatch = new CountDownLatch(1);
    private AtomicLong accessCount = new AtomicLong(0);

    public CountDownLatch getDownLatch() {
        return downLatch;
    }

    public void setDownLatch(CountDownLatch downLatch) {
        this.downLatch = downLatch;
    }

    public AtomicLong getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(AtomicLong accessCount) {
        this.accessCount = accessCount;
    }
}
