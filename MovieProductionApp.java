import java.time.LocalDate;

public class MovieProductionApp {
    public static void main(String[] args) {
        Director director = new Director(1, "Nolan Reed", "reed@studio.com", 90000, "Sci-Fi");
        Movie movie = new Movie(101, "Orbit", "Sci-Fi", 2026, 140, director);
        Budget budget = new Budget(500000);
        Timeline timeline = new Timeline(LocalDate.now(), LocalDate.now().plusMonths(6));

        Production production = new Production(1, "Orbit Production", movie, budget, timeline);
        production.loadAll();

        ConsoleMenu menu = new ConsoleMenu(production);
        menu.start();
    }
}