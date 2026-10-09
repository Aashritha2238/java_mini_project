import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    public Production(int productionId, String productionName, Movie movie,
                      Budget budget, Timeline timeline) {
        this.productionId = productionId;
        this.productionName = productionName;
        this.movie = movie;
        this.budget = budget;
        this.timeline = timeline;
        this.currentPhase = ProductionPhase.PRE_PRODUCTION;
    }

    public int getProductionId() { return productionId; }
    public String getProductionName() { return productionName; }
    public ProductionPhase getCurrentPhase() { return currentPhase; }
    public Movie getMovie() { return movie; }
    public Budget getBudget() { return budget; }
    public Timeline getTimeline() { return timeline; }
    public ProductionTeam<Actor> getActorTeam() { return actorTeam; }
    public ProductionTeam<CrewMember> getCrewTeam() { return crewTeam; }
    public List<Scene> getScenes() { return Collections.unmodifiableList(scenes); }
    public List<ShootingSchedule> getSchedules() { return Collections.unmodifiableList(schedules); }
    public List<Location> getLocations() { return Collections.unmodifiableList(locations); }
    public List<Equipment> getEquipment() { return Collections.unmodifiableList(equipment); }

    // ---------- adding people and resources

    public void addActor(Actor actor) {
        if (findActor(actor.getId()) != null) {
            throw new IllegalArgumentException("An actor with id " + actor.getId() + " already exists");
        }
        actorTeam.addMember(actor);
    }

    public void addCrew(CrewMember crew) {
        if (findCrew(crew.getId()) != null) {
            throw new IllegalArgumentException("A crew member with id " + crew.getId() + " already exists");
        }
        crewTeam.addMember(crew);
    }

    public void addLocation(Location location) {
        if (findLocation(location.getLocationId()) != null) {
            throw new IllegalArgumentException("A location with id " + location.getLocationId() + " already exists");
        }
        locations.add(location);
    }

    public void addEquipment(Equipment item) {
        if (findEquipment(item.getEquipmentId()) != null) {
            throw new IllegalArgumentException("Equipment with id " + item.getEquipmentId() + " already exists");
        }
        equipment.add(item);
    }

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

    // ---------- scenes and schedules

    public void addScene(Scene scene) {
        if (findScene(scene.getSceneId()) != null) {
            throw new IllegalArgumentException("A scene with id " + scene.getSceneId() + " already exists");
        }
        if (!locations.contains(scene.getLocation())) {
            throw new IllegalArgumentException("Add the location " + scene.getLocation().getName()
                    + " to the production first");
        }
        scenes.add(scene);
    }

    public void addSchedule(ShootingSchedule schedule)
            throws ScheduleConflictException, ResourceNotAvailableException, BudgetExceededException {
        Scene scene = schedule.getScene();
        if (findScene(scene.getSceneId()) == null) {
            throw new IllegalArgumentException("Scene " + scene.getSceneNumber() + " is not part of this production");
        }
        for (Equipment e : scene.getEquipment()) {
            if (!equipment.contains(e)) {
                throw new IllegalArgumentException("Add the equipment " + e.getName() + " to the production first");
            }
        }

        checkConflicts(schedule);
        schedule.schedule();

        double cost = scene.getLocation().getDailyCost();
        for (Equipment e : scene.getEquipment()) {
            cost += e.getDailyCost();
        }
        try {
            budget.addExpense("Scene " + scene.getSceneNumber() + " shoot", cost);
        } catch (BudgetExceededException ex) {
            schedule.cancelSchedule();
            throw ex;
        }
        schedules.add(schedule);
    }

    private void checkConflicts(ShootingSchedule s) throws ScheduleConflictException {
        for (ShootingSchedule other : schedules) {
            if (other.getScene().getSceneId() == s.getScene().getSceneId()) {
                throw new ScheduleConflictException("Scene " + s.getScene().getSceneNumber()
                        + " is already scheduled");
            }
            if (!other.getDate().equals(s.getDate())) {
                continue;
            }
            boolean overlap = s.getStartTime().isBefore(other.getEndTime())
                    && other.getStartTime().isBefore(s.getEndTime());
            if (!overlap) {
                continue;
            }
            for (Actor a : s.getScene().getActors()) {
                if (other.getScene().getActors().contains(a)) {
                    throw new ScheduleConflictException(a.getName() + " is already in scene "
                            + other.getScene().getSceneNumber() + " at that time");
                }
            }
            for (CrewMember c : s.getScene().getCrewMembers()) {
                if (other.getScene().getCrewMembers().contains(c)) {
                    throw new ScheduleConflictException(c.getName() + " is already in scene "
                            + other.getScene().getSceneNumber() + " at that time");
                }
            }
        }
    }

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

    // ---------- phase and progress

    public void advancePhase() {
        ProductionPhase[] phases = ProductionPhase.values();
        if (currentPhase.ordinal() == phases.length - 1) {
            throw new IllegalStateException("The production is already in its last phase");
        }
        if (currentPhase == ProductionPhase.PRODUCTION) {
            for (Scene s : scenes) {
                if (s.getStatus() != SceneStatus.COMPLETED) {
                    throw new IllegalStateException("Scene " + s.getSceneNumber()
                            + " is not completed yet");
                }
            }
        }
        currentPhase = phases[currentPhase.ordinal() + 1];
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

    // ---------- saving and loading (call loadAll once, right after creating the Production)

    public void saveAll() {
        try {
            new File(DIR).mkdirs();
            List<Director> directors = new ArrayList<>();
            directors.add(movie.getDirector());
            List<Movie> movies = new ArrayList<>();
            movies.add(movie);

            new FileStorage<Director>(DIR + "director.csv").save(directors);
            new FileStorage<Movie>(DIR + "movie.csv").save(movies);
            new FileStorage<Actor>(DIR + "actors.csv").save(actorTeam.getMembers());
            new FileStorage<CrewMember>(DIR + "crew.csv").save(crewTeam.getMembers());
            new FileStorage<Location>(DIR + "locations.csv").save(locations);
            new FileStorage<Equipment>(DIR + "equipment.csv").save(equipment);
            new FileStorage<Scene>(DIR + "scenes.csv").save(scenes);
            new FileStorage<ShootingSchedule>(DIR + "schedules.csv").save(schedules);
            Files.writeString(Path.of(DIR + "phase.txt"), currentPhase.name());
        } catch (Exception e) {
            throw new RuntimeException("Could not save data: " + e.getMessage());
        }
    }

    public void loadAll() {
        try {
            if (exists("director.csv") && exists("movie.csv")) {
                Director director = new FileStorage<Director>(DIR + "director.csv")
                        .load(line -> parseDirector(line)).get(0);
                movie = new FileStorage<Movie>(DIR + "movie.csv")
                        .load(line -> parseMovie(line, director)).get(0);
            }

            actorTeam = new ProductionTeam<>();
            crewTeam = new ProductionTeam<>();
            scenes.clear();
            schedules.clear();
            locations.clear();
            equipment.clear();

            if (exists("actors.csv")) {
                for (Actor a : new FileStorage<Actor>(DIR + "actors.csv").load(line -> parseActor(line))) {
                    addActor(a);
                }
            }
            if (exists("crew.csv")) {
                for (CrewMember c : new FileStorage<CrewMember>(DIR + "crew.csv").load(line -> parseCrew(line))) {
                    addCrew(c);
                }
            }
            if (exists("locations.csv")) {
                for (Location l : new FileStorage<Location>(DIR + "locations.csv").load(line -> parseLocation(line))) {
                    addLocation(l);
                }
            }
            if (exists("equipment.csv")) {
                for (Equipment e : new FileStorage<Equipment>(DIR + "equipment.csv").load(line -> parseEquipment(line))) {
                    addEquipment(e);
                }
            }
            if (exists("scenes.csv")) {
                for (Scene s : new FileStorage<Scene>(DIR + "scenes.csv").load(line -> parseScene(line))) {
                    addScene(s);
                }
            }
            if (exists("schedules.csv")) {
                List<ShootingSchedule> loaded = new FileStorage<ShootingSchedule>(DIR + "schedules.csv")
                        .load(line -> parseSchedule(line));
                for (ShootingSchedule s : loaded) {
                    try {
                        addSchedule(s);
                    } catch (Exception e) {
                        System.out.println("Skipped schedule " + s.getScheduleId() + ": " + e.getMessage());
                    }
                }
            }
            if (exists("phase.txt")) {
                currentPhase = ProductionPhase.valueOf(Files.readString(Path.of(DIR + "phase.txt")).trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load data: " + e.getMessage());
        }
    }

    private boolean exists(String fileName) {
        return new File(DIR + fileName).exists();
    }

    private Director parseDirector(String line) {
        String[] p = line.split(",", -1);
        return new Director(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]), p[4]);
    }

    private Movie parseMovie(String line, Director director) {
        String[] p = line.split(",", -1);
        return new Movie(Integer.parseInt(p[0]), p[1], p[2], Integer.parseInt(p[3]),
                Integer.parseInt(p[4]), director);
    }

    private Actor parseActor(String line) {
        String[] p = line.split(",", -1);
        return new Actor(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]), p[4]);
    }

    private CrewMember parseCrew(String line) {
        String[] p = line.split(",", -1);
        return new CrewMember(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]), p[4], p[5]);
    }

    private Location parseLocation(String line) {
        String[] p = line.split(",", -1);
        return new Location(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]));
    }

    private Equipment parseEquipment(String line) {
        String[] p = line.split(",", -1);
        return new Equipment(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]));
    }

    private Scene parseScene(String line) {
        String[] p = line.split(",", -1);
        Location location = findLocation(Integer.parseInt(p[5]));
        if (location == null) {
            throw new IllegalArgumentException("Location " + p[5] + " not found for scene " + p[0]);
        }
        Scene scene = new Scene(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], location);
        if (!p[6].isEmpty()) {
            for (String id : p[6].split(";")) {
                Actor a = findActor(Integer.parseInt(id));
                if (a != null) {
                    scene.addActor(a);
                }
            }
        }
        if (!p[7].isEmpty()) {
            for (String id : p[7].split(";")) {
                CrewMember c = findCrew(Integer.parseInt(id));
                if (c != null) {
                    scene.addCrewMember(c);
                }
            }
        }
        if (!p[8].isEmpty()) {
            for (String id : p[8].split(";")) {
                Equipment e = findEquipment(Integer.parseInt(id));
                if (e != null) {
                    scene.addEquipment(e);
                }
            }
        }
        scene.updateProgress(Double.parseDouble(p[4]));
        scene.setStatus(SceneStatus.valueOf(p[3]));
        return scene;
    }

    private ShootingSchedule parseSchedule(String line) {
        String[] p = line.split(",", -1);
        Scene scene = findScene(Integer.parseInt(p[4]));
        if (scene == null) {
            throw new IllegalArgumentException("Scene " + p[4] + " not found for schedule " + p[0]);
        }
        return new ShootingSchedule(Integer.parseInt(p[0]), LocalDate.parse(p[1]),
                LocalTime.parse(p[2]), LocalTime.parse(p[3]), scene);
    }
}