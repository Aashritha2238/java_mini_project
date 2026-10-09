import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * The complete text menu of the Movie Production Management System.
 *
 * Every menu option is stored in menuActions (option number -> Runnable), as in the
 * class diagram. All input is validated: readInt, readDate and the other read...
 * methods keep asking until the user types something valid.
 *
 * Owner: Member 4 (Kanishgha Sri).
 */
public class ConsoleMenu {

    /** Like Runnable, but allowed to throw exceptions. Used only inside this class. */
    @FunctionalInterface
    private interface MenuAction {
        void execute() throws Exception;
    }

    private static final int OPTION_ADD_ACTOR = 1;
    private static final int OPTION_ADD_CREW = 2;
    private static final int OPTION_ADD_LOCATION = 3;
    private static final int OPTION_ADD_EQUIPMENT = 4;
    private static final int OPTION_CREATE_SCENE = 5;
    private static final int OPTION_SCHEDULE_SCENE = 6;
    private static final int OPTION_UPDATE_SCENE_PROGRESS = 7;
    private static final int OPTION_SHOW_BUDGET = 8;
    private static final int OPTION_SHOW_TIMELINE = 9;
    private static final int OPTION_ADVANCE_PHASE = 10;
    private static final int OPTION_SAVE = 11;
    private static final int OPTION_LOAD = 12;
    private static final int OPTION_EXIT = 13;

    private final Scanner scanner;
    private final Production production;
    private final Map<Integer, Runnable> menuActions = new LinkedHashMap<>();
    private boolean running = true;

    public ConsoleMenu(Production production) {
        this.production = production;
        this.scanner = new Scanner(System.in);
        registerMenuActions();
    }

    // ------------------------------------------------------------------
    // Menu set-up and main loop
    // ------------------------------------------------------------------

    private void registerMenuActions() {
        register(OPTION_ADD_ACTOR, this::addActor);
        register(OPTION_ADD_CREW, this::addCrewMember);
        register(OPTION_ADD_LOCATION, this::addLocation);
        register(OPTION_ADD_EQUIPMENT, this::addEquipment);
        register(OPTION_CREATE_SCENE, this::createScene);
        register(OPTION_SCHEDULE_SCENE, this::scheduleScene);
        register(OPTION_UPDATE_SCENE_PROGRESS, this::updateSceneProgress);
        register(OPTION_SHOW_BUDGET, this::showBudget);
        register(OPTION_SHOW_TIMELINE, this::showTimeline);
        register(OPTION_ADVANCE_PHASE, this::advancePhase);
        register(OPTION_SAVE, this::saveData);
        register(OPTION_LOAD, this::loadData);
        register(OPTION_EXIT, this::exitProgram);
    }

    /** Wraps an action so that any exception is shown as a message instead of crashing the menu. */
    private void register(int optionNumber, MenuAction action) {
        menuActions.put(optionNumber, () -> {
            try {
                action.execute();
            } catch (ResourceNotAvailableException e) {
                System.out.println("Resource not available: " + e.getMessage());
            } catch (BudgetExceededException e) {
                System.out.println("Budget exceeded: " + e.getMessage());
            } catch (ScheduleConflictException e) {
                System.out.println("Schedule conflict: " + e.getMessage());
            } catch (NoSuchElementException e) {
                throw e; // end of input: let start() deal with it
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    public void start() {
        System.out.println("=== Movie Production Management System ===");
        try {
            while (running) {
                printMenu();
                int chosenOption = readInt("Enter your choice: ");
                Runnable chosenAction = menuActions.get(chosenOption);
                if (chosenAction == null) {
                    System.out.println("Invalid option. Please choose a number from 1 to " + OPTION_EXIT + ".");
                } else {
                    chosenAction.run();
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended. Closing the program.");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("-------------------- MENU --------------------");
        System.out.println(" 1. Add actor");
        System.out.println(" 2. Add crew member");
        System.out.println(" 3. Add location");
        System.out.println(" 4. Add equipment");
        System.out.println(" 5. Create scene");
        System.out.println(" 6. Schedule a scene");
        System.out.println(" 7. Update scene progress");
        System.out.println(" 8. Show budget");
        System.out.println(" 9. Show timeline");
        System.out.println("10. Advance production phase");
        System.out.println("11. Save data");
        System.out.println("12. Load data");
        System.out.println("13. Exit");
        System.out.println("----------------------------------------------");
    }

    // ------------------------------------------------------------------
    // Menu actions
    // ------------------------------------------------------------------

    private void addActor() {
        System.out.println("--- Add actor ---");
        String actorName = readText("Actor name: ");
        String contactDetails = readText("Contact (phone/email): ");
        double actorFee = readNonNegativeDouble("Fee: ");
        String characterName = readText("Character name: ");

        int newActorId = nextPersonId();
        Actor newActor = new Actor(newActorId, actorName, contactDetails, actorFee, characterName);
        production.addActor(newActor);
        System.out.println("Actor added with id " + newActorId + ".");
    }

    private void addCrewMember() {
        System.out.println("--- Add crew member ---");
        String crewName = readText("Crew member name: ");
        String contactDetails = readText("Contact (phone/email): ");
        double crewFee = readNonNegativeDouble("Fee: ");
        String crewRole = readText("Role (e.g. Cinematographer): ");
        String departmentName = readText("Department (e.g. Camera): ");

        int newCrewId = nextPersonId();
        CrewMember newCrewMember = new CrewMember(newCrewId, crewName, contactDetails, crewFee,
                crewRole, departmentName);
        production.addCrew(newCrewMember);
        System.out.println("Crew member added with id " + newCrewId + ".");
    }

    private void addLocation() {
        System.out.println("--- Add location ---");
        String locationName = readText("Location name: ");
        String locationAddress = readText("Address: ");
        double dailyCost = readNonNegativeDouble("Daily cost: ");

        int newLocationId = nextLocationId();
        Location newLocation = new Location(newLocationId, locationName, locationAddress, dailyCost);
        production.addLocation(newLocation);
        System.out.println("Location added with id " + newLocationId + ".");
    }

    private void addEquipment() {
        System.out.println("--- Add equipment ---");
        String equipmentName = readText("Equipment name: ");
        String equipmentType = readText("Type (e.g. Camera, Lighting): ");
        double dailyCost = readNonNegativeDouble("Daily cost: ");

        int newEquipmentId = nextEquipmentId();
        Equipment newEquipment = new Equipment(newEquipmentId, equipmentName, equipmentType, dailyCost);
        production.addEquipment(newEquipment);
        System.out.println("Equipment added with id " + newEquipmentId + ".");
    }

    private void createScene() {
        System.out.println("--- Create scene ---");
        if (production.getLocations().isEmpty()) {
            System.out.println("Add at least one location first (option 3).");
            return;
        }

        int sceneNumber = readUnusedSceneNumber();
        String sceneDescription = readText("Scene description: ");

        printLocations();
        Location sceneLocation = chooseLocation();
        if (sceneLocation == null) {
            System.out.println("Scene creation cancelled.");
            return;
        }

        Scene newScene = new Scene(nextSceneId(), sceneNumber, sceneDescription, sceneLocation);
        addActorsToScene(newScene);
        addCrewToScene(newScene);
        addEquipmentToScene(newScene);

        production.addScene(newScene);
        System.out.println("Scene " + sceneNumber + " created with id " + newScene.getSceneId() + ".");
    }

    private void scheduleScene() throws Exception {
        System.out.println("--- Schedule a scene ---");
        if (production.getScenes().isEmpty()) {
            System.out.println("There are no scenes yet. Create one first (option 5).");
            return;
        }
        printScenes();
        Scene sceneToSchedule = chooseScene();
        if (sceneToSchedule == null) {
            System.out.println("Scheduling cancelled.");
            return;
        }

        LocalDate shootingDate = readDate("Shooting date (yyyy-MM-dd): ");
        LocalTime startTime;
        LocalTime endTime;
        while (true) {
            startTime = readTime("Start time (HH:mm, 24-hour): ");
            endTime = readTime("End time (HH:mm, 24-hour): ");
            if (endTime.isAfter(startTime)) {
                break;
            }
            System.out.println("End time must be after the start time. Please enter both times again.");
        }

        ShootingSchedule newSchedule = new ShootingSchedule(nextScheduleId(), shootingDate,
                startTime, endTime, sceneToSchedule);
        production.addSchedule(newSchedule); // may throw ScheduleConflictException etc.
        System.out.println("Scene " + sceneToSchedule.getSceneNumber() + " scheduled on "
                + shootingDate + " from " + startTime + " to " + endTime + ".");
    }

    private void updateSceneProgress() {
        System.out.println("--- Update scene progress ---");
        if (production.getScenes().isEmpty()) {
            System.out.println("There are no scenes yet.");
            return;
        }
        printScenes();
        Scene sceneToUpdate = chooseScene();
        if (sceneToUpdate == null) {
            System.out.println("Update cancelled.");
            return;
        }
        double newProgressPercent = readPercentage("New progress (0 to 100): ");
        sceneToUpdate.updateProgress(newProgressPercent);
        System.out.println("Scene " + sceneToUpdate.getSceneNumber() + " is now at "
                + formatNumber(sceneToUpdate.getProgress()) + "% (" + sceneToUpdate.getStatus() + ").");
    }

    private void showBudget() {
        Budget budget = production.getBudget();
        System.out.println("--- Budget ---");
        System.out.println("Total budget : " + formatNumber(budget.getTotalAmount()));
        System.out.println("Spent        : " + formatNumber(budget.getSpentAmount()));
        System.out.println("Remaining    : " + formatNumber(budget.getRemaining()));
        Map<String, Double> expensesByCategory = budget.getExpenses();
        if (expensesByCategory.isEmpty()) {
            System.out.println("No expenses recorded yet.");
        } else {
            System.out.println("Expenses by category:");
            for (Map.Entry<String, Double> expense : expensesByCategory.entrySet()) {
                System.out.println("  " + expense.getKey() + " : " + formatNumber(expense.getValue()));
            }
        }
    }

    private void showTimeline() {
        System.out.println("--- Timeline ---");
        System.out.println("Current phase    : " + production.getCurrentPhase());
        System.out.println("Overall progress : " + formatNumber(production.getOverallProgress()) + "%");
        System.out.println("On schedule      : " + (production.getTimeline().isOnSchedule() ? "Yes" : "No"));
        if (production.getScenes().isEmpty()) {
            System.out.println("No scenes created yet.");
        } else {
            System.out.println("Scenes:");
            printScenes();
        }
    }

    private void advancePhase() {
        ProductionPhase phaseBefore = production.getCurrentPhase();
        production.advancePhase();
        ProductionPhase phaseAfter = production.getCurrentPhase();
        if (phaseBefore == phaseAfter) {
            System.out.println("The production is already in its final phase (" + phaseAfter + ").");
        } else {
            System.out.println("Phase advanced: " + phaseBefore + " -> " + phaseAfter);
        }
    }

    private void saveData() {
        production.saveAll();
        System.out.println("All data saved.");
    }

    private void loadData() {
        production.loadAll();
        System.out.println("All data loaded.");
    }

    private void exitProgram() {
        running = false;
        System.out.println("Goodbye!");
    }

    // ------------------------------------------------------------------
    // Choosing things from lists
    // ------------------------------------------------------------------

    private void printLocations() {
        System.out.println("Available locations:");
        for (Location location : production.getLocations()) {
            System.out.println("  [" + location.getLocationId() + "] " + location.getName()
                    + " (daily cost " + formatNumber(location.getDailyCost()) + ")");
        }
    }

    private void printScenes() {
        for (Scene scene : production.getScenes()) {
            System.out.println("  [" + scene.getSceneId() + "] Scene " + scene.getSceneNumber()
                    + ": " + scene.getDescription() + " | " + scene.getStatus()
                    + " | " + formatNumber(scene.getProgress()) + "%");
        }
    }

    /** Asks for a location id until a valid one (or 0 to cancel) is typed. Returns null on cancel. */
    private Location chooseLocation() {
        while (true) {
            int locationId = readInt("Location id (0 to cancel): ");
            if (locationId == 0) {
                return null;
            }
            for (Location location : production.getLocations()) {
                if (location.getLocationId() == locationId) {
                    return location;
                }
            }
            System.out.println("No location with id " + locationId + ".");
        }
    }

    /** Asks for a scene id until a valid one (or 0 to cancel) is typed. Returns null on cancel. */
    private Scene chooseScene() {
        while (true) {
            int sceneId = readInt("Scene id (0 to cancel): ");
            if (sceneId == 0) {
                return null;
            }
            for (Scene scene : production.getScenes()) {
                if (scene.getSceneId() == sceneId) {
                    return scene;
                }
            }
            System.out.println("No scene with id " + sceneId + ".");
        }
    }

    private void addActorsToScene(Scene scene) {
        List<Actor> availableActors = production.getActorTeam().getMembers();
        if (availableActors.isEmpty()) {
            System.out.println("(No actors added yet, skipping actors.)");
            return;
        }
        System.out.println("Available actors:");
        for (Actor actor : availableActors) {
            System.out.println("  [" + actor.getId() + "] " + actor.getName()
                    + " as " + actor.getCharacterName());
        }
        Set<Integer> alreadyAddedIds = new HashSet<>();
        while (true) {
            int actorId = readInt("Actor id to add to the scene (0 to finish): ");
            if (actorId == 0) {
                return;
            }
            Actor selectedActor = null;
            for (Actor actor : availableActors) {
                if (actor.getId() == actorId) {
                    selectedActor = actor;
                }
            }
            if (selectedActor == null) {
                System.out.println("No actor with id " + actorId + ".");
            } else if (!alreadyAddedIds.add(actorId)) {
                System.out.println(selectedActor.getName() + " is already in this scene.");
            } else {
                scene.addActor(selectedActor);
                System.out.println("Added " + selectedActor.getName() + ".");
            }
        }
    }

    private void addCrewToScene(Scene scene) {
        List<CrewMember> availableCrew = production.getCrewTeam().getMembers();
        if (availableCrew.isEmpty()) {
            System.out.println("(No crew members added yet, skipping crew.)");
            return;
        }
        System.out.println("Available crew members:");
        for (CrewMember crewMember : availableCrew) {
            System.out.println("  [" + crewMember.getId() + "] " + crewMember.getName()
                    + " (" + crewMember.getRole() + ")");
        }
        Set<Integer> alreadyAddedIds = new HashSet<>();
        while (true) {
            int crewId = readInt("Crew id to add to the scene (0 to finish): ");
            if (crewId == 0) {
                return;
            }
            CrewMember selectedCrewMember = null;
            for (CrewMember crewMember : availableCrew) {
                if (crewMember.getId() == crewId) {
                    selectedCrewMember = crewMember;
                }
            }
            if (selectedCrewMember == null) {
                System.out.println("No crew member with id " + crewId + ".");
            } else if (!alreadyAddedIds.add(crewId)) {
                System.out.println(selectedCrewMember.getName() + " is already in this scene.");
            } else {
                scene.addCrewMember(selectedCrewMember);
                System.out.println("Added " + selectedCrewMember.getName() + ".");
            }
        }
    }

    private void addEquipmentToScene(Scene scene) {
        List<Equipment> availableEquipment = production.getEquipment();
        if (availableEquipment.isEmpty()) {
            System.out.println("(No equipment added yet, skipping equipment.)");
            return;
        }
        System.out.println("Available equipment:");
        for (Equipment equipment : availableEquipment) {
            System.out.println("  [" + equipment.getEquipmentId() + "] " + equipment.getName()
                    + " (" + equipment.getType() + ")");
        }
        while (true) {
            int equipmentId = readInt("Equipment id to add to the scene (0 to finish): ");
            if (equipmentId == 0) {
                return;
            }
            Equipment selectedEquipment = null;
            for (Equipment equipment : availableEquipment) {
                if (equipment.getEquipmentId() == equipmentId) {
                    selectedEquipment = equipment;
                }
            }
            if (selectedEquipment == null) {
                System.out.println("No equipment with id " + equipmentId + ".");
            } else {
                scene.addEquipment(selectedEquipment); // Scene stores a Set, so duplicates are ignored
                System.out.println("Added " + selectedEquipment.getName() + ".");
            }
        }
    }

    // ------------------------------------------------------------------
    // Id generation (so the user never has to invent ids)
    // ------------------------------------------------------------------

    /** Actors and crew share one id range, so a person id is never used twice. */
    private int nextPersonId() {
        int highestId = 0;
        for (Actor actor : production.getActorTeam().getMembers()) {
            highestId = Math.max(highestId, actor.getId());
        }
        for (CrewMember crewMember : production.getCrewTeam().getMembers()) {
            highestId = Math.max(highestId, crewMember.getId());
        }
        return highestId + 1;
    }

    private int nextLocationId() {
        int highestId = 0;
        for (Location location : production.getLocations()) {
            highestId = Math.max(highestId, location.getLocationId());
        }
        return highestId + 1;
    }

    private int nextEquipmentId() {
        int highestId = 0;
        for (Equipment equipment : production.getEquipment()) {
            highestId = Math.max(highestId, equipment.getEquipmentId());
        }
        return highestId + 1;
    }

    private int nextSceneId() {
        int highestId = 0;
        for (Scene scene : production.getScenes()) {
            highestId = Math.max(highestId, scene.getSceneId());
        }
        return highestId + 1;
    }

    private int nextScheduleId() {
        int highestId = 0;
        for (ShootingSchedule schedule : production.getSchedules()) {
            highestId = Math.max(highestId, schedule.getScheduleId());
        }
        return highestId + 1;
    }

    private int readUnusedSceneNumber() {
        while (true) {
            int sceneNumber = readInt("Scene number: ");
            if (sceneNumber <= 0) {
                System.out.println("Scene number must be greater than 0.");
                continue;
            }
            boolean alreadyUsed = false;
            for (Scene scene : production.getScenes()) {
                if (scene.getSceneNumber() == sceneNumber) {
                    alreadyUsed = true;
                }
            }
            if (alreadyUsed) {
                System.out.println("Scene number " + sceneNumber + " already exists. Choose another.");
            } else {
                return sceneNumber;
            }
        }
    }

    // ------------------------------------------------------------------
    // Validated input. Each method repeats the question until the answer is valid.
    // ------------------------------------------------------------------

    /** Reads a whole integer. Re-asks when the text is not a number. */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String typedText = scanner.nextLine().trim();
            try {
                return Integer.parseInt(typedText);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please type a whole number such as 3.");
            }
        }
    }

    /** Reads a date in the format yyyy-MM-dd (for example 2026-11-15). Re-asks when invalid. */
    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String typedText = scanner.nextLine().trim();
            try {
                return LocalDate.parse(typedText);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use the format yyyy-MM-dd, for example 2026-11-15.");
            }
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String typedText = scanner.nextLine().trim();
            try {
                return LocalTime.parse(typedText);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time. Use the 24-hour format HH:mm, for example 09:30.");
            }
        }
    }

    private double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String typedText = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(typedText);
                if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                    System.out.println("Please enter a number that is 0 or more.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please type a number such as 2500 or 2500.50.");
            }
        }
    }

    private double readPercentage(String prompt) {
        while (true) {
            double value = readNonNegativeDouble(prompt);
            if (value <= 100) {
                return value;
            }
            System.out.println("Progress cannot be more than 100.");
        }
    }

    /** Reads non-empty text. Commas are refused because the data is stored as comma-separated CSV. */
    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String typedText = scanner.nextLine().trim();
            if (typedText.isEmpty()) {
                System.out.println("This field cannot be empty.");
            } else if (typedText.contains(",")) {
                System.out.println("Commas are not allowed (data is saved as CSV). Please rephrase.");
            } else {
                return typedText;
            }
        }
    }

    private String formatNumber(double value) {
        return String.format("%.2f", value);
    }
}
