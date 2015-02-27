package org.nstodc.database;

import org.nstodc.database.type.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Database model.
 */
public class Database {

    private int defaultMembershipAmount;
    private Set<Breed> breeds = new HashSet<Breed>();
    private Set<Dog> dogs = new HashSet<Dog>();
    private Set<Handler> handlers = new HashSet<Handler>();
    private Set<Membership> memberships = new HashSet<Membership>();
    private Set<MembershipType> membershipTypes = new HashSet<MembershipType>();
    private Set<Payment> payments = new HashSet<Payment>();
    private Set<PaymentType> paymentTypes = new HashSet<PaymentType>();
    private Set<Suburb> suburbs = new HashSet<Suburb>();
    private Set<ObedienceClass> obedienceClasses = new HashSet<ObedienceClass>();

    public Set<Breed> getBreeds() {
        return Collections.unmodifiableSet(breeds);
    }

    public void addBreed(Breed breed) throws ValidationException {
        // Unique?
        for (Breed breed1 : breeds) {
            if (breed.getBreedId() == breed1.getBreedId()) {
                throw new ValidationException("Duplicate breed: " + breed);
            }
        }
        breeds.add(breed);
    }

    public Set<Dog> getDogs() {
        return Collections.unmodifiableSet(dogs);
    }

    public void addDog(Dog dog) throws ValidationException {
        // Unique?
        for (Dog dog1 : dogs) {
            if (dog.getDogId() == dog1.getDogId()) {
                throw new ValidationException("Duplicate dog: " + dog);
            }
        }

        // Membership?
        boolean validMembership = false;
        for (Membership membership : memberships) {
            if (membership.getMembershipId() == dog.getMembershipId()) {
                validMembership = true;
                break;
            }
        }
        if (!validMembership) {
            throw new ValidationException("Invalid membership id: " + dog);
        }

        dogs.add(dog);
    }

    public void removeDog(Dog dog) {
        for (Iterator<Dog> iterator = dogs.iterator(); iterator.hasNext(); ) {
            Dog next = iterator.next();
            if (next.getDogId() == dog.getDogId()) {
                iterator.remove();
            }
        }
    }

    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    public void addHandler(Handler handler) throws ValidationException {
        // Unique?
        for (Handler handler1 : handlers) {
            if (handler.getHandlerId() == handler1.getHandlerId()) {
                throw new ValidationException("Duplicate handler: " + handler);
            }
        }

        // Membership?
        boolean validMembership = false;
        for (Membership membership : memberships) {
            if (membership.getMembershipId() == handler.getMembershipId()) {
                validMembership = true;
                break;
            }
        }
        if (!validMembership) {
            throw new ValidationException("Invalid membership id: " + handler);
        }

        handlers.add(handler);
    }

    public void removeHandler(Handler handler) {
        for (Iterator<Handler> iterator = handlers.iterator(); iterator.hasNext(); ) {
            Handler next = iterator.next();
            if (next.getHandlerId() == handler.getHandlerId()) {
                iterator.remove();
            }
        }
    }

    public Set<Membership> getMemberships() {
        return Collections.unmodifiableSet(memberships);
    }

    public void addMembership(Membership membership) throws ValidationException {
        // Unique?
        for (Membership membership1 : memberships) {
            if (membership.getMembershipId() == membership1.getMembershipId()) {
                throw new ValidationException("Duplicate membership: " + membership);
            }
        }
        memberships.add(membership);
    }

    public Set<MembershipType> getMembershipTypes() {
        if (membershipTypes == null) {
            membershipTypes = new HashSet<MembershipType>();
        }
        if (membershipTypes.isEmpty()) {
            MembershipType ordinary = new MembershipType(0);
            ordinary.setMembershipType("Ordinary");
            membershipTypes.add(ordinary);
            MembershipType associate = new MembershipType(1);
            associate.setMembershipType("Associate");
            membershipTypes.add(associate);
            MembershipType honorary = new MembershipType(2);
            honorary.setMembershipType("Honorary");
            membershipTypes.add(honorary);
            MembershipType life = new MembershipType(3);
            life.setMembershipType("Life");
            membershipTypes.add(life);
        }
        return Collections.unmodifiableSet(membershipTypes);
    }

    public Set<Payment> getPayments() {
        return Collections.unmodifiableSet(payments);
    }

    public void addPayment(Payment payment) throws ValidationException {
        // Unique?
        for (Payment payment1 : payments) {
            if (payment.getPaymentId() == payment1.getPaymentId()) {
                throw new ValidationException("Duplicate payment: " + payment);
            }
        }

        // Membership?
        boolean validPayment = false;
        for (Membership membership : memberships) {
            if (membership.getMembershipId() == payment.getMembershipId()) {
                validPayment = true;
                break;
            }
        }
        if (!validPayment) {
            throw new ValidationException("Invalid membership id: " + payment);
        }

        payments.add(payment);
    }

    public void removePayment(Payment payment) {
        for (Iterator<Payment> iterator = payments.iterator(); iterator.hasNext(); ) {
            Payment next = iterator.next();
            if (next.getPaymentId() == payment.getPaymentId()) {
                iterator.remove();
            }
        }
    }

    public Set<PaymentType> getPaymentTypes() {
        if (paymentTypes == null) {
            paymentTypes = new HashSet<PaymentType>();
        }

        if (payments.isEmpty()) {
            PaymentType cash = new PaymentType(0);
            cash.setPaymentType("Cash");
            cash.setPaymentTypeSequenceId(0);
            paymentTypes.add(cash);
            PaymentType cheque = new PaymentType(1);
            cheque.setPaymentType("Cheque");
            cheque.setPaymentTypeSequenceId(1);
            paymentTypes.add(cheque);
        }
        return Collections.unmodifiableSet(paymentTypes);
    }

    public Set<Suburb> getSuburbs() {
        return Collections.unmodifiableSet(suburbs);
    }

    public void addSuburb(Suburb suburb) throws ValidationException {
        // Unique?
        for (Suburb suburb1 : suburbs) {
            if (suburb.getSuburbId() == suburb1.getSuburbId()) {
                throw new ValidationException("Duplicate suburb: " + suburb);
            }
        }
        suburbs.add(suburb);
    }

    public Set<ObedienceClass> getObedienceClasses() {
        if (obedienceClasses == null) {
            obedienceClasses = new HashSet<ObedienceClass>();
        }
        if (obedienceClasses.isEmpty()) {
            ObedienceClass puppy = new ObedienceClass(0);
            puppy.setListSequenceId(0);
            puppy.setObedienceClass("Puppy");
            obedienceClasses.add(puppy);
            ObedienceClass a1 = new ObedienceClass(1);
            a1.setListSequenceId(1);
            a1.setObedienceClass("1A");
            obedienceClasses.add(a1);
            ObedienceClass b1 = new ObedienceClass(2);
            b1.setListSequenceId(2);
            b1.setObedienceClass("1B");
            obedienceClasses.add(b1);
            ObedienceClass c1 = new ObedienceClass(3);
            c1.setListSequenceId(3);
            c1.setObedienceClass("1C");
            obedienceClasses.add(c1);
            ObedienceClass d1 = new ObedienceClass(4);
            d1.setListSequenceId(4);
            d1.setObedienceClass("1D");
            obedienceClasses.add(d1);
            ObedienceClass e1 = new ObedienceClass(5);
            e1.setListSequenceId(5);
            e1.setObedienceClass("1E");
            obedienceClasses.add(e1);
            ObedienceClass a2 = new ObedienceClass(6);
            a2.setListSequenceId(6);
            a2.setObedienceClass("2A");
            obedienceClasses.add(a2);
            ObedienceClass b2 = new ObedienceClass(7);
            b2.setListSequenceId(7);
            b2.setObedienceClass("2B");
            obedienceClasses.add(b2);
            ObedienceClass three = new ObedienceClass(8);
            three.setListSequenceId(8);
            three.setObedienceClass("3");
            obedienceClasses.add(three);
            ObedienceClass four = new ObedienceClass(9);
            four.setListSequenceId(9);
            four.setObedienceClass("4");
            obedienceClasses.add(four);
            ObedienceClass five = new ObedienceClass(10);
            five.setListSequenceId(10);
            five.setObedienceClass("5");
            obedienceClasses.add(five);
        }
        return obedienceClasses;
    }

    public int generateNextMembershipId() {
        int n = 0;
        for (Membership membership : memberships) {
            if (membership.getMembershipId() > n) {
                n = membership.getMembershipId();
            }
        }
        return n + 1;
    }

    public int getDefaultMembershipAmount() {
        return defaultMembershipAmount;
    }

    public void setDefaultMembershipAmount(int defaultMembershipAmount) {
        this.defaultMembershipAmount = defaultMembershipAmount;
    }
}
