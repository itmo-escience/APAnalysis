import java.util.Comparator;

/**
 * Created by dn on 12.06.2016.
 */
public class EvclidComparator implements Comparator<Patient> {

    Patient template;

    public EvclidComparator(Patient template) {
        this.template = template;
    }

    private double countDistance(Patient pretender) {
        double diff = 0.0;
        for (int i =0; i < pretender.agHigh.size(); i++) {
            diff += (pretender.agHigh.get(i) - template.agHigh.get(i))*(pretender.agHigh.get(i) - template.agHigh.get(i));
            diff += (pretender.agLow.get(i) - template.agLow.get(i))*(pretender.agLow.get(i) - template.agLow.get(i));
        }
        pretender.setDiff(Math.sqrt(diff));
        return pretender.getDiff();
    }

    @Override
    public int compare(Patient o1, Patient o2) {
        if (countDistance(o1) - countDistance(o2) > 0) return 1; else return -1;
    }
}
