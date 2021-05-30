package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import uilities.AppOptions;
import uilities.OutputWindow;

/* Every instance represent a real presence 
   sensor accessible via CoAP */
public class PresenceSensor extends SmartDevice {

    /* Static variables */

    // to have a quick access to all IPs
    private static ArrayList<String> IPs = new ArrayList<String>();
    private static int sensorsCount = 0;

    /* Single sensor variables */
    private CoapClient client;
    private CoapObserveRelation observeRelation;
    private int id;
    private String ip;

    /* ------------------------------------------------------ */

    public PresenceSensor(String ip) {

        IPs.add(ip);
        this.ip = ip;
        id = sensorsCount;

        sensorsCount++;
        SmartDevice.increaseCount();

        // CoAP client connection to sensor resource
        client = new CoapClient("coap://[" + ip + "]/presence");

        // creates and observe relation with device's resource 
        observeRelation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                // Handle resource notifications switching bulbs ON or OFF              
                if (!AppOptions.manualMode) {
                    String content = response.getResponseText();
                    if (content.equals("T")) {
                        OutputWindow.getLog()
                                .println("[INFO: PRESENCE SENSOR] Someone here, switching ON all lights...");
                        Bulb.setAllSwitches("ON");
                    }
                    if (content.equals("F")) {
                        OutputWindow.getLog()
                                .println("[INFO: PRESENCE SENSOR] No one here, switching OFF all lights...");
                        Bulb.setAllSwitches("OFF");
                    }
                }
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: PRESENCE SENSOR] Error in observing presence sensor");
            }
        });

    }

    /* ------------------------------------------------------ */
    /* Static getters and utils functions */

    public static void refreshCount() {
        sensorsCount = 0;
    }

    public static int getCount() {
        return sensorsCount;
    }

    public static ArrayList<String> getIPs() {
        return IPs;
    }

    /* ------------------------------------------------------ */
    /* Coap request */

    public void get() {
        // Get current presence status [T/F]
        client.get(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                OutputWindow.getLog().println("[INFO: PRESENCE SENSOR] Get response: " + content);
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: PRESENCE SENSOR] Possible timeout");
            }

        });
    }

    /* ------------------------------------------------------ */
    /* Setters and getters */

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

    public int getID() {
        return id;
    }

    public String getIP() {
        return ip;
    }

}
