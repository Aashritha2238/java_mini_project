package production;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

import model.Scene;
import model.SceneStatus;
import storage.Storable;
import resource.Location;
import resource.Equipment;
import resource.ResourceNotAvailableException;

public class ShootingSchedule implements Schedulable, Storable, Comparable<ShootingSchedule> {
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

    @Override
    public int compareTo(ShootingSchedule other) {

        int result = this.date.compareTo(other.date);

        if (result == 0) {
            result = this.startTime.compareTo(other.startTime);
        }

        if (result == 0) {
            result = Integer.compare(
                    this.scheduleId,
                    other.scheduleId
            );
        }

        return result;
    }

    public static final Comparator<ShootingSchedule> BY_SCENE_NUMBER = Comparator.comparingInt(schedule -> schedule.getScene().getSceneNumber());

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