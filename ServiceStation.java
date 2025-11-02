import java.util.*;

class semaphore {
    protected int value = 0;
    protected semaphore() { value = 0; }
    protected semaphore(int initial) { value = initial; }

    public synchronized void P() {
        value--;
        if (value < 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
    }

    public synchronized void V() {
        value++;
        if (value <= 0) notify();
    }
}

class Car extends Thread {
    private final String name;
    private final Queue<String> queue;
    private final semaphore mutex, empty, full;

    Car(String name, Queue<String> queue, semaphore mutex, semaphore empty, semaphore full) {
        this.name = name;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
    }

    public void run() {
        System.out.println(name + " arrived");
        empty.P();
        mutex.P();
        queue.add(name);
        System.out.println(name + " entered waiting queue");
        mutex.V();
        full.V();
    }
}
