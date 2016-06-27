import java.util.Comparator;

/**
 * Created by dn on 12.06.2016.
 */
public class MahalanobisComparator implements Comparator<Patient> {

    private Patient template;
    private double [][] inverseCovarianceMatrix;

    public MahalanobisComparator(Patient template, double [][] inverseCovarianceMatrix) {
        this.template = template;
        this.inverseCovarianceMatrix = inverseCovarianceMatrix;
    }

    //X T * S-1 * X
    private double countDistance(Patient pretender) {
        double [] cx = new double [pretender.agHigh.size() + pretender.agLow.size()];
        for (int i =0; i < pretender.agHigh.size(); i++) {
            cx[i] = (pretender.agHigh.get(i) - template.agHigh.get(i)) * inverseCovarianceMatrix[1][i];
        }
        for (int i =0; i < pretender.agLow.size(); i++) {
            cx[i + pretender.agHigh.size()] = ((pretender.agLow.get(i) - template.agLow.get(i))
                    * inverseCovarianceMatrix[1][i + pretender.agHigh.size()]);
        }
        double result = 0.0 ;
        for (int i =0; i < template.agHigh.size(); i++) {
            result += (pretender.agHigh.get(i) -  template.agHigh.get(i)) * cx[i];
        }
        for (int i =0; i < template.agLow.size(); i++) {
            result += (pretender.agLow.get(i) -  template.agLow.get(i)) * cx[i + template.agLow.size()];
        }
        result = Math.sqrt(Math.abs(result));
        pretender.setDiff(result);
        return result;
    }

    @Override
    public int compare(Patient o1, Patient o2) {
        if (countDistance(o1) - countDistance(o2) > 0) return 1; else return -1;
    }
}
