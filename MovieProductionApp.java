import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MovieProductionApp {
    public static void main(String[] args) {
        // 1. A movie with its director
        Director director = new Director(1, "Nolan Reed", "reed@studio.com", 90000, "Sci-Fi");
        Movie movie = new Movie(101, "Orbit", "Sci-Fi", 2026, 140, director);
        System.out.println(movie.getDetails());
        System.out.println();

        // 2. Two generic teams
        ProductionTeam<Actor> actorTeam = new ProductionTeam<>();
        actorTeam.addMember(new Actor(11, "Asha Menon", "asha@mail.com", 50000, "Captain Lena"));
        actorTeam.addMember(new Actor(12, "Ravi Kumar", "ravi@mail.com", 35000, "Engineer Dev"));
        actorTeam.addMember(new Actor(13, "Meera Nair", "meera@mail.com", 42000, "Dr. Iris"));

        ProductionTeam<CrewMember> crewTeam = new ProductionTeam<>();
        crewTeam.addMember(new CrewMember(21, "John Paul", "john@mail.com", 20000, "Cinematographer", "Camera"));
        crewTeam.addMember(new CrewMember(22, "Sara Khan", "sara@mail.com", 15000, "Sound Mixer", "Sound"));

        // 3. Polymorphism: one List<Person>, each object answers getDetails() its own way
        List<Person> everyone = new ArrayList<>();
        everyone.add(director);
        everyone.addAll(actorTeam.getMembers());
        everyone.addAll(crewTeam.getMembers());
        System.out.println("--- Everyone on the production ---");
        for (Person p : everyone) {
            System.out.println(p.getDetails());
        }
        System.out.println();

        // 4. Total fee per team
        System.out.println("Actor team total fee: " + actorTeam.getTotalFee());
        System.out.println("Crew team total fee:  " + crewTeam.getTotalFee());
        System.out.println();

        // 5. Search and remove
        Actor found = actorTeam.findByName("ravi kumar");
        System.out.println("Search 'ravi kumar': " + (found != null ? found.getDetails() : "not found"));
        actorTeam.removeMember(12);
        System.out.println("After removing id 12, actor total fee: " + actorTeam.getTotalFee());
        System.out.println("Search again: " + (actorTeam.findByName("Ravi Kumar") == null ? "not found" : "found"));
        System.out.println();

        // 6. Sort a team by fee (highest first) using a Comparator
        List<Actor> sorted = new ArrayList<>(actorTeam.getMembers());
        sorted.sort(Comparator.comparingDouble(Actor::getFee).reversed());
        System.out.println("--- Actors sorted by fee (high to low) ---");
        for (Actor a : sorted) {
            System.out.println(a.getName() + " - " + a.getFee());
        }
        System.out.println();

        // CSV output shows the Storable interface in action
        System.out.println("Movie CSV:    " + movie.toCsv());
        System.out.println("Director CSV: " + director.toCsv());
    }
}
