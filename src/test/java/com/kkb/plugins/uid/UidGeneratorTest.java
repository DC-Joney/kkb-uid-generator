/*
 * Copyright
 */
package com.kkb.plugins.uid;

import com.kkb.plugins.uid.generator.UidGenerator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for {@link UidGenerator}
 *
 * @author ztkool
 * @since
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UidApplication.class})
public class UidGeneratorTest {
    //    private static final int SIZE = 7000000; // 700w
    private static final int SIZE = 10000; // 1w
    private static final boolean VERBOSE = true;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() << 1;

    @Resource
    private UidGenerator<Long> uidGenerator;

    @Test
    public void test1() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMXBean.getName();
        System.out.println(name);
    }

    @Test
    public void testSerialGenerate() throws IOException {
        Set<Long> uidSet = new HashSet<>(SIZE);
        long start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
            doGenerate(uidSet, i);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        checkUniqueID(uidSet);
    }

    @Test
    public void testParallelGenerate() throws InterruptedException, IOException {
        AtomicInteger control = new AtomicInteger(-1);
        Set<Long> uidSet = new ConcurrentSkipListSet<>();
        List<Thread> threadList = new ArrayList<>(THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(() -> workerRun(uidSet, control));
            thread.setName("UID-generator-" + i);
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        Assert.assertEquals(SIZE, control.get());
        checkUniqueID(uidSet);
    }

    private void workerRun(Set<Long> uidSet, AtomicInteger control) {
        for (; ; ) {
            int myPosition = control.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
            if (myPosition == SIZE) {
                return;
            }
            doGenerate(uidSet, myPosition);
        }
    }

    private void doGenerate(Set<Long> uidSet, int index) {
        Long uid = uidGenerator.get();
        String parsedInfo = uidGenerator.parse(uid);
        boolean existed = !uidSet.add(uid);
        if (existed) {
            System.out.println("Found duplicate UID " + uid);
        }
        Assert.assertTrue(uid > 0L);
        Assert.assertTrue(StringUtils.isNotBlank(parsedInfo));
        if (VERBOSE) {
            System.out.println(Thread.currentThread().getName() + " No." + index + " >>> " + parsedInfo);
        }
    }

    private void checkUniqueID(Set<Long> uidSet) throws IOException {
        System.out.println(uidSet.size());
        Assert.assertEquals(SIZE, uidSet.size());
    }

}
