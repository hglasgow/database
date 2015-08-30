package org.nstodc.database.type;

/**
 * Types of payment.
 */
public class PaymentType {

    private final int paymentTypeId;

    private int paymentTypeSequenceId;
    private String paymentType = "";

    public PaymentType(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public void setPaymentTypeSequenceId(int paymentTypeSequenceId) {
        this.paymentTypeSequenceId = paymentTypeSequenceId;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public int getPaymentTypeSequenceId() {
        return paymentTypeSequenceId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return "PaymentType{" +
                "paymentTypeId=" + paymentTypeId +
                ", paymentTypeSequenceId=" + paymentTypeSequenceId +
                ", paymentType='" + paymentType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentType that = (PaymentType) o;

        if (paymentTypeId != that.paymentTypeId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return paymentTypeId;
    }
}
