import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

// Semaphore class
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

// Shared Resources
class SharedResources {
    final Queue<Car> queue = new ArrayDeque<>();
    final int capacity;
    final int pumpCount;
    final semaphore mutex;
    final semaphore empty;
    final semaphore full;
    final semaphore pumps;
    
    // GUI callback
    SimulationGUI gui;

    SharedResources(int capacity, int pumpCount) {
        this.capacity = capacity;
        this.pumpCount = pumpCount;
        this.mutex = new semaphore(1);
        this.empty = new semaphore(capacity);
        this.full = new semaphore(0);
        this.pumps = new semaphore(pumpCount);
    }
    
    void setGUI(SimulationGUI gui) {
        this.gui = gui;
    }
}

// Car class (Producer)
class Car extends Thread {
    public final String name;
    final SharedResources res;
    private volatile boolean running = true;

    Car(String name, SharedResources res) {
        this.name = name;
        this.res = res;
    }

    public void stopCar() {
        running = false;
        this.interrupt();
    }

    public void run() {
        try {
            if (!running) return;
            Thread.sleep((long)(1000 / res.gui.getSpeedMultiplier()));
        } catch (InterruptedException e) {
            if (!running) return;
        }
        
        if (res.gui != null) {
            res.gui.logEvent(name + " arrived");
        }

        res.mutex.P();
        if (!running) {
            res.mutex.V();
            return;
        }
        
        if (res.queue.isEmpty() && res.pumps.tryP()) {
            res.queue.add(this);
            res.mutex.V();
            res.full.V();
        } else {
            res.mutex.V();
            res.empty.P();
            
            if (!running) return;
            
            res.mutex.P();
            res.queue.add(this);
            if (res.gui != null) {
                res.gui.logEvent(name + " entered waiting area");
                res.gui.updateWaitingArea();
            }
            res.mutex.V();
            res.full.V();
        }
    }
}

// Pump class (Consumer)
class Pump extends Thread {
    private final int pumpId;
    final SharedResources res;
    private final int totalCars;
    private volatile boolean running = true;

    private static int carsServed = 0;
    private static final semaphore counterLock = new semaphore(1);
    private static volatile boolean simulationDone = false;

    Pump(int pumpId, SharedResources res, int totalCars) {
        this.pumpId = pumpId;
        this.res = res;
        this.totalCars = totalCars;
    }

    public void stopPump() {
        running = false;
        this.interrupt();
    }

    public static void resetCounter() {
        carsServed = 0;
        simulationDone = false;
    }

    public void run() {
        while (running) {
            if (simulationDone) return;

            res.full.P();
            if (simulationDone || !running) return;

            res.mutex.P();
            Car car = res.queue.poll();
            if (res.gui != null) {
                res.gui.updateWaitingArea();
            }
            res.mutex.V();

            if (car == null) continue;

            try {
                Thread.sleep((long)(250 / res.gui.getSpeedMultiplier()));
            } catch (InterruptedException e) {
                if (!running) return;
            }
            
            if (res.gui != null) {
                res.gui.updatePumpStatus(pumpId, car.name, "OCCUPIED");
                res.gui.logEvent("Pump " + pumpId + ": " + car.name + " Occupied");
            }
            
            try {
                Thread.sleep((long)(500 / res.gui.getSpeedMultiplier()));
            } catch (InterruptedException e) {
                if (!running) return;
            }
            
            if (res.gui != null) {
                res.gui.logEvent("Pump " + pumpId + ": " + car.name + " begins service");
            }

            res.empty.V();
            
            try {
                Thread.sleep((long)(1000 / res.gui.getSpeedMultiplier()));
            } catch (InterruptedException e) {
                if (!running) return;
            }

            if (res.gui != null) {
                res.gui.logEvent("Pump " + pumpId + ": " + car.name + " finishes service");
                res.gui.updatePumpStatus(pumpId, null, "FREE");
                res.gui.logEvent("Pump " + pumpId + ": Bay " + pumpId + " is now free");
            }

            res.pumps.V();

            counterLock.P();
            carsServed++;
            boolean done = (carsServed >= totalCars);
            counterLock.V();

            if (done && !simulationDone) {
                simulationDone = true;
                if (res.gui != null) {
                    res.gui.logEvent("All cars processed; simulation ends");
                    res.gui.onSimulationComplete();
                }

                for (int i = 0; i < res.pumpCount; i++) {
                    res.full.V();
                }
            }
        }
    }
}

// GUI Panel for a single Pump
class PumpPanel extends JPanel {
    private JLabel statusLabel;
    private JLabel carLabel;
    private int pumpId;
    
    public PumpPanel(int pumpId) {
        this.pumpId = pumpId;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(200, 200, 200));
        
        JLabel titleLabel = new JLabel("Pump " + pumpId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setOpaque(false);
        
        statusLabel = new JLabel("FREE", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(0, 150, 0));
        
        carLabel = new JLabel("-", SwingConstants.CENTER);
        carLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        centerPanel.add(statusLabel);
        centerPanel.add(carLabel);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    public void updateStatus(String carName, String status) {
        SwingUtilities.invokeLater(() -> {
            if (status.equals("OCCUPIED") && carName != null) {
                statusLabel.setText("OCCUPIED");
                statusLabel.setForeground(new Color(200, 0, 0));
                carLabel.setText(carName);
                setBackground(new Color(255, 200, 200));
            } else {
                statusLabel.setText("FREE");
                statusLabel.setForeground(new Color(0, 150, 0));
                carLabel.setText("-");
                setBackground(new Color(200, 255, 200));
            }
        });
    }
    
    public void reset() {
        updateStatus(null, "FREE");
    }
}

// Main GUI Class
class SimulationGUI extends JFrame {
    private JTextArea logArea;
    private JPanel pumpsPanel;
    private JPanel waitingAreaPanel;
    private JButton startButton, pauseButton, resetButton;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JLabel waitingCountLabel;
    
    private SharedResources resources;
    private java.util.List<Car> cars = new ArrayList<>();
    private java.util.List<Pump> pumps = new ArrayList<>();
    private Map<Integer, PumpPanel> pumpPanels = new HashMap<>();
    
    private boolean simulationRunning = false;
    private boolean simulationPaused = false;
    private double speedMultiplier = 1.0;
    
    // Configuration
    private int waitingAreaCapacity;
    private int numPumps;
    private String[] carNames;
    
    public SimulationGUI() {
        setTitle("Car Wash & Gas Station Simulator");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create menu bar
        createMenuBar();
        
        // Top panel with controls
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Center panel with pumps and waiting area
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Pumps panel
        JPanel pumpsContainer = new JPanel(new BorderLayout());
        pumpsContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2),
            "Service Bays (Pumps)",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        pumpsPanel = new JPanel(new GridLayout(1, 0, 10, 10));
        pumpsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pumpsContainer.add(pumpsPanel, BorderLayout.CENTER);
        centerPanel.add(pumpsContainer, BorderLayout.CENTER);
        
        // Waiting area panel
        JPanel waitingContainer = new JPanel(new BorderLayout());
        waitingContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE, 2),
            "Waiting Area",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        waitingContainer.setPreferredSize(new Dimension(200, 0));
        
        waitingCountLabel = new JLabel("Cars waiting: 0 / 0", SwingConstants.CENTER);
        waitingCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        waitingContainer.add(waitingCountLabel, BorderLayout.NORTH);
        
        waitingAreaPanel = new JPanel();
        waitingAreaPanel.setLayout(new BoxLayout(waitingAreaPanel, BoxLayout.Y_AXIS));
        waitingAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane waitingScroll = new JScrollPane(waitingAreaPanel);
        waitingContainer.add(waitingScroll, BorderLayout.CENTER);
        centerPanel.add(waitingContainer, BorderLayout.EAST);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Activity Log",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        logPanel.setPreferredSize(new Dimension(0, 200));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        add(logPanel, BorderLayout.SOUTH);
        
        // Show configuration dialog
        showConfigDialog();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem configItem = new JMenuItem("New Configuration");
        configItem.addActionListener(e -> showConfigDialog());
        fileMenu.add(configItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(230, 230, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        startButton = new JButton("Start Simulation");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(100, 200, 100));
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startSimulation());
        
        pauseButton = new JButton("Pause");
        pauseButton.setFont(new Font("Arial", Font.BOLD, 14));
        pauseButton.setBackground(new Color(255, 200, 100));
        pauseButton.setEnabled(false);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(e -> togglePause());
        
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setBackground(new Color(255, 150, 150));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetSimulation());
        
        // Speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        speedPanel.setOpaque(false);
        JLabel speedTitleLabel = new JLabel("Speed:");
        speedTitleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        speedSlider = new JSlider(1, 50, 10);
        speedSlider.setPreferredSize(new Dimension(200, 40));
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            speedMultiplier = speedSlider.getValue() / 10.0;
            speedLabel.setText(String.format("%.1fx", speedMultiplier));
        });
        
        speedLabel = new JLabel("1.0x");
        speedLabel.setFont(new Font("Arial", Font.BOLD, 12));
        speedLabel.setPreferredSize(new Dimension(40, 20));
        
        speedPanel.add(speedTitleLabel);
        speedPanel.add(speedSlider);
        speedPanel.add(speedLabel);
        
        panel.add(startButton);
        panel.add(pauseButton);
        panel.add(resetButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(speedPanel);
        
        return panel;
    }
    
    private void showConfigDialog() {
        JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel capacityLabel = new JLabel("Waiting Area Capacity (1-10):");
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        
        JLabel pumpsLabel = new JLabel("Number of Service Bays:");
        JSpinner pumpsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        
        JLabel carsLabel = new JLabel("Car Names (space-separated):");
        JTextField carsField = new JTextField("A B C D E F G H I J");
        
        configPanel.add(capacityLabel);
        configPanel.add(capacitySpinner);
        configPanel.add(pumpsLabel);
        configPanel.add(pumpsSpinner);
        configPanel.add(carsLabel);
        configPanel.add(carsField);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            configPanel,
            "Simulation Configuration",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            waitingAreaCapacity = (Integer) capacitySpinner.getValue();
            numPumps = (Integer) pumpsSpinner.getValue();
            carNames = carsField.getText().trim().split("\\s+");
            
            initializeSimulation();
        }
    }
    
    private void initializeSimulation() {
        // Clear existing
        pumpsPanel.removeAll();
        pumpPanels.clear();
        logArea.setText("");
        
        // Create pump panels
        for (int i = 1; i <= numPumps; i++) {
            PumpPanel pumpPanel = new PumpPanel(i);
            pumpPanels.put(i, pumpPanel);
            pumpsPanel.add(pumpPanel);
        }
        
        // Update waiting area label
        updateWaitingArea();
        
        pumpsPanel.revalidate();
        pumpsPanel.repaint();
        
        logEvent("Configuration loaded: " + numPumps + " pumps, capacity=" + waitingAreaCapacity + ", " + carNames.length + " cars");
    }
    
    private void startSimulation() {
        if (simulationRunning) return;
        
        simulationRunning = true;
        simulationPaused = false;
        Pump.resetCounter();
        
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        
        logEvent("=== Simulation Started ===");
        
        // Create shared resources
        resources = new SharedResources(waitingAreaCapacity, numPumps);
        resources.setGUI(this);
        
        // Create and start pumps
        pumps.clear();
        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, resources, carNames.length);
            pumps.add(pump);
            pump.start();
        }
        
        // Create and start cars with delay
        cars.clear();
        new Thread(() -> {
            for (String carName : carNames) {
                if (!simulationRunning) break;
                
                try {
                    Thread.sleep((long)(50 / speedMultiplier));
                } catch (InterruptedException e) {
                    break;
                }
                
                Car car = new Car(carName, resources);
                cars.add(car);
                car.start();
            }
        }).start();
    }
    
    private void togglePause() {
        simulationPaused = !simulationPaused;
        
        if (simulationPaused) {
            pauseButton.setText("Resume");
            pauseButton.setBackground(new Color(150, 255, 150));
            logEvent("=== Simulation Paused ===");
            
            // Stop all threads
            for (Car car : cars) {
                car.stopCar();
            }
            for (Pump pump : pumps) {
                pump.stopPump();
            }
        } else {
            // Resume is essentially a restart from current state
            pauseButton.setText("Pause");
            pauseButton.setBackground(new Color(255, 200, 100));
            logEvent("=== Simulation Resumed ===");
            // Note: Full resume would require more complex state management
            // For now, user should reset and restart
            JOptionPane.showMessageDialog(this, 
                "To continue, please Reset and Start a new simulation.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void resetSimulation() {
        // Stop all threads
        simulationRunning = false;
        
        for (Car car : cars) {
            car.stopCar();
        }
        for (Pump pump : pumps) {
            pump.stopPump();
        }
        
        cars.clear();
        pumps.clear();
        
        // Reset UI
        for (PumpPanel panel : pumpPanels.values()) {
            panel.reset();
        }
        
        waitingAreaPanel.removeAll();
        waitingAreaPanel.revalidate();
        waitingAreaPanel.repaint();
        updateWaitingArea();
        
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        pauseButton.setText("Pause");
        pauseButton.setBackground(new Color(255, 200, 100));
        
        logEvent("=== Simulation Reset ===");
        
        Pump.resetCounter();
    }
    
    public void updatePumpStatus(int pumpId, String carName, String status) {
        SwingUtilities.invokeLater(() -> {
            PumpPanel panel = pumpPanels.get(pumpId);
            if (panel != null) {
                panel.updateStatus(carName, status);
            }
        });
    }
    
    public void updateWaitingArea() {
        SwingUtilities.invokeLater(() -> {
            waitingAreaPanel.removeAll();
            
            if (resources != null) {
                resources.mutex.P();
                int count = resources.queue.size();
                java.util.List<String> carList = new ArrayList<>();
                for (Car car : resources.queue) {
                    carList.add(car.name);
                }
                resources.mutex.V();
                
                waitingCountLabel.setText("Cars waiting: " + count + " / " + waitingAreaCapacity);
                
                for (String carName : carList) {
                    JLabel carLabel = new JLabel("  🚗 " + carName);
                    carLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                    carLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    waitingAreaPanel.add(carLabel);
                }
            } else {
                waitingCountLabel.setText("Cars waiting: 0 / " + waitingAreaCapacity);
            }
            
            waitingAreaPanel.revalidate();
            waitingAreaPanel.repaint();
        });
    }
    
    public void logEvent(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            logArea.append(timestamp + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void onSimulationComplete() {
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            simulationRunning = false;
            
            JOptionPane.showMessageDialog(this,
                "All cars have been serviced!\nSimulation complete.",
                "Simulation Complete",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}

// Main class to launch GUI
public class ServiceStationGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SimulationGUI();
        });
    }
}
