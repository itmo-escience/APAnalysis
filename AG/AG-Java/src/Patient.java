import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by dn on 11.06.2016.
 */
public class Patient {
    ArrayList<Double> agHigh = new ArrayList<Double>();
    ArrayList<Double> agLow = new ArrayList<Double>();
    String sex;
    int age;
    Date startTime;
    String type;
    double diff;

    public Patient (Patient ptn){
        this.agHigh = new ArrayList<Double>(ptn.agHigh);
        this.agLow = new ArrayList<Double>(ptn.agLow);
        sex = ptn.getSex();
        age = ptn.getAge();
        startTime = ptn.getStartTime();
        type = ptn.getType();
        diff = ptn.getDiff();
    }

    public Patient(String sex, int age, String type) {
        this.sex = sex;
        this.type = type;
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Double [] getAgHigh() {
        return (Double [])agHigh.toArray();
    }

    public Double [] getAgLow() {
        return (Double [])agLow.toArray();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addRecord(double hiAG, double lowAG, Date time) {
        agHigh.add(hiAG);
        agLow.add(lowAG);
    }

    public double getDiff() {
        return diff;
    }

    public void setDiff(double diff) {
        this.diff = diff;
    }
}
