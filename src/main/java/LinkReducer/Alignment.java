package LinkReducer;

import java.util.Objects;

public class Alignment {

    private String subject;
    private String predicate;
    private String object;
    private Float measure;

    public Alignment() {
        this.measure = (float) -1;
        this.subject = "";
        this.object = "";
        this.predicate = "";
    }

    public Alignment(String subject, String object, Float measure) {
        this.subject = subject;
        this.object = object;
        this.measure = measure;
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public Float getMeasure() {
        return measure;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setMeasure(Float measure) {
        this.measure = measure;
    }

    @Override
    public String toString() {
        return "Alignment{" +
                "subject='" + subject + '\'' +
                ", predicate='" + predicate + '\'' +
                ", object='" + object + '\'' +
                ", measure=" + measure +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alignment)) return false;
        Alignment alignment = (Alignment) o;
        return subject.equals(alignment.subject) && object.equals(alignment.object) && measure.equals(alignment.measure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, object, measure);
    }
}
