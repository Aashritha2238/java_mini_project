import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Equipment implements Bookable, Storable {
    private int equipmentId;
    private String name;
    private String type;
    private double dailyCost;
    private Set<LocalDate> bookedDates = new HashSet<>();
        public Equipment(int equipmentId, String name, String type, double dailyCost) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.type = type;
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
    public String toCsv() {
        // equipmentId,name,type,dailyCost,date;date
        String dates = bookedDates.stream().sorted().map(LocalDate::toString)
                .collect(Collectors.joining(";"));
        return equipmentId + "," + name + "," + type.replace(",", " ") + ","
                + dailyCost + "," + dates;
    }

     // Used by FileStorage.load(...) to rebuild an object from one CSV line.
    public static Equipment fromCsv(String line) {
        String[] p = line.split(",", -1);
        Equipment obj = new Equipment(Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3]));
        if (!p[4].isBlank()) {
            for (String d : p[4].split(";")) {
                obj.bookedDates.add(LocalDate.parse(d));
            }
        }
        return obj;
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


