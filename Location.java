import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Location implements Bookable, Storable { 
    private int locationId;
    private String name;
    private String address;
    private double dailyCost; 
    private Set<LocalDate> bookedDates = new HashSet<>(); // HashSet can't store duplicates so we can't book 2 same location in a same date.
    public Location(int locationId, String name, String address, double dailyCost) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.dailyCost = dailyCost;
    }
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
    public String toCsv(){
        // locationId,name,address,dailyCost,date;date
        String dates = bookedDates.stream().sorted().map(LocalDate::toString)
                .collect(Collectors.joining(";"));
        return locationId + "," + name + "," + address.replace(",", " ") + ","
                + dailyCost + "," + dates;
    }
    public int getLocationId() { 
        return locationId;
     }
    public String getName() { 
        return name;
     }
    public double getDailyCost() { 
        return dailyCost;
     }

}


