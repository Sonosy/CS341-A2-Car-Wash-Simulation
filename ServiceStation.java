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
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized boolean tryP() {
        if (value > 0) {
            value--;
            return true;
        }
        return false;
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
    final int pumpCount;
    final semaphore mutex; // protects queue + pump check
    final semaphore empty; // waiting area spaces
    final semaphore full; // cars waiting to be served
    final semaphore pumps; // available pumps

    SharedResources(int capacity, int pumpCount) {
        this.capacity = capacity;
        this.mutex = new semaphore(1);
        this.empty = new semaphore(capacity);
        this.full = new semaphore(0);
        this.pumps = new semaphore(pumpCount);
        this.pumpCount = pumpCount;
    }
}

class Car extends Thread {
    public final String name;
    final SharedResources res;

    Car(String name, SharedResources res) {
        this.name = name;
        this.res = res;
    }

    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " arrived");

        res.mutex.P();
        if (res.queue.isEmpty() && res.pumps.tryP()) {
            res.queue.add(this);
            res.mutex.V();
            res.full.V();
        } else {
            res.mutex.V();
            res.empty.P();
            res.mutex.P();
            res.queue.add(this);
            System.out.println(name + " arrived and waiting");
            res.mutex.V();
            res.full.V();
        }
    }
}

class Pump extends Thread {
    private final int pumpId;
    final SharedResources res;
    private final int totalCars;

    private static int carsServed = 0;
    private static final semaphore counterLock = new semaphore(1);
    private static volatile boolean simulationDone = false;

    Pump(int pumpId, SharedResources res, int totalCars) {
        this.pumpId = pumpId;
        this.res = res;
        this.totalCars = totalCars;
    }

    public void run() {
        while (true) {
            if (simulationDone)
                return; // exit immediately if simulation ended

            res.full.P(); // wait for a car
            if (simulationDone)
                return; // recheck after waking up

            res.mutex.P();
            Car car = res.queue.poll();
            res.mutex.V();

            if (car == null)
                continue;

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Pump " + pumpId + ": " + car.name + " Occupied");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Pump " + pumpId + ": " + car.name + " login");
            System.out.println("Pump " + pumpId + ": " + car.name + " begins service at Bay " + pumpId);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Pump " + pumpId + ": " + car.name + " finishes service");
            System.out.println("Pump " + pumpId + ": Bay " + pumpId + " is now free");
 
            res.pumps.V(); // mark pump free
            res.empty.V();

            counterLock.P();
            carsServed++;
            boolean done = (carsServed >= totalCars);
            counterLock.V();

            if (done && !simulationDone) {
                simulationDone = true;
                System.out.println("All cars processed; simulation ends");

                for (int i = 0; i < res.pumpCount; i++) {
                    res.full.V();
                }
            }
        }
    }
}

public class ServiceStation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Waiting area capacity: ");
        int waitingAreaCapacity = scanner.nextInt();
        if (waitingAreaCapacity < 1) {
            waitingAreaCapacity = 1;
            System.out.print("Least capacity is 1. Setting to 1.\n");

        }
        if (waitingAreaCapacity > 10)
           { waitingAreaCapacity = 10;
            System.out.print("Maximum capacity is 10. Setting to 10.\n");}


        System.out.print("Number of service bays (pumps): ");
        int numPumps = scanner.nextInt();

        scanner.nextLine();
        System.out.print("Cars arriving (order): ");
        String[] carNames = scanner.nextLine().trim().split("\\s+");
        int numCars = carNames.length;

        SharedResources res = new SharedResources(waitingAreaCapacity, numPumps);

        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, res, numCars);
            pump.start();
        }

        for (String carName : carNames) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            new Car(carName, res).start();
        }

        scanner.close();

    }
}
