package smartDevices;

public abstract class SmartDevice {

    public static int count = 0;

    protected String type;
    protected int id;

    public String toString(){
        return type + "-" + id;
    }

    public String getType(){
        return type;
    }

    public int getID(){
        return id;
    }

}
