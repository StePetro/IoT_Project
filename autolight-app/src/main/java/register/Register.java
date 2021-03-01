package register;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import smartDevices.Bulb;
import smartDevices.PresenceSensor;
import smartDevices.SmartDevice;

public class Register {

    static private ArrayList<SmartDevice> devices = new ArrayList<SmartDevice>();
    private CoapClient client;

    public Register(String ip) {

        client = new CoapClient("coap://[" + ip + "]/register");
        refreshRegister();

    }

    public void refreshRegister(){

        System.out.println("[INFO: Register] Refreshing register...");
    
        CoapResponse response = client.get();
        String[] descriptors = response.getResponseText().split(" ");

        for( String descriptor : descriptors){

            String[] type_ip = descriptor.split("@");

            if(type_ip[0].equals("BULB")){
                devices.add(new Bulb(type_ip[1]));
                System.out.println("[INFO: Register] Bulb with ip: " + type_ip[1] + " added to register, total bulbs registred: " + Bulb.getCount() );
            }

            if(type_ip[0].equals("PR_SENS")){
                devices.add(new PresenceSensor(type_ip[1]));
                System.out.println("[INFO: Register] Presence sensor with ip: " + type_ip[1] + " added to register, total presence sensors registred: " + PresenceSensor.getCount() );
            }

        }

        System.out.println("[INFO: Register] Total devices registred: " + devices.size());

    }

    static public ArrayList<SmartDevice> getRegistredDevices(){
        return devices;
    }

}
