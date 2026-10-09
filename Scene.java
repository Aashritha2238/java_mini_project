import java.util.*;
import java.util.stream.Collectors;

public class Scene implements Storable {
    private int sceneId;
    private int sceneNumber;
    private String description;
    private SceneStatus status = SceneStatus.PLANNED;
    private double progress = 0.0;
    private List<Actor> actors = new ArrayList<>();
    private List<CrewMember> crewMembers = new ArrayList<>();
    private Set<Equipment> equipment;
    private Location location;

    public Scene(int sceneId, int sceneNumber, String description,
                 Location location, Set<Equipment> equipment) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.sceneId = sceneId;
        this.sceneNumber = sceneNumber;
        this.description = description;
        this.location = location;
        this.equipment = (equipment == null)? new HashSet<>() : new HashSet<>(equipment);
    }
    public void addActor(Actor actor) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor cannot be null");
        }
        actors.add(actor);
    }

    public void addCrewMember(CrewMember crewMember) {
        if (crewMember == null) {
            throw new IllegalArgumentException("Crew member cannot be null");
        }
        crewMembers.add(crewMember);
    }

    public void updateProgress(double newProgress) {
        if (newProgress < 0 || newProgress > 100 || Double.isNaN(newProgress) || Double.isInfinite(newProgress)) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        this.progress = newProgress;
        if (newProgress >= 100) {
            status = SceneStatus.COMPLETED;
        } 
        else if (newProgress > 0) {
            status = SceneStatus.SHOT;
        } 
        else if (status == SceneStatus.SHOT || status == SceneStatus.COMPLETED) {
            status = SceneStatus.SCHEDULED;
        }
    }

    public String toCsv() {
        // sceneId,sceneNumber,description,status,progress,locationId,actorIds,crewIds,equipmentIds
        String locId    = (location == null) ? "" : String.valueOf(location.getLocationId());
        String actorIds = actors.stream().map(a -> String.valueOf(a.getId()))
                .collect(Collectors.joining(";"));
        String crewIds  = crewMembers.stream().map(c -> String.valueOf(c.getId()))
                .collect(Collectors.joining(";"));
        String equipIds = equipment.stream().map(e -> String.valueOf(e.getEquipmentId()))
                .collect(Collectors.joining(";"));
        return sceneId + "," + sceneNumber + "," + description.replace(",", " ") + ","
                + status + "," + progress + "," + locId + ","
                + actorIds + "," + crewIds + "," + equipIds;
    }

    public int getSceneId() {
        return sceneId;
    }

    public int getSceneNumber() {
        return sceneNumber;
    }

    public SceneStatus getStatus() {
        return status;
    }

    public void setStatus(SceneStatus s) {
        if (s == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = s;
    }

    public double getProgress() {
        return progress;
    }

    public Location getLocation() {
        return location;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public List<CrewMember> getCrewMembers() {
        return crewMembers;
    }

    public Set<Equipment> getEquipment() {
        return equipment;
    }
}