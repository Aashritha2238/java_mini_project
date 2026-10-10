import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Production {

    private static final String DIR = "data/";

    private int productionId;
    private String productionName;
    private ProductionPhase currentPhase;
    private Movie movie;
    private Budget budget;
    private Timeline timeline;

    private ProductionTeam<Actor> actorTeam = new ProductionTeam<>();
    private ProductionTeam<CrewMember> crewTeam = new ProductionTeam<>();

    private List<Scene> scenes = new ArrayList<>();
    private List<ShootingSchedule> schedules = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<Equipment> equipment = new ArrayList<>();


    // ---------- constructor ----------

    public Production(int productionId, String productionName, Movie movie,
                      Budget budget, Timeline timeline) {

        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }

        if (timeline == null) {
            throw new IllegalArgumentException("Timeline cannot be null");
        }

        this.productionId = productionId;
        this.productionName = productionName;
        this.movie = movie;
        this.budget = budget;
        this.timeline = timeline;
        this.currentPhase = ProductionPhase.PRE_PRODUCTION;
    }


    // ---------- getters ----------

    public int getProductionId() {
        return productionId;
    }

    public String getProductionName() {
        return productionName;
    }

    public ProductionPhase getCurrentPhase() {
        return currentPhase;
    }

    public Movie getMovie() {
        return movie;
    }

    public Budget getBudget() {
        return budget;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public ProductionTeam<Actor> getActorTeam() {
        return actorTeam;
    }

    public ProductionTeam<CrewMember> getCrewTeam() {
        return crewTeam;
    }

    public List<Scene> getScenes() {
        return Collections.unmodifiableList(scenes);
    }

    public List<ShootingSchedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    public List<Location> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }


    // ---------- adding people and resources ----------

    public void addActor(Actor actor) {

        if (actor == null) {
            throw new IllegalArgumentException("Actor cannot be null");
        }

        if (findActor(actor.getId()) != null) {
            throw new IllegalArgumentException(
                    "An actor with id " + actor.getId() + " already exists");
        }

        actorTeam.addMember(actor);
    }


    public void addCrew(CrewMember crew) {

        if (crew == null) {
            throw new IllegalArgumentException("Crew member cannot be null");
        }

        if (findCrew(crew.getId()) != null) {
            throw new IllegalArgumentException(
                    "A crew member with id " + crew.getId() + " already exists");
        }

        crewTeam.addMember(crew);
    }


    public void addLocation(Location location) {

        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        if (findLocation(location.getLocationId()) != null) {
            throw new IllegalArgumentException(
                    "A location with id " + location.getLocationId() + " already exists");
        }

        locations.add(location);
    }


    public void addEquipment(Equipment item) {

        if (item == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }

        if (findEquipment(item.getEquipmentId()) != null) {
            throw new IllegalArgumentException(
                    "Equipment with id " + item.getEquipmentId() + " already exists");
        }

        equipment.add(item);
    }


    // ---------- find methods ----------

    public Actor findActor(int id) {

        for (Actor a : actorTeam.getMembers()) {

            if (a.getId() == id) {
                return a;
            }
        }

        return null;
    }


    public CrewMember findCrew(int id) {

        for (CrewMember c : crewTeam.getMembers()) {

            if (c.getId() == id) {
                return c;
            }
        }

        return null;
    }


    public Location findLocation(int id) {

        for (Location l : locations) {

            if (l.getLocationId() == id) {
                return l;
            }
        }

        return null;
    }


    public Equipment findEquipment(int id) {

        for (Equipment e : equipment) {

            if (e.getEquipmentId() == id) {
                return e;
            }
        }

        return null;
    }


    public Scene findScene(int id) {

        for (Scene s : scenes) {

            if (s.getSceneId() == id) {
                return s;
            }
        }

        return null;
    }


    // ---------- scenes and schedules ----------

    public void addScene(Scene scene) {

        if (scene == null) {
            throw new IllegalArgumentException("Scene cannot be null");
        }

        if (findScene(scene.getSceneId()) != null) {
            throw new IllegalArgumentException(
                    "A scene with id " + scene.getSceneId() + " already exists");
        }

        if (scene.getLocation() == null) {
            throw new IllegalArgumentException(
                    "Scene must have a location");
        }

        if (!locations.contains(scene.getLocation())) {
            throw new IllegalArgumentException(
                    "Add the location " + scene.getLocation().getName()
                    + " to the production first");
        }

        for (Equipment e : scene.getEquipment()) {

            if (!equipment.contains(e)) {
                throw new IllegalArgumentException(
                        "Add the equipment " + e.getName()
                        + " to the production first");
            }
        }

        scenes.add(scene);
    }


    public void addSchedule(ShootingSchedule schedule)
            throws ScheduleConflictException,
                   ResourceNotAvailableException,
                   BudgetExceededException {

        if (schedule == null) {
            throw new IllegalArgumentException("Schedule cannot be null");
        }

        Scene scene = schedule.getScene();

        if (scene == null) {
            throw new IllegalArgumentException(
                    "Schedule must have a scene");
        }

        if (findScene(scene.getSceneId()) == null) {
            throw new IllegalArgumentException(
                    "Scene " + scene.getSceneNumber()
                    + " is not part of this production");
        }


        // Check that all equipment belongs to this production
        for (Equipment e : scene.getEquipment()) {

            if (!equipment.contains(e)) {
                throw new IllegalArgumentException(
                        "Add the equipment " + e.getName()
                        + " to the production first");
            }
        }


        // Check scheduling conflicts
        checkConflicts(schedule);


        // Book location and equipment
        schedule.schedule();


        // Calculate shooting cost
        double cost = scene.getLocation().getDailyCost();

        for (Equipment e : scene.getEquipment()) {
            cost += e.getDailyCost();
        }


        // Add cost to budget
        try {

            budget.addExpense(
                    "Scene " + scene.getSceneNumber() + " shoot",
                    cost
            );

        } catch (BudgetExceededException ex) {

            // If budget is exceeded, cancel the booking
            schedule.cancelSchedule();

            throw ex;
        }


        schedules.add(schedule);
    }


    // ---------- schedule conflict checking ----------

    private void checkConflicts(ShootingSchedule s)
            throws ScheduleConflictException {

        for (ShootingSchedule other : schedules) {


            // Same scene cannot be scheduled twice
            if (other.getScene().getSceneId()
                    == s.getScene().getSceneId()) {

                throw new ScheduleConflictException(
                        "Scene " + s.getScene().getSceneNumber()
                        + " is already scheduled");
            }


            // Different dates cannot conflict
            if (!other.getDate().equals(s.getDate())) {
                continue;
            }


            // Check time overlap
            boolean overlap =
                    s.getStartTime().isBefore(other.getEndTime())
                    && other.getStartTime().isBefore(s.getEndTime());

            if (!overlap) {
                continue;
            }


            // Check actors
            for (Actor a : s.getScene().getActors()) {

                if (other.getScene().getActors().contains(a)) {

                    throw new ScheduleConflictException(
                            a.getName()
                            + " is already in scene "
                            + other.getScene().getSceneNumber()
                            + " at that time");
                }
            }


            // Check crew members
            for (CrewMember c : s.getScene().getCrewMembers()) {

                if (other.getScene().getCrewMembers().contains(c)) {

                    throw new ScheduleConflictException(
                            c.getName()
                            + " is already in scene "
                            + other.getScene().getSceneNumber()
                            + " at that time");
                }
            }
        }
    }


    // ---------- filtering and sorting ----------

    public List<Scene> filterScenes(Predicate<Scene> condition) {

        List<Scene> result = new ArrayList<>();

        for (Scene s : scenes) {

            if (condition.test(s)) {
                result.add(s);
            }
        }

        return result;
    }


    public void sortSchedules(Comparator<ShootingSchedule> order) {

        schedules.sort(order);
    }


    // ---------- phase and progress ----------

    public void advancePhase() {

        ProductionPhase[] phases = ProductionPhase.values();


        // Already at POST_PRODUCTION
        if (currentPhase.ordinal() == phases.length - 1) {

            throw new IllegalStateException(
                    "The production is already in its last phase");
        }


        // Before moving from PRODUCTION to POST_PRODUCTION,
        // all scenes must be completed.
        if (currentPhase == ProductionPhase.PRODUCTION) {

            for (Scene s : scenes) {

                if (s.getStatus() != SceneStatus.COMPLETED) {

                    throw new IllegalStateException(
                            "Scene " + s.getSceneNumber()
                            + " is not completed yet");
                }
            }
        }


        currentPhase =
                phases[currentPhase.ordinal() + 1];
    }


    public double getOverallProgress() {

        if (scenes.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (Scene s : scenes) {
            total += s.getProgress();
        }

        return total / scenes.size();
    }


    // ---------- saving ----------

    public void saveAll() {

        try {

            new File(DIR).mkdirs();


            // Save director
            List<Director> directors = new ArrayList<>();
            directors.add(movie.getDirector());

            // Save movie
            List<Movie> movies = new ArrayList<>();
            movies.add(movie);


            new FileStorage<Director>(
                    DIR + "director.csv")
                    .save(directors);

            new FileStorage<Movie>(
                    DIR + "movie.csv")
                    .save(movies);

            new FileStorage<Actor>(
                    DIR + "actors.csv")
                    .save(actorTeam.getMembers());

            new FileStorage<CrewMember>(
                    DIR + "crew.csv")
                    .save(crewTeam.getMembers());

            new FileStorage<Location>(
                    DIR + "locations.csv")
                    .save(locations);

            new FileStorage<Equipment>(
                    DIR + "equipment.csv")
                    .save(equipment);

            new FileStorage<Scene>(
                    DIR + "scenes.csv")
                    .save(scenes);

            new FileStorage<ShootingSchedule>(
                    DIR + "schedules.csv")
                    .save(schedules);


            // Save current production phase
            Files.writeString(
                    Path.of(DIR + "phase.txt"),
                    currentPhase.name()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not save data: " + e.getMessage());
        }
    }


    // ---------- loading ----------

    public void loadAll() {

        try {


            // ---------- load director and movie ----------

            if (exists("director.csv")
                    && exists("movie.csv")) {

                List<Director> directors =
                        new FileStorage<Director>(
                                DIR + "director.csv")
                                .load(Director::fromCsv);

                if (!directors.isEmpty()) {

                    movie =
                            new FileStorage<Movie>(
                                    DIR + "movie.csv")
                                    .load(line ->
                                            Movie.fromCsv(line, directors))
                                    .get(0);
                }
            }


            // ---------- clear current collections ----------

            actorTeam = new ProductionTeam<>();
            crewTeam = new ProductionTeam<>();

            scenes.clear();
            schedules.clear();
            locations.clear();
            equipment.clear();


            // ---------- load actors ----------

            if (exists("actors.csv")) {

                List<Actor> loadedActors =
                        new FileStorage<Actor>(
                                DIR + "actors.csv")
                                .load(Actor::fromCsv);

                for (Actor a : loadedActors) {
                    addActor(a);
                }
            }


            // ---------- load crew ----------

            if (exists("crew.csv")) {

                List<CrewMember> loadedCrew =
                        new FileStorage<CrewMember>(
                                DIR + "crew.csv")
                                .load(CrewMember::fromCsv);

                for (CrewMember c : loadedCrew) {
                    addCrew(c);
                }
            }


            // ---------- load locations ----------

            if (exists("locations.csv")) {

                List<Location> loadedLocations =
                        new FileStorage<Location>(
                                DIR + "locations.csv")
                                .load(this::parseLocation);

                for (Location l : loadedLocations) {
                    addLocation(l);
                }
            }


            // ---------- load equipment ----------

            if (exists("equipment.csv")) {

                List<Equipment> loadedEquipment =
                        new FileStorage<Equipment>(
                                DIR + "equipment.csv")
                                .load(this::parseEquipment);

                for (Equipment e : loadedEquipment) {
                    addEquipment(e);
                }
            }


            // ---------- load scenes ----------

            if (exists("scenes.csv")) {

                List<Scene> loadedScenes =
                        new FileStorage<Scene>(
                                DIR + "scenes.csv")
                                .load(this::parseScene);

                for (Scene s : loadedScenes) {
                    addScene(s);
                }
            }


            // ---------- load schedules ----------

            if (exists("schedules.csv")) {

                List<ShootingSchedule> loadedSchedules =
                        new FileStorage<ShootingSchedule>(
                                DIR + "schedules.csv")
                                .load(this::parseSchedule);


                for (ShootingSchedule s : loadedSchedules) {

                    try {

                        addSchedule(s);

                    } catch (Exception e) {

                        System.out.println(
                                "Skipped schedule "
                                + s.getScheduleId()
                                + ": "
                                + e.getMessage());
                    }
                }
            }


            // ---------- load production phase ----------

            if (exists("phase.txt")) {

                String phase =
                        Files.readString(
                                Path.of(DIR + "phase.txt"))
                                .trim();

                currentPhase =
                        ProductionPhase.valueOf(phase);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not load data: "
                    + e.getMessage());
        }
    }


    // ---------- helper for checking files ----------

    private boolean exists(String fileName) {

        return new File(DIR + fileName).exists();
    }


    // ---------- CSV parsers ----------

    private Location parseLocation(String line) {

        String[] p = line.split(",", -1);

        return new Location(
                Integer.parseInt(p[0]),
                p[1],
                p[2],
                Double.parseDouble(p[3])
        );
    }


    private Equipment parseEquipment(String line) {

        String[] p = line.split(",", -1);

        return new Equipment(
                Integer.parseInt(p[0]),
                p[1],
                p[2],
                Double.parseDouble(p[3])
        );
    }


    private Scene parseScene(String line) {

        String[] p = line.split(",", -1);


        // CSV format:
        // sceneId,sceneNumber,description,status,progress,
        // locationId,actorIds,crewIds,equipmentIds


        Location location =
                findLocation(Integer.parseInt(p[5]));


        if (location == null) {

            throw new IllegalArgumentException(
                    "Location " + p[5]
                    + " not found for scene "
                    + p[0]);
        }


        // Create equipment set first because the actual
        // Scene constructor requires a Set<Equipment>.
        Set<Equipment> sceneEquipment =
                new HashSet<>();


        // equipmentIds are stored in p[8]
        if (p.length > 8 && !p[8].isEmpty()) {

            for (String id : p[8].split(";")) {

                Equipment e =
                        findEquipment(
                                Integer.parseInt(id));

                if (e != null) {
                    sceneEquipment.add(e);
                }
            }
        }


        Scene scene =
                new Scene(
                        Integer.parseInt(p[0]),
                        Integer.parseInt(p[1]),
                        p[2],
                        location,
                        sceneEquipment
                );


        // ---------- actors ----------

        if (p.length > 6 && !p[6].isEmpty()) {

            for (String id : p[6].split(";")) {

                Actor a =
                        findActor(
                                Integer.parseInt(id));

                if (a != null) {
                    scene.addActor(a);
                }
            }
        }


        // ---------- crew ----------

        if (p.length > 7 && !p[7].isEmpty()) {

            for (String id : p[7].split(";")) {

                CrewMember c =
                        findCrew(
                                Integer.parseInt(id));

                if (c != null) {
                    scene.addCrewMember(c);
                }
            }
        }


        // Restore progress
        scene.updateProgress(
                Double.parseDouble(p[4])
        );


        // Restore exact saved status
        scene.setStatus(
                SceneStatus.valueOf(p[3])
        );


        return scene;
    }


    private ShootingSchedule parseSchedule(String line) {

        String[] p = line.split(",", -1);


        // scheduleId,date,startTime,endTime,sceneId

        Scene scene =
                findScene(
                        Integer.parseInt(p[4])
                );


        if (scene == null) {

            throw new IllegalArgumentException(
                    "Scene " + p[4]
                    + " not found for schedule "
                    + p[0]);
        }


        return new ShootingSchedule(
                Integer.parseInt(p[0]),
                LocalDate.parse(p[1]),
                LocalTime.parse(p[2]),
                LocalTime.parse(p[3]),
                scene
        );
    }
}