package org.nstodc.ui.membership;

import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Handler;
import org.nstodc.database.type.Membership;
import org.nstodc.database.type.Payment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility bundle for managing memberships.
 */
public class MembershipBundle {

    private final Membership membership;
    private final Set<Handler> handlers;
    private final Set<Dog> dogs;
    private final Set<Payment> payments;

    public MembershipBundle(Membership membership) {
        this.membership = membership;
        this.handlers = new HashSet<Handler>();
        this.dogs = new HashSet<Dog>();
        this.payments = new HashSet<Payment>();
    }

    public MembershipBundle(Membership membership, Set<Handler> handlers, Set<Dog> dogs, Set<Payment> payments) {
        this.membership = membership;
        this.handlers = handlers;
        this.dogs = dogs;
        this.payments = payments;
    }


    public Membership getMembership() {
        return membership;
    }

    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    public Set<Dog> getDogs() {
        return Collections.unmodifiableSet(dogs);
    }

    public Set<Payment> getPayments() {
        return Collections.unmodifiableSet(payments);
    }
}
