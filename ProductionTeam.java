import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** A generic team: ProductionTeam<Actor>, ProductionTeam<CrewMember>, ... */
public class ProductionTeam<T extends Person> {
    private List<T> members = new ArrayList<>();

    public void addMember(T member) {
        members.add(member);
    }

    /** Removes the member with the given id; throws if nobody has that id. */
    public void removeMember(int id) {
        Iterator<T> it = members.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == id) {
                it.remove();
                return;
            }
        }
        throw new IllegalArgumentException("No member with id " + id);
    }

    /** Case-insensitive search; returns null when nobody matches. */
    public T findByName(String name) {
        for (T m : members) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public double getTotalFee() {
        double total = 0;
        for (T m : members) {
            total += m.getFee();
        }
        return total;
    }

    /** Read-only view (extra getter, not in the diagram) so main can loop and sort a copy. */
    public List<T> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
