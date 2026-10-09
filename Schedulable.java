public interface Schedulable {
    void schedule() throws ResourceNotAvailableException;
    void cancelSchedule();
}