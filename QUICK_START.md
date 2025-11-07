# Quick Start Guide - Car Wash Simulator GUI

## Launch Commands

### Compile:
```powershell
javac ServiceStationGUI.java
```

### Run:
```powershell
java ServiceStationGUI
```

## Quick Controls

| Control | Function |
|---------|----------|
| **Start Simulation** | Begin the simulation |
| **Pause** | Pause execution (stops threads) |
| **Reset** | Clear and restart |
| **Speed Slider** | Adjust simulation speed (0.1x - 5.0x) |
| **File → New Configuration** | Change simulation parameters |

## GUI Sections

### 1. Control Panel (Top)
- Buttons: Start, Pause, Reset
- Speed control slider with multiplier display

### 2. Service Bays (Center - Main Area)
- Visual panels for each pump
- Shows pump status and current car
- Color-coded: Green = Free, Red = Occupied

### 3. Waiting Area (Right Side)
- List of cars currently waiting
- Counter showing current/max capacity
- Auto-updates in real-time

### 4. Activity Log (Bottom)
- Timestamped event history
- All arrivals, services, and completions
- Auto-scrolls to latest events

## Configuration Parameters

### Waiting Area Capacity
- Range: 1-10
- Default: 5
- Maximum number of cars that can wait

### Number of Service Bays
- Range: 1-6
- Default: 3
- Number of concurrent pumps

### Car Names
- Space-separated names
- Default: A B C D E F G H I J
- Any alphanumeric names work

## Speed Control Tips

| Speed | Best For |
|-------|----------|
| 0.1x - 0.5x | Detailed analysis, step-by-step observation |
| 1.0x | Normal speed, balanced viewing |
| 2.0x - 3.0x | Quick testing, multiple runs |
| 4.0x - 5.0x | Stress testing, fast completion |

## Color Coding

### Pump Status:
- 🟢 **Green** = FREE (pump available)
- 🔴 **Red** = OCCUPIED (serving a car)

### Buttons:
- 🟢 **Green** = Start (ready to begin)
- 🟡 **Orange** = Pause (can pause)
- 🔴 **Red** = Reset (stop and clear)

## Typical Workflow

1. **Configure** → Set parameters in dialog
2. **Start** → Click "Start Simulation"
3. **Observe** → Watch pumps and waiting area
4. **Adjust Speed** → Use slider for better view
5. **Analyze** → Check activity log
6. **Reset** → Clear for next run
7. **Repeat** → Try different configurations

## What to Observe

### Queue Behavior:
- How cars enter and leave waiting area
- Maximum queue size reached
- Empty vs full queue conditions

### Pump Utilization:
- Which pumps are most active
- Idle time vs busy time
- Load distribution

### Service Flow:
- Time from arrival to service start
- Service duration per car
- Total throughput

### Synchronization:
- No race conditions
- Proper mutex usage
- Semaphore coordination

## Sample Test Cases

### Test 1: Basic Flow
```
Capacity: 5, Pumps: 3, Cars: A B C D E
Speed: 1.0x
Goal: Observe normal operation
```

### Test 2: Queue Pressure
```
Capacity: 3, Pumps: 2, Cars: A B C D E F G H I J K L
Speed: 0.5x
Goal: See full queue management
```

### Test 3: High Availability
```
Capacity: 10, Pumps: 5, Cars: A B C D E F G H
Speed: 1.5x
Goal: Multiple free pumps, minimal wait
```

### Test 4: Bottleneck
```
Capacity: 2, Pumps: 1, Cars: A B C D E F G H I J
Speed: 0.5x
Goal: Maximum contention observation
```

## Common Issues

### Issue: Simulation doesn't start
**Solution**: Click Reset first, then Start

### Issue: Can't see pump activity
**Solution**: Increase speed or wait longer

### Issue: Pause doesn't resume
**Solution**: Use Reset, then Start (current limitation)

### Issue: Window too small
**Solution**: Maximize window or resize as needed

## Advanced Usage

### Multiple Configurations:
1. Run a simulation
2. Wait for completion or Reset
3. File → New Configuration
4. Try different parameters
5. Compare results in logs

### Performance Analysis:
1. Set speed to 0.5x
2. Watch individual pump timing
3. Count cars in queue over time
4. Note service patterns

### Screenshot/Documentation:
1. Pause at interesting moments
2. Capture current state
3. Copy log entries
4. Reset and continue

## Files in Project

| File | Purpose |
|------|---------|
| `ServiceStationGUI.java` | GUI implementation (this version) |
| `ServiceStation.java` | Original console version |
| `GUI_README.md` | Detailed documentation |
| `QUICK_START.md` | This quick reference |
| `README.md` | Original assignment description |

## Key Features

✅ Real-time visualization
✅ Start/Pause/Reset controls  
✅ Variable speed control (0.1x-5.0x)
✅ Visual pump status
✅ Waiting area display
✅ Complete activity logging
✅ Thread-safe implementation
✅ Easy reconfiguration
✅ No external dependencies

## Next Steps

1. Experiment with different configurations
2. Observe queue behavior patterns
3. Test edge cases (1 pump, 10 cars)
4. Compare with console version
5. Document interesting findings

Enjoy exploring the Car Wash Simulator! 🚗💨
