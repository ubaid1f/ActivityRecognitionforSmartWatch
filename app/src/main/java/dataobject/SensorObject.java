package dataobject;

import java.io.Serializable;

/**
 * Created by euguzns on 2018-01-19.
 */

public class SensorObject implements Serializable {

    private static final long serialVersionUID = 1234567890;
    MessageType msgType;
    String actType;

    // Array to store ACC data
//    private double[] accLinearX;
//    private double[] accLinearY;
//    private double[] accLinearZ;
    private double[] accX;
    private double[] accY;
    private double[] accZ;

    private int sensorSize = 50;

    public SensorObject() {
//        accLinearX = new double[sensorSize];
//        accLinearY = new double[sensorSize];
//        accLinearZ = new double[sensorSize];
        accX = new double[sensorSize];
        accY = new double[sensorSize];
        accZ = new double[sensorSize];
    }

    public SensorObject(MessageType msgtype) {
        this.msgType = msgtype;
    }

    public SensorObject(MessageType msgtype, String actType) {
        this.msgType = msgtype;
        this.actType = actType;
//        if (msgtype == MessageType.Data) {
//        accLinearX = new double[sensorSize];
//        accLinearY = new double[sensorSize];
//        accLinearZ = new double[sensorSize];
        accX = new double[sensorSize];
        accY = new double[sensorSize];
        accZ = new double[sensorSize];
//        }
    }

    public MessageType getMsgtype() {
        return msgType;
    }

    public String getActType() {
        return actType;
    }

//    public double[] getAccLinearX() {
//        return accLinearX;
//    }
//
//    public double[] getAccLinearY() {
//        return accLinearY;
//    }
//
//    public double[] getAccLinearZ() {
//        return accLinearZ;
//    }

    public double[] getAccX() {
        return accX;
    }

    public double[] getAccY() {
        return accY;
    }

    public double[] getAccZ() {
        return accZ;
    }

//    public void setData(double[] lx, double[] ly, double[] lz, double[] x, double[] y, double[] z) {
//        accLinearX = lx;
//        accLinearY = ly;
//        accLinearZ = lz;
//        accX = x;
//        accY = y;
//        accZ = z;
//    }

    public void setData(double[] x, double[] y, double[] z) {
        accX = x;
        accY = y;
        accZ = z;
    }
}
