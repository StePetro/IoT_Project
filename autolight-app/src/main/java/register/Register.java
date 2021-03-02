package register;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import smartDevices.Bulb;
import smartDevices.PresenceSensor;
import smartDevices.SmartDevice;

public abstract class Register {

    static private ArrayList<SmartDevice> devices = new ArrayList<SmartDevice>();
    static private CoapClient client;
    static private CoapObserveRelation observeRelation;

    static public void start(String ip){

        client = new CoapClient("coap://[" + ip + "]/register");

        observeRelation = client.observe(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                refreshRegister(content);
            }

            public void onError() {
                System.err.println("[ERROR: Register] Error in observing register");
            }

        });

    }

    static public void refreshRegister(){
        client.get(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                refreshRegister(content);
            }

            public void onError() {
                System.err.println("[ERROR: PRESENCE SENSOR] Possible timeout");
            }

        });
    }

    static public void refreshRegister(String content){

        System.out.println("[INFO: Register] Refreshing register...");

        devices.clear();
        SmartDevice.refreshCount();
        Bulb.refreshCount();
        PresenceSensor.refreshCount();
    
        String[] descriptors = content.split(" ");

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

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

}
