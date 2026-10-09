public class Actor extends Person {
    private String characterName;

    public Actor(int id, String name, String contact, double fee, String characterName) {
        super(id, name, contact, fee);
        this.characterName = characterName;
    }

    public String getCharacterName() { return characterName; }

    @Override
    public String getDetails() {
        return "Actor #" + getId() + ": " + getName() + " plays \"" + characterName
                + "\" | Fee: " + getFee() + " | Contact: " + getContact();
    }

    @Override
    public String toCsv() { return super.toCsv() + "," + characterName; }

    /** Rebuilds an Actor from a line written by toCsv(). */
    public static Actor fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Actor(Integer.parseInt(p[0]), p[1], p[2],
                Double.parseDouble(p[3]), p[4]);
    }
}
