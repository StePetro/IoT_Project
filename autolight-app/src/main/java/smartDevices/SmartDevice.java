package smartDevices;

public abstract class SmartDevice {

    private static int count = 0;

    public static int getCount(){
        return count;
    }

    public static void refreshCount(){
        count = 0;
    }

    public static void increaseCount(){
        count++;
    }

}
