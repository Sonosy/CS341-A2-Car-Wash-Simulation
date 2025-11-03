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

class Pump extends Thread {
    private final int pumpId;                     
    private final Queue<String> queue;            // Shared waiting queue
    private final semaphore mutex, empty, full;   // Shared semaphores
    private final int totalCars;                  // Total cars in simulation

    private static int carsServed = 0;            // Shared counter
    private static semaphore counterLock = new semaphore(1); // Protects counter

    Pump(int pumpId, Queue<String> queue, semaphore mutex, semaphore empty, semaphore full, int totalCars) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.totalCars = totalCars;
    }

    @Override
    public void run() {
        while (true) {
            full.P();     // wait until at least one car is in the queue
            mutex.P();    // lock queue to safely remove car

            String car = queue.poll();
            if (car == null) {
                mutex.V();
                continue;
            }

            System.out.println("Pump " + pumpId + ": " + car + " login");
            System.out.println("Pump " + pumpId + ": " + car + " begins service at Bay " + pumpId);

            mutex.V();    // unlock queue
            empty.V();    // free one waiting spot

            // simulate service time
            try {
                Thread.sleep((long)(Math.random() * 2000 + 1000));
            } catch (InterruptedException e) {}

            System.out.println("Pump " + pumpId + ": " + car + " finishes service");
            System.out.println("Pump " + pumpId + ": Bay " + pumpId + " is now free");
            counterLock.P();           // lock before changing the counter
            carsServed++;              // increment safely
            boolean done = (carsServed >= totalCars);
            counterLock.V();           // unlock
            
            if (done) {
                System.out.println("All cars processed; simulation ends");
                break; 
            }
        }
    }
}

