package production;

import resource.ResourceNotAvailableException;

public interface Schedulable {
    void schedule() throws ResourceNotAvailableException;
    void cancelSchedule();
}