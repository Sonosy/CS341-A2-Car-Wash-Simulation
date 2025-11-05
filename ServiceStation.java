import java.util.*;

class semaphore {
    protected int value = 0;

    protected semaphore() {
        value = 0;
    }

    protected semaphore(int initial) {
        value = initial;
    }

    public synchronized void P() {
        value--;
        if (value < 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized int getValue() {
        return value;
    }

    public synchronized void V() {
        value++;
        if (value <= 0)
            notify();
    }
}

class SharedResources {
    final Queue<Car> queue = new ArrayDeque<>();
    final int capacity;
    final Semaphore mutex;     // binary mutex for queue access
    final Semaphore empty;     // counts free slots in waiting area
    final Semaphore full;      // counts cars waiting
    final Semaphore pumps;     // counts free service bays

    SharedResources(int capacity, int pumpCount) {
        this.capacity = capacity;
        this.mutex = new Semaphore(1);
        this.empty = new Semaphore(capacity);
        this.full = new Semaphore(0);
        this.pumps = new Semaphore(pumpCount);
    }
}

class Car extends Thread {
    private final String name;
    final SharedResources res;

    Car(String name, SharedResources res) {
        this.name = name;
        this.res = res; 
    }

    public void run() {
        System.out.println(name + " arrived");
        res.empty.P();
        res.mutex.P();
        if (res.pumps.getValue() < 0 ) {
            
        }
        res.queue.add(this);
        System.out.println(name + " entered waiting queue");
        res.mutex.V();
        res.full.V();
    }
}

class Pump extends Thread {
    private final int pumpId;
    final SharedResources res;

    private final int totalCars; // Total cars in simulation

    private static int carsServed = 0; // Shared counter
    private static semaphore counterLock = new semaphore(1); // Protects counter

    Pump(int pumpId, SharedResources res, int totalCars) {
        this.pumpId = pumpId;
        this.res = res;
        this.totalCars = totalCars;
    }

    @Override
    public void run() {
        while (true) {
            res.full.P(); // wait until at least one car is in the queue
            res.mutex.P(); // lock queue to safely remove car

            Car car = res.queue.poll();
            if (car == null) {
                res.mutex.V();
                continue;
            }
            
            res.pumps.P();
            System.out.println("Pump " + pumpId + ": " + car + " login");
            System.out.println("Pump " + pumpId + ": " + car + " begins service at Bay " + pumpId);

            res.mutex.V(); // unlock queue
            res.empty.V(); // free one waiting spot

            // simulate service time
            try {
                Thread.sleep((long) (Math.random() * 2000 + 1000));
            } catch (InterruptedException e) {
            }

            res.pumps.V();
            System.out.println("Pump " + pumpId + ": " + car + " finishes service");
            System.out.println("Pump " + pumpId + ": Bay " + pumpId + " is now free");
            counterLock.P(); // lock before changing the counter
            carsServed++; // increment safely
            boolean done = (carsServed >= totalCars);
            counterLock.V(); // unlock

            if (done) {
                System.out.println("All cars processed; simulation ends");
                System.exit(0);
            }
        }
    }
}

public class ServiceStation {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Waiting area capacity: ");
        int waitingAreaCapacity = scanner.nextInt();

        System.out.print("Number of service bays (pumps): ");
        int numPumps = scanner.nextInt();

        scanner.nextLine();

        System.out.print("Cars arriving (order): ");
        String[] carNames = scanner.nextLine().trim().split("\\s+");
        int numCars = carNames.length;

        SharedResources res = new SharedResources(waitingAreaCapacity, numPumps);
;

        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, res, numCars);
            pump.start();
        }

        for (String carName : carNames) {
            try {
                Thread.sleep(0); // simulate arrival delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Car car = new Car(carName, res);
            car.start();
        }

        scanner.close();
    }
}