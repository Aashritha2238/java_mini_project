public class CrewMember extends Person {
    private String role;
    private String department;

    public CrewMember(int id, String name, String contact, double fee,
                      String role, String department) {
        super(id, name, contact, fee);
        this.role = role;
        this.department = department;
    }

    public String getRole() { return role; }
    public String getDepartment() { return department; }

    @Override
    public String getDetails() {
        return "Crew #" + getId() + ": " + getName() + " (" + role + ", " + department
                + ") | Fee: " + getFee() + " | Contact: " + getContact();
    }

    @Override
    public String toCsv() { return super.toCsv() + "," + role + "," + department; }

    /** Rebuilds a CrewMember from a line written by toCsv(). */
    public static CrewMember fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new CrewMember(Integer.parseInt(p[0]), p[1], p[2],
                Double.parseDouble(p[3]), p[4], p[5]);
    }
}
