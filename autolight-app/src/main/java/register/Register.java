package register;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapServer;

import register.resources.RegisterResource;
import smartDevices.Bulb;
import smartDevices.LuminositySensor;
import smartDevices.PresenceSensor;
import smartDevices.SmartDevice;
import uilities.OutputWindow;

/* Non istantiable class to avoid starting multiple register instances */
public abstract class Register {
    
    /* Variables */ 
    static private ArrayList<Bulb> bulbs = new ArrayList<Bulb>();
    static private ArrayList<PresenceSensor> presenceSensors = new ArrayList<PresenceSensor>();
    static private ArrayList<LuminositySensor> luminositySensors = new ArrayList<LuminositySensor>();
    static private CoapObserveRelation observeRelation;
    static private CoapServer server;

    /* ------------------------------------------------------ */
    /* CoAP server that handles register resource */ 

    static public void start() {

        server = new CoapServer();
        server.add(new RegisterResource("register"));
        server.start();

    }

    static public CoapServer getServer() {
        return server;
    }

    /* ------------------------------------------------------ */

    /* Concludes registration with devices istantiation */ 
    static public boolean addToRegister(String type, String ip) {

        boolean success = false;

        if (type.equals("BULB") && !Bulb.getIPs().contains(ip)) {
            bulbs.add(new Bulb(ip));
            OutputWindow.getLog().println("[INFO: Register] Bulb with ip: " + ip
                    + " added to register, total bulbs registred: " + Bulb.getCount());
            success = true;
        }

        if (type.equals("PR_SENS") && !PresenceSensor.getIPs().contains(ip)) {
            presenceSensors.add(new PresenceSensor(ip));
            OutputWindow.getLog().println("[INFO: Register] Presence sensor with ip: " + ip
                    + " added to register, total presence sensors registred: " + PresenceSensor.getCount());
            success = true;
        }

        if (type.equals("LUM_SENS") && !LuminositySensor.getIPs().contains(ip)) {
            luminositySensors.add(new LuminositySensor(ip));
            OutputWindow.getLog().println("[INFO: Register] Luminosity sensor with ip: " + ip
                    + " added to register, total luminosity sensors registred: " + LuminositySensor.getCount());
            success = true;
        }

        OutputWindow.getLog().println("[INFO: Register] Total devices registred: " + SmartDevice.getCount());
        return success;

    }

    /* ------------------------------------------------------ */
    /* Getters */

    static public ArrayList<Bulb> getRegistredBulbs() {
        return bulbs;
    }

    static public ArrayList<LuminositySensor> getRegistredLuminositySensors() {
        return luminositySensors;
    }

    static public ArrayList<PresenceSensor> getRegistredPresenceSensors() {
        return presenceSensors;
    }

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

}
