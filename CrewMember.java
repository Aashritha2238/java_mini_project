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
}
