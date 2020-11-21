/*

 * TestScheduler.java

 */


public class TestScheduler

{

    public static void main(String args[]) {

/**

 * This must run at the highest priority to ensure that

 * it can create the scheduler and the example threads.

 * If it did not run at the highest priority, it is possible

 * that the scheduler could preempt this and not allow it to

 * create the example threads.

 */

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        Scheduler CPUScheduler = new Scheduler();

        CPUScheduler.start();

        TestThread t1 = new TestThread("Thread 1");

        t1.start();

        CPUScheduler.addThread(t1);

        TestThread t2 = new TestThread("Thread 2");

        t2.start();

        CPUScheduler.addThread(t2);

        TestThread t3 = new TestThread("Thread 3");

        t3.start();

        CPUScheduler.addThread(t3);

    }

}