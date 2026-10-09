public class Director extends Person {
    private String specialization;

    public Director(int id, String name, String contact, double fee, String specialization) {
        super(id, name, contact, fee);
        this.specialization = specialization;
    }

    public String getSpecialization() { return specialization; }

    @Override
    public String getDetails() {
        return "Director #" + getId() + ": " + getName() + " (" + specialization
                + ") | Fee: " + getFee() + " | Contact: " + getContact();
    }
}
