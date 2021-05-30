package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import uilities.AppOptions;
import uilities.OutputWindow;

/* Every instance represent a real luminosity 
   sensor accessible via CoAP */
public class LuminositySensor extends SmartDevice {

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

    /* Constructor */ 
    public LuminositySensor(String ip) {

        IPs.add(ip);
        this.ip = ip;
        id = sensorsCount;
        sensorsCount++;
        SmartDevice.increaseCount();

        // CoAP client connection to sensor resource 
        client = new CoapClient("coap://[" + ip + "]/luminosity");

        // creates and observe relation with device's resource 
        observeRelation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                if (!AppOptions.manualMode) {
                    String content = response.getResponseText();
                    int actualLuminosity = -1;
                    if (!content.trim().equals("")) {
                        actualLuminosity = Integer.parseInt(content);
                    }
                    OutputWindow.getLog().println("[INFO: LUMINOSITY SENSOR] Actual luminosity is " + actualLuminosity
                            + " instead desired is " + AppOptions.desiredLum);

                    // a change in luminosity triggers the automatic luminosity adaptation
                    Bulb.setAllToDesiredLuminosity(actualLuminosity, AppOptions.desiredLum); 
                }
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: LUMINOSITY SENSOR] Error in observing luminosity sensor");
            }
        });

    }

    /* ------------------------------------------------------ */
    /* Static getters and utils functions */

    public static ArrayList<String> getIPs() {
        return IPs;
    }

    public static void refreshCount() {
        sensorsCount = 0;
    }

    public static int getCount() {
        return sensorsCount;
    }

    /* ------------------------------------------------------ */
    /* Coap requests */

    public void get() {
        // ASYNC Get current luminosity
        client.get(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                OutputWindow.getLog().println("[INFO: LUMINOSITY SENSOR] Get response: " + content);
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: LUMINOSITY SENSOR] Possible timeout");
            }

        });
    }

    public void setBulbLuminosity(int lum) {
        // ASYNC Set luminosity sensor bulb luminosity for coherency
        client.put(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                OutputWindow.getLog().println("[INFO: LUMINOSITY SENSOR] Bulb luminosity set response: " + content);
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: LUMINOSITY SENSOR] Possible timeout");
            }

        }, "bulb=" + lum, MediaTypeRegistry.TEXT_PLAIN);

    }

    /* ------------------------------------------------------ */
    /* Setters and getters */

    public int getID() {
        return id;
    }

    public String getIP() {
        return ip;
    }

    public int getDesiredLuminosity() {
        return AppOptions.desiredLum;
    }

    public void setDesiredLuminosity(int dl) {
        AppOptions.desiredLum = dl;
    }

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

}
