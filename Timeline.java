import java.time.LocalDate;
import java.util.*;

public class Timeline {
    private LocalDate startDate;
    private LocalDate endDate;
     // here Key : Phase and Value : deadline && EnumMap is a map implementation made for enum keys.
    private Map<ProductionPhase, LocalDate> phaseDeadlines = new EnumMap<>(ProductionPhase.class);
    // Current phase of the movie; isOnSchedule() checks this phase's deadline
    private ProductionPhase currentPhase = ProductionPhase.PRE_PRODUCTION;
   
    public Timeline(LocalDate startDate, LocalDate endDate) {
         if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date is before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setPhaseDeadline(ProductionPhase phase, LocalDate deadline) {
        if (phase == null || deadline == null) {
            throw new IllegalArgumentException("Phase and deadline cannot be null");
        }
        phaseDeadlines.put(phase, deadline);
    }

     public void setCurrentPhase(ProductionPhase phase) {
        if (phase == null) {
            throw new IllegalArgumentException("Phase cannot be null");
        }
        this.currentPhase = phase;
    }

    // Average progress of all scenes (0 to 100). No scenes -> 0.
    public double trackProgress(List<Scene> scenes) {
        if (scenes == null || scenes.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        for (Scene s : scenes) {
            total += s.getProgress();
        }
        return total / scenes.size();
    }
      // True while today is on or before the current phase's deadline.
    public boolean isOnSchedule() {
        LocalDate deadline = phaseDeadlines.get(currentPhase);
        if (deadline == null) {
            return true;                       // no deadline set for this phase
        }
        return !LocalDate.now().isAfter(deadline);
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }
    public LocalDate getEndDate() { 
        return endDate; 
    }

}
