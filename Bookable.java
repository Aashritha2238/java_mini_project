import java.time.LocalDate;

public interface Bookable {
    void book(LocalDate date) throws ResourceNotAvailableException;
    boolean cancelBooking(LocalDate date);
    boolean isAvailable(LocalDate date);

}
