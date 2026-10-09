/** Abstract base class for everyone who works on a production. */
public abstract class Person implements Storable {
    private int id;
    private String name;
    private String contact;
    private double fee;

    public Person(int id, String name, String contact, double fee) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.fee = fee;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    public double getFee() { return fee; }

    /** Each subclass describes itself differently. */
    public abstract String getDetails();

    @Override
    public String toCsv() {
        return id + "," + name + "," + contact + "," + fee;
    }
}
