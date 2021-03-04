package register;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import smartDevices.Bulb;
import smartDevices.LuminositySensor;
import smartDevices.PresenceSensor;
import smartDevices.SmartDevice;
import uilities.OutputWindow;

public abstract class Register {

    static private ArrayList<Bulb> bulbs = new ArrayList<Bulb>();
    static private ArrayList<PresenceSensor> presenceSensors = new ArrayList<PresenceSensor>();
    static private ArrayList<LuminositySensor> luminositySensors = new ArrayList<LuminositySensor>();
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
               OutputWindow.getLog().println("[ERROR: Register] Error in observing register");
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
                OutputWindow.getLog().println("[ERROR: PRESENCE SENSOR] Possible timeout");
            }

        });
    }

    static public void refreshRegister(String content){

        OutputWindow.getLog().println("[INFO: Register] Refreshing register...");
    
        String[] descriptors = content.split(" ");

        for( String descriptor : descriptors){

            String[] type_ip = descriptor.split("@");

            if(type_ip[0].equals("BULB") && !Bulb.getIPs().contains(type_ip[1])){
                bulbs.add(new Bulb(type_ip[1]));
                OutputWindow.getLog().println("[INFO: Register] Bulb with ip: " + type_ip[1] + " added to register, total bulbs registred: " + Bulb.getCount() );
            }

            if(type_ip[0].equals("PR_SENS") && !PresenceSensor.getIPs().contains(type_ip[1])){
                presenceSensors.add(new PresenceSensor(type_ip[1]));
                OutputWindow.getLog().println("[INFO: Register] Presence sensor with ip: " + type_ip[1] + " added to register, total presence sensors registred: " + PresenceSensor.getCount() );
            }

            if(type_ip[0].equals("LUM_SENS") && !LuminositySensor.getIPs().contains(type_ip[1]) ){
                luminositySensors.add(new LuminositySensor(type_ip[1]));
                OutputWindow.getLog().println("[INFO: Register] Luminosity sensor with ip: " + type_ip[1] + " added to register, total luminosity sensors registred: " + LuminositySensor.getCount() );
            }

        }

        OutputWindow.getLog().println("[INFO: Register] Total devices registred: " + SmartDevice.getCount());

    }

    static public ArrayList<Bulb> getRegistredBulbs(){
        return bulbs;
    }

    static public ArrayList<LuminositySensor> getRegistredLuminositySensors(){
        return luminositySensors;
    }

    static public ArrayList<PresenceSensor> getRegistredPresenceSensors(){
        return presenceSensors;
    }

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

}
