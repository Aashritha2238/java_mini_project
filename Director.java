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

    @Override
    public String toCsv() { return super.toCsv() + "," + specialization; }

    /** Rebuilds a Director from a line written by toCsv(). */
    public static Director fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Director(Integer.parseInt(p[0]), p[1], p[2],
                Double.parseDouble(p[3]), p[4]);
    }
}
