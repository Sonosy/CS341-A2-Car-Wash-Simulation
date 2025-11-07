# Car Wash & Gas Station Simulator - GUI Version

## Overview
This is a graphical user interface (GUI) implementation of the Producer-Consumer Car Wash simulation with full control over the simulation execution.

## Features

### 1. **Visual Pump Display**
- Each service bay (pump) is displayed as a separate panel
- Shows real-time status: **FREE** (green) or **OCCUPIED** (red)
- Displays which car is currently being serviced at each pump
- Color-coded for easy identification

### 2. **Waiting Area Visualization**
- Real-time display of cars waiting in queue
- Shows car count: "Cars waiting: X / Y" (current/capacity)
- Visual list of all cars currently in the waiting area
- Auto-updates as cars enter and leave

### 3. **Interactive Controls**

#### Start Button
- Begins the simulation with configured parameters
- Disabled during active simulation

#### Pause Button
- Pauses the simulation at any point
- Allows you to freeze and analyze the current state
- Can be resumed (currently requires reset to continue)

#### Reset Button
- Stops the current simulation
- Clears all states (pumps, waiting area, logs)
- Allows you to start fresh

### 4. **Speed Control Slider**
- Range: 0.1x to 5.0x speed
- Control simulation speed in real-time
- Slower speeds (0.5x-1.0x): Better for analysis and observation
- Faster speeds (2.0x-5.0x): Quickly run through scenarios
- Live adjustment - changes take effect immediately

### 5. **Activity Log**
- Comprehensive timestamped event log
- Records all activities:
  - Car arrivals
  - Cars entering waiting area
  - Pump occupancy changes
  - Service start/completion
  - Bay availability
- Auto-scrolls to latest events
- Searchable and copyable

### 6. **Configuration Dialog**
- Set waiting area capacity (1-10)
- Configure number of service bays (pumps)
- Define car names (space-separated)
- Can be reconfigured anytime via File menu

## How to Run

### Compile:
```powershell
javac ServiceStationGUI.java
```

### Run:
```powershell
java ServiceStationGUI
```

## Usage Instructions

### Initial Setup:
1. When the application starts, a configuration dialog appears
2. Set your desired parameters:
   - **Waiting Area Capacity**: 1-10 (default: 5)
   - **Number of Service Bays**: 1-6 (default: 3)
   - **Car Names**: Space-separated list (default: A B C D E F G H I J)
3. Click OK to initialize

### Running the Simulation:
1. Click **Start Simulation** to begin
2. Watch as cars arrive and are serviced by available pumps
3. Observe the waiting area filling and emptying
4. Monitor the activity log for detailed events

### Controlling Speed:
1. Adjust the **Speed** slider at any time
2. Move left for slower (better for analysis)
3. Move right for faster (quick testing)
4. Current speed multiplier is displayed (e.g., "2.5x")

### Pausing and Analyzing:
1. Click **Pause** to freeze the simulation
2. Examine pump states and waiting queue
3. Review the activity log
4. Note: Currently pause stops threads; use Reset to continue

### Resetting:
1. Click **Reset** to stop and clear everything
2. Click **Start Simulation** again to run with same config
3. Or use **File → New Configuration** to change parameters

### Reconfiguration:
1. Go to **File → New Configuration** menu
2. Enter new parameters
3. The display updates to show new pump count
4. Click **Start Simulation** to run with new settings

## Visual Indicators

### Pump Status Colors:
- **Green background**: Pump is FREE and available
- **Red background**: Pump is OCCUPIED and servicing a car
- **Car name displayed**: Shows which car is at the pump

### Waiting Area:
- 🚗 icon with car name for each waiting car
- Count display shows current/maximum capacity
- Scrollable if many cars are waiting

### Activity Log:
- Timestamps in [HH:MM:SS] format
- Color-coded events (implementation detail)
- Chronological order from top to bottom

## Example Scenarios to Test

### Scenario 1: High Load
- Capacity: 3
- Pumps: 2
- Cars: A B C D E F G H I J K L
- Speed: 0.5x
- **Observe**: Queue management and waiting behavior

### Scenario 2: Low Load
- Capacity: 5
- Pumps: 4
- Cars: A B C D E
- Speed: 1.0x
- **Observe**: Multiple free pumps, minimal waiting

### Scenario 3: Stress Test
- Capacity: 10
- Pumps: 2
- Cars: A B C D E F G H I J K L M N O P Q R S T
- Speed: 2.0x
- **Observe**: Full queue management

### Scenario 4: Analysis Mode
- Capacity: 2
- Pumps: 1
- Cars: A B C D E F
- Speed: 0.5x
- **Observe**: Detailed step-by-step execution

## Technical Details

### Thread Management:
- Each car runs in its own thread (Producer)
- Each pump runs in its own thread (Consumer)
- Proper synchronization using semaphores and mutex
- Clean thread termination on reset/pause

### Synchronization:
- Uses the original semaphore implementation
- Maintains all thread-safety guarantees
- Prevents race conditions
- Proper resource cleanup

### GUI Updates:
- Thread-safe UI updates using SwingUtilities.invokeLater()
- Real-time reflection of simulation state
- No blocking of the event dispatch thread

## Troubleshooting

### Simulation won't start:
- Check if previous simulation is still running
- Try clicking Reset first
- Ensure valid configuration was entered

### Pumps show no activity:
- Check if cars were entered in configuration
- Verify simulation started (check log)
- Try increasing speed if set too low

### Pause doesn't resume:
- Current limitation: pause stops threads
- Use Reset and Start for new simulation run
- Future enhancement: implement true pause/resume

## Keyboard Shortcuts
- Currently not implemented
- Future enhancement opportunity

## Original Console Version
The original console-based version is still available in `ServiceStation.java` and can be run separately for comparison or testing purposes.

## Requirements
- Java 8 or higher (for Swing support)
- No external libraries required
- Works on Windows, macOS, and Linux

## Credits
Implementation based on the classic Producer-Consumer problem using the Bounded Buffer pattern with semaphores for synchronization.
