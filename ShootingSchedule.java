import java.time.LocalDate;
import java.time.LocalTime;

public class ShootingSchedule implements Schedulable, Storable {
    private int scheduleId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Scene scene;

    public ShootingSchedule(int scheduleId, LocalDate date, LocalTime startTime,
                            LocalTime endTime, Scene scene) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.scheduleId = scheduleId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scene = scene;
    }

    public int getScheduleId() { return scheduleId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Scene getScene() { return scene; }

    @Override
    public void schedule() throws ResourceNotAvailableException {
        Location loc = scene.getLocation();
        if (!loc.isAvailable(date)) {
            throw new ResourceNotAvailableException("Location " + loc.getName() + " is booked on " + date);
        }
        for (Equipment e : scene.getEquipment()) {
            if (!e.isAvailable(date)) {
                throw new ResourceNotAvailableException("Equipment " + e.getName() + " is booked on " + date);
            }
        }
        loc.book(date);
        for (Equipment e : scene.getEquipment()) {
            e.book(date);
        }
        if (scene.getStatus() == SceneStatus.PLANNED) {
            scene.setStatus(SceneStatus.SCHEDULED);
        }
    }

    @Override
    public void cancelSchedule() {
        scene.getLocation().cancelBooking(date);
        for (Equipment e : scene.getEquipment()) {
            e.cancelBooking(date);
        }
        if (scene.getStatus() == SceneStatus.SCHEDULED) {
            scene.setStatus(SceneStatus.PLANNED);
        }
    }

    @Override
    public String toCsv() {
        return scheduleId + "," + date + "," + startTime + "," + endTime + "," + scene.getSceneId();
    }
}