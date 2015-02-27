package org.nstodc.database.type;

/**
 * Represents a payment for a membership.
 */
public class Payment {

    private final int paymentId;
    private final int membershipId;

    private int paymentTypeId;
    private int amount;
    private int year;
    private String paymentDate = "";
    private int receiptNumber;

    public Payment(int paymentId, int membershipId) {
        this.paymentId = paymentId;
        this.membershipId = membershipId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(int receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", membershipId=" + membershipId +
                ", paymentTypeId=" + paymentTypeId +
                ", amount=" + amount +
                ", year=" + year +
                ", paymentDate='" + paymentDate + '\'' +
                ", receiptNumber=" + receiptNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payment payment = (Payment) o;

        if (membershipId != payment.membershipId) return false;
        if (paymentId != payment.paymentId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = paymentId;
        result = 31 * result + membershipId;
        return result;
    }
}
