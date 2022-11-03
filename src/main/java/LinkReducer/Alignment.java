package LinkReducer;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class Alignment {

    private String subject;
    private String object;
    private Float measure;

    public Alignment() {
        this.measure = (float) -1;
        this.subject = "";
        this.object = "";
    }

    public Alignment(String subject, String object, Float measure) {
        this.subject = subject;
        this.object = object;
        this.measure = measure;
    }

    public String getSubject() {
        return subject;
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

    public void setObject(String object) {
        this.object = object;
    }

    public void setMeasure(Float measure) {
        this.measure = measure;
    }

    public String toJSON() {
        return "{\"entity1\": \"" + subject + "\", \"entity2\": \"" + object + "\", \"measure\": " + measure + "}";
    }

    @Override
    public String toString() {
        return "Alignment{" +
                "subject='" + subject + '\'' +
                ", object='" + object + '\'' +
                ", measure=" + measure +
                "}\n";
    }

    public boolean halfEquals(Object o) {
        if (!(o instanceof Alignment)) return false;
        Alignment alignment = (Alignment) o;
        return subject.equals(alignment.subject) && object.equals(alignment.object);
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
