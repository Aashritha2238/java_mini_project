import java.util.List;

public class Movie implements Storable {
    private int movieId;
    private String title;
    private String genre;
    private int releaseYear;
    private int durationMinutes;
    private Director director;

    public Movie(int movieId, String title, String genre, int releaseYear,
                 int durationMinutes, Director director) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.director = director;
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getReleaseYear() { return releaseYear; }
    public int getDurationMinutes() { return durationMinutes; }
    public Director getDirector() { return director; }

    public String getDetails() {
        return "Movie #" + movieId + ": " + title + " (" + releaseYear + ", " + genre + ", "
                + durationMinutes + " min) directed by " + director.getName();
    }

    @Override
    public String toCsv() {
        return movieId + "," + title + "," + genre + "," + releaseYear + ","
                + durationMinutes + "," + director.getId();
    }

    /** Rebuilds a Movie; the director is looked up by id in the already-loaded list. */
    public static Movie fromCsv(String line, List<Director> directors) {
        String[] p = line.split(",", -1);
        int dirId = Integer.parseInt(p[5]);
        Director d = null;
        for (Director x : directors) {
            if (x.getId() == dirId) { d = x; break; }
        }
        return new Movie(Integer.parseInt(p[0]), p[1], p[2],
                Integer.parseInt(p[3]), Integer.parseInt(p[4]), d);
    }
}
