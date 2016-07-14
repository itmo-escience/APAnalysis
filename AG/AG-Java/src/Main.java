import org.apache.commons.math3.linear.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by dn on 11.06.2016.
 */
public class Main {
    public static String PATIENT = "PATIENT";

    public static void main(String [] args)  {
        try {
            //List<Patient> result = getPatients(10, "./data.out.ch0");
            List<Patient> result = getPatients(10, "/Users/antonradice/Desktop/APAnalysis/AG/Data/sample.csv");
            //List<Patient> result = getPatients(10, "/Users/antonradice/Desktop/ExperimentData/out_100000_patients.csv");
            int top = result.size();

            // Count Evclid distance Task I.1
            /*
            Collections.sort(result, new EvclidComparator(result.get(0)));

            for(int i = 0; i < top; i++) {
                System.out.print(result.get(i).getAge() + " - " + result.get(i).getDiff() + " ; ");
            }
            System.out.println();
            */

            // Count Mahalanobis distance Task I.2

            //test inverse operation
            /*
            double [][] m = new double[][]{ {1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
            Array2DRowRealMatrix mm = new Array2DRowRealMatrix(m);
            RealMatrix mi = MatrixUtils.inverse(mm);
            System.out.println("rows: " + mm.getRowDimension() + ", cols: " + mm.getColumnDimension());
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    System.out.print(mm.getData()[i][j] + "  ");
                }
                System.out.println("");
            }
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    System.out.print(mi.getData()[i][j] + "  ");
                }
                System.out.println("");
            }
            */

            // code for covariance and inverse matrix
            System.out.println("Patients count: " + result.size());
            long total_exec_t0 = System.nanoTime();
            long covar_exec_t0 = System.nanoTime();
            double [][] matrix = findCovariance(result);
            long covar_exec_t1 = System.nanoTime();
            long inverse_exec_t0 = System.nanoTime();
            Array2DRowRealMatrix rm = new Array2DRowRealMatrix(matrix);
            RealMatrix inverseMatrix = MatrixUtils.inverse(rm);
            long inverse_exec_t1 = System.nanoTime();
            long mahal_exec_t0 = System.nanoTime();
            Collections.sort(result, new MahalanobisComparator(result.get(0), inverseMatrix.getData()));
            long mahal_exec_t1 = System.nanoTime();
            long total_exec_t1 = System.nanoTime();
            System.out.println("Total execution elapsed time: " + (total_exec_t1 - total_exec_t0) + " nanoseconds, " + (total_exec_t1 - total_exec_t0)/ 1000000000.0 + " seconds.");
            System.out.println("Covariance execution elapsed time: " + (covar_exec_t1 - covar_exec_t0) + " nanoseconds, " + (covar_exec_t1 - covar_exec_t0)/ 1000000000.0 + " seconds.");
            System.out.println("Inverse execution elapsed time: " + (inverse_exec_t1 - inverse_exec_t0) + " nanoseconds, " + (inverse_exec_t1 - inverse_exec_t0)/ 1000000000.0 + " seconds.");
            System.out.println("Mahalanobis execution elapsed time: " + (mahal_exec_t1 - mahal_exec_t0) + " nanoseconds, " + (mahal_exec_t1 - mahal_exec_t0)/ 1000000000.0 + " seconds.");

            for(int i = 0; i < top; i++) {
                System.out.print(result.get(i).getAge() + " - " + result.get(i).getDiff() + " ; ");
            }
            System.out.println();

            /*
            Patient ptn = new Patient(result.get(0));
            int amount = ptn.agHigh.size()/3;
            for (int i = 0; i < amount; i++) {
                ptn.agHigh.remove(i);
                ptn.agHigh.remove(ptn.agHigh.size() - 1);
                ptn.agLow.remove(i);
                ptn.agLow.remove(ptn.agLow.size() - 1);
            }

            //dwt(ptn, result.get(0));
            */
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // covariance matrix computation
    private static double [][] findCovariance (List<Patient> result) {
        double [][] matrix = new double [result.get(0).agHigh.size() + result.get(0).agLow.size()][result.get(0).agHigh.size() + result.get(0).agLow.size()];
        double [] ME = new double [result.get(0).agHigh.size() + result.get(0).agLow.size()];

        for(Patient ptn : result) {
                for (int i = 0; i < ptn.agHigh.size(); i++) {
                    ME[i] += ptn.agHigh.get(i);
                }
                for (int i = 0; i < ptn.agLow.size(); i++) {
                    ME[i + ptn.agHigh.size()] += ptn.agLow.get(i);
                }

        }

        for (int i = 0; i < ME.length; i++) {
            ME[i] = ME[i]/result.size();
        }

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.get(i).agHigh.size(); j++) {
                for (int k = 0; k < result.get(i).agHigh.size(); k++) {
                    matrix[j][k] += (result.get(i).agHigh.get(j) - ME[j])
                                    *(result.get(i).agHigh.get(k) - ME[k]);
                }
                for (int k = 0; k < result.get(i).agLow.size(); k++) {
                    matrix[j][k + result.get(i).agLow.size()] += (result.get(i).agHigh.get(j) - ME[j])
                                                                *(result.get(i).agLow.get(k) - ME[k + result.get(i).agLow.size()]);
                }
            }
            for (int j = 0; j < result.get(i).agLow.size(); j++) {
                for (int k = 0; k < result.get(i).agHigh.size(); k++) {
                    matrix[j + result.get(i).agLow.size()][k] += (result.get(i).agLow.get(j) - ME[result.get(i).agLow.size()+ j])
                                                                *(result.get(i).agHigh.get(k) - ME[k]);
                }
                for (int k = 0; k < result.get(i).agLow.size(); k++) {
                    matrix[j + result.get(i).agLow.size()][k + result.get(i).agLow.size()] += (result.get(i).agLow.get(j) - ME[j+ result.get(i).agLow.size()])
                                                                                            *(result.get(i).agLow.get(k) - ME[k + result.get(i).agLow.size()]);
                }
            }
        }

        for (int j = 0; j < matrix.length; j++) {
            for (int k = 0; k < matrix.length; k++) {
                matrix[j][k] = matrix[j][k] / matrix.length;
                System.out.println(matrix[j][k]);
            }
        }
         return matrix;
    }

    public static List<Patient> getPatients(int topCount, String fileName) throws IOException, ParseException {
        ArrayList<Patient> result = new ArrayList<Patient>();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String tmp = null;
        Patient tmpPatient = null;


        DateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss");
        while ((tmp = br.readLine()) != null) {
            if (tmp.startsWith(PATIENT)) {
                String [] patientInfo = tmp.split("\\s+");
                tmpPatient = new Patient(patientInfo[1], Integer.parseInt(patientInfo[2]), patientInfo[3]);
                result.add(tmpPatient);
            } else {
                String [] agInfo = tmp.split(";");
                tmpPatient.addRecord(Double.parseDouble(agInfo[1]), Double.parseDouble(agInfo[2]), df.parse(agInfo[0]));
            }

        }
        return result;
    }

    public static void dwt(Patient currentPatient, Patient patient2Check) {
        double [][] dwtMatrix = new double[currentPatient.agHigh.size()][patient2Check.agHigh.size()];
        for (int i = 0; i < currentPatient.agHigh.size(); i++) {
            for (int j = 0; j < patient2Check.agHigh.size(); j++) {
                dwtMatrix[i][j] = Math.abs(currentPatient.agHigh.get(i) - patient2Check.agHigh.get(j));
                dwtMatrix[i][j] += Math.abs(currentPatient.agLow.get(i) - patient2Check.agLow.get(j));
            }
        }

        double evclidDist[] = new double[patient2Check.agHigh.size() -  - currentPatient.agHigh.size()] ;
        for (int i = 0; i < patient2Check.agHigh.size() - currentPatient.agHigh.size(); i++) {
            evclidDist [i] = 0.0;
            for (int j = 0; j < currentPatient.agHigh.size(); j++) {
                evclidDist[i] += (patient2Check.agHigh.get(i+j) - currentPatient.agHigh.get(j)) * (patient2Check.agHigh.get(i+j) - currentPatient.agHigh.get(j));
            }
            evclidDist [i] = Math.sqrt(evclidDist [i]);
        }

        for (int i = 0; i < currentPatient.agHigh.size(); i++) {
            for (int j = 0; j < patient2Check.agHigh.size(); j++) {
                double sum = 0.0;
                int count = 0;
                if (i-1 > 0) {
                    sum = dwtMatrix[i-1][j];
                    count++;
                }
                if (j-1 > 0) {
                    if (count > 0) {
                        sum = Math.min(sum, dwtMatrix[i][j - 1]);
                    } else {
                        count++;
                    }
                }
                if (j-1 > 0 && i-1 > 0) {
                    sum = Math.min(sum, dwtMatrix[i - 1][j - 1]);
                }

                dwtMatrix[i][j] = sum + dwtMatrix[i][j];
            }
        }

        System.out.println(dwtMatrix[currentPatient.agHigh.size() - 1][patient2Check.agHigh.size() - 1]);

    }

}
