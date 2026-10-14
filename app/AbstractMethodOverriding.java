package app;

import model.Actor;
import model.Person;

public class AbstractMethodOverriding {
    public static void main(String[] args) {

        Person person = new Actor(
            1, "Ravi", "9876543210", 50000, "Hero"
        );

        System.out.println(person.getResponsibilities());
    }
}