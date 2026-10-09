import java.util.HashMap;
import java.util.Map;

public class Budget {
    private double totalAmount;
    private double spentAmount;
    private Map<String, Double> expenses = new HashMap<>();

    public Budget(double totalAmount) {
        if (totalAmount < 0) {
            throw new IllegalArgumentException("Total budget cannot be negative");
        }
        this.totalAmount = totalAmount;
    }

    /** Adds the expense, or throws BudgetExceededException if it would pass the total. */
    public void addExpense(String category, double amt) throws BudgetExceededException {
        if (amt < 0) {
            throw new IllegalArgumentException("Expense cannot be negative");
        }
        if (spentAmount + amt > totalAmount) {
            throw new BudgetExceededException("Expense of " + amt + " for '" + category
                    + "' exceeds the budget. Remaining: " + getRemaining());
        }
        expenses.merge(category, amt, Double::sum);
        spentAmount += amt;
    }

    public double getRemaining() { return totalAmount - spentAmount; }

    // extra getters (not in the diagram) for the menu's "show budget"
    public double getTotalAmount() { return totalAmount; }
    public double getSpentAmount() { return spentAmount; }
    public Map<String, Double> getExpenses() { return new HashMap<>(expenses); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total: %.2f | Spent: %.2f | Remaining: %.2f%n",
                totalAmount, spentAmount, getRemaining()));
        for (Map.Entry<String, Double> e : expenses.entrySet()) {
            sb.append(String.format("  %-20s %.2f%n", e.getKey(), e.getValue()));
        }
        return sb.toString();
    }
}
