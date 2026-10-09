import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/*
 * TEMPORARY STAND-INS for the classes other members are writing.
 * They exist ONLY so ConsoleMenu can be compiled and tested alone.
 * DELETE THIS WHOLE FILE once the real classes are in the folder.
 * Run the demo with:  java MenuDemo
 */

enum ProductionPhase { PRE_PRODUCTION, PRODUCTION, POST_PRODUCTION }

enum SceneStatus { PLANNED, SCHEDULED, SHOT, COMPLETED }

class ResourceNotAvailableException extends Exception {
    public ResourceNotAvailableException(String message) { super(message); }
}

class BudgetExceededException extends Exception {
    public BudgetExceededException(String message) { super(message); }
}

class ScheduleConflictException extends Exception {
    public ScheduleConflictException(String message) { super(message); }
}

class Location {
    private int locationId; private String name; private String address; private double dailyCost;
    private Set<LocalDate> bookedDates = new HashSet<>();
    Location(int locationId, String name, String address, double dailyCost) {
        this.locationId = locationId; this.name = name; this.address = address; this.dailyCost = dailyCost;
    }
    int getLocationId() { return locationId; }
    String getName() { return name; }
    double getDailyCost() { return dailyCost; }
    void book(LocalDate date) throws ResourceNotAvailableException {
        if (!bookedDates.add(date)) throw new ResourceNotAvailableException(name + " is already booked on " + date);
    }
    void cancelBooking(LocalDate date) { bookedDates.remove(date); }
    boolean isAvailable(LocalDate date) { return !bookedDates.contains(date); }
}

class Equipment {
    private int equipmentId; private String name; private String type; private double dailyCost;
    Equipment(int equipmentId, String name, String type, double dailyCost) {
        this.equipmentId = equipmentId; this.name = name; this.type = type; this.dailyCost = dailyCost;
    }
    int getEquipmentId() { return equipmentId; }
    String getName() { return name; }
    String getType() { return type; }
    double getDailyCost() { return dailyCost; }
}

class Scene {
    private int sceneId; private int sceneNumber; private String description;
    private SceneStatus status = SceneStatus.PLANNED; private double progress = 0;
    private List<Actor> actors = new ArrayList<>();
    private List<CrewMember> crewMembers = new ArrayList<>();
    private Set<Equipment> equipment = new HashSet<>();
    private Location location;
    Scene(int sceneId, int sceneNumber, String description, Location location) {
        this.sceneId = sceneId; this.sceneNumber = sceneNumber; this.description = description; this.location = location;
    }
    int getSceneId() { return sceneId; }
    int getSceneNumber() { return sceneNumber; }
    String getDescription() { return description; }
    SceneStatus getStatus() { return status; }
    void setStatus(SceneStatus status) { this.status = status; }
    double getProgress() { return progress; }
    Location getLocation() { return location; }
    Set<Equipment> getEquipment() { return equipment; }
    void addActor(Actor actor) { actors.add(actor); }
    void addCrewMember(CrewMember crewMember) { crewMembers.add(crewMember); }
    void addEquipment(Equipment item) { equipment.add(item); }
    void updateProgress(double newProgress) {
        progress = newProgress;
        if (progress >= 100) status = SceneStatus.COMPLETED;
        else if (progress > 0) status = SceneStatus.SHOT;
    }
}

class ShootingSchedule {
    private int scheduleId; private LocalDate date; private LocalTime startTime; private LocalTime endTime; private Scene scene;
    ShootingSchedule(int scheduleId, LocalDate date, LocalTime startTime, LocalTime endTime, Scene scene) {
        this.scheduleId = scheduleId; this.date = date; this.startTime = startTime; this.endTime = endTime; this.scene = scene;
    }
    int getScheduleId() { return scheduleId; }
    LocalDate getDate() { return date; }
    LocalTime getStartTime() { return startTime; }
    LocalTime getEndTime() { return endTime; }
    Scene getScene() { return scene; }
    void schedule() throws ResourceNotAvailableException {
        scene.getLocation().book(date);
        scene.setStatus(SceneStatus.SCHEDULED);
    }
}

class Budget {
    private double totalAmount; private double spentAmount = 0;
    private Map<String, Double> expenses = new LinkedHashMap<>();
    Budget(double totalAmount) { this.totalAmount = totalAmount; }
    double getTotalAmount() { return totalAmount; }
    double getSpentAmount() { return spentAmount; }
    Map<String, Double> getExpenses() { return expenses; }
    double getRemaining() { return totalAmount - spentAmount; }
    void addExpense(String category, double amount) throws BudgetExceededException {
        if (spentAmount + amount > totalAmount)
            throw new BudgetExceededException("Expense of " + amount + " for " + category
                    + " exceeds the remaining budget of " + getRemaining());
        expenses.merge(category, amount, Double::sum);
        spentAmount += amount;
    }
}

class Timeline {
    private LocalDate endDate;
    Timeline(LocalDate endDate) { this.endDate = endDate; }
    boolean isOnSchedule() { return !LocalDate.now().isAfter(endDate); }
    double trackProgress(List<Scene> scenes) {
        if (scenes.isEmpty()) return 0;
        double sum = 0;
        for (Scene scene : scenes) sum += scene.getProgress();
        return sum / scenes.size();
    }
}

class Production {
    private ProductionPhase currentPhase = ProductionPhase.PRE_PRODUCTION;
    private Budget budget; private Timeline timeline;
    private ProductionTeam<Actor> actorTeam = new ProductionTeam<>();
    private ProductionTeam<CrewMember> crewTeam = new ProductionTeam<>();
    private List<Scene> scenes = new ArrayList<>();
    private List<ShootingSchedule> schedules = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<Equipment> equipment = new ArrayList<>();

    Production(Budget budget, Timeline timeline) { this.budget = budget; this.timeline = timeline; }

    void addActor(Actor actor) { actorTeam.addMember(actor); }
    void addCrew(CrewMember crew) { crewTeam.addMember(crew); }
    void addLocation(Location location) { locations.add(location); }
    void addEquipment(Equipment item) { equipment.add(item); }
    void addScene(Scene scene) { scenes.add(scene); }

    void addSchedule(ShootingSchedule newSchedule) throws ScheduleConflictException,
            ResourceNotAvailableException, BudgetExceededException {
        for (ShootingSchedule existing : schedules) {
            if (existing.getScene() == newSchedule.getScene())
                throw new ScheduleConflictException("Scene " + newSchedule.getScene().getSceneNumber() + " is already scheduled.");
            boolean sameDay = existing.getDate().equals(newSchedule.getDate());
            boolean sameLocation = existing.getScene().getLocation() == newSchedule.getScene().getLocation();
            boolean overlap = newSchedule.getStartTime().isBefore(existing.getEndTime())
                    && existing.getStartTime().isBefore(newSchedule.getEndTime());
            if (sameDay && sameLocation && overlap)
                throw new ScheduleConflictException("Another scene already uses this location at that time.");
        }
        budget.addExpense("Location", newSchedule.getScene().getLocation().getDailyCost());
        newSchedule.schedule();
        schedules.add(newSchedule);
    }

    void advancePhase() {
        ProductionPhase[] phases = ProductionPhase.values();
        if (currentPhase.ordinal() < phases.length - 1) currentPhase = phases[currentPhase.ordinal() + 1];
    }
    double getOverallProgress() { return timeline.trackProgress(scenes); }
    void saveAll() { System.out.println("[stub] saveAll() called"); }
    void loadAll() { System.out.println("[stub] loadAll() called"); }

    ProductionPhase getCurrentPhase() { return currentPhase; }
    Budget getBudget() { return budget; }
    Timeline getTimeline() { return timeline; }
    ProductionTeam<Actor> getActorTeam() { return actorTeam; }
    ProductionTeam<CrewMember> getCrewTeam() { return crewTeam; }
    List<Scene> getScenes() { return scenes; }
    List<ShootingSchedule> getSchedules() { return schedules; }
    List<Location> getLocations() { return locations; }
    List<Equipment> getEquipment() { return equipment; }
}

class MenuDemo {
    public static void main(String[] args) {
        Production production = new Production(new Budget(100000), new Timeline(LocalDate.now().plusMonths(6)));
        new ConsoleMenu(production).start();
    }
}
