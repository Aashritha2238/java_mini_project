import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Equipment implements Bookable, Storable {
    private int equipmentId;
    private String name;
    private String type;
    private double dailyCost;
    private Set<LocalDate> bookedDates = new HashSet<>();
public void book(LocalDate date) throws ResourceNotAvailableException {
        if(!isAvailable(date)){
            throw new ResourceNotAvailableException(name + " is already booked on " + date);
        }
        bookedDates.add(date);
        }
    public boolean cancelBooking(LocalDate date){
        return bookedDates.remove(date);
    }
    public boolean isAvailable(LocalDate date){
        return !bookedDates.contains(date);
    }
    public String toCsv() {
        // equipmentId,name,type,dailyCost,date;date
        String dates = bookedDates.stream().sorted().map(LocalDate::toString)
                .collect(Collectors.joining(";"));
        return equipmentId + "," + name + "," + type.replace(",", " ") + ","
                + dailyCost + "," + dates;
    }
    public int getEquipmentId() { 
        return equipmentId;
     }
    public String getName() { 
        return name;
     }
    public double getDailyCost() {
         return dailyCost; 
        }
}


