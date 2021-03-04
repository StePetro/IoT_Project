package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import uilities.AppOptions;
import uilities.OutputWindow;

public class LuminositySensor extends SmartDevice {
    // To interact with presence sensors using coap

    private static ArrayList<String> IPs = new ArrayList<String>();

    private static int sensorsCount = 0;
    private CoapClient client;
    private CoapObserveRelation observeRelation;

    public LuminositySensor(String ip) {

        IPs.add(ip);
        sensorsCount++;
        SmartDevice.increaseCount();
        client = new CoapClient("coap://[" + ip + "]/luminosity");

        observeRelation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                int actualLuminosity = -1;
                if(!content.trim().equals("")){
                    actualLuminosity = Integer.parseInt(content);
                }
                OutputWindow.getLog().println("[INFO: LUMINOSITY SENSOR] Actual luminosity is "+actualLuminosity+" instead desired is "+ AppOptions.desiredLum);
                if(!AppOptions.manualMode){
                    Bulb.setAllToDesiredLuminosity(actualLuminosity, AppOptions.desiredLum);
                }
            }

            public void onError() {
                OutputWindow.getLog().println("[ERROR: LUMINOSITY SENSOR] Error in observing luminosity sensor");
            }
        });

    }

    public static void refreshCount(){
        sensorsCount = 0;
    }

    public static int getCount() {
        return sensorsCount;
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

    public void get() {
        // Get current luminosity
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

	public static ArrayList<String> getIPs() {
		return IPs;
	}

}
