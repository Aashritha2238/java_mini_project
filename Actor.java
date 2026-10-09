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
}
