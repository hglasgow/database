package org.nstodc.database.type;

/**
 * The various obedience classes a dog can be in.
 */
public class ObedienceClass {

    private final int obedienceClassId;

    private int listSequenceId;
    private String obedienceClass = "";

    public ObedienceClass(int obedienceClassId) {
        this.obedienceClassId = obedienceClassId;
    }

    public void setListSequenceId(int listSequenceId) {
        this.listSequenceId = listSequenceId;
    }

    public int getObedienceClassId() {
        return obedienceClassId;
    }

    public int getListSequenceId() {
        return listSequenceId;
    }

    public String getObedienceClass() {
        return obedienceClass;
    }

    public void setObedienceClass(String obedienceClass) {
        this.obedienceClass = obedienceClass;
    }

    @Override
    public String toString() {
        return "ObedienceClass{" +
                "obedienceClassId=" + obedienceClassId +
                ", listSequenceId=" + listSequenceId +
                ", obedienceClass='" + obedienceClass + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObedienceClass that = (ObedienceClass) o;

        if (obedienceClassId != that.obedienceClassId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = obedienceClassId;
        return result;
    }
}
