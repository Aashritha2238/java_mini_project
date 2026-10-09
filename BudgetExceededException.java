/** Thrown when an expense would push total spending past the budget. */
public class BudgetExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    public BudgetExceededException(String message) {
        super(message);
    }
}
