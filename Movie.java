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
}
