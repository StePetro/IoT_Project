package smartDevices;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class LuminositySensor extends SmartDevice {
    // To interact with presence sensors using coap

    private static int sensorsCount = 0;
    private CoapClient client;
    private CoapObserveRelation observeRelation;
    private int desiredLuminosity = 100;

    public LuminositySensor(String ip) {

        sensorsCount++;
        SmartDevice.increaseCount();
        client = new CoapClient("coap://[" + ip + "]/luminosity");

        observeRelation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                int actualLuminosity = Integer.parseInt(response.getResponseText());
                System.out.println("[INFO: LUMINOSITY SENSOR] Actual luminosity is "+actualLuminosity+" instead desired is "+desiredLuminosity);
                Bulb.setAllToDesiredLuminosity(actualLuminosity, desiredLuminosity);
            }

            public void onError() {
                System.err.println("[ERROR: LUMINOSITY SENSOR] Error in observing luminosity sensor");
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
        return desiredLuminosity;
    }

    public void setDesiredLuminosity(int dl) {
        desiredLuminosity = dl;
    }

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

    public void get() {
        // Get current luminosity
        client.get(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                System.out.println("[INFO: LUMINOSITY SENSOR] Get response: " + content);
            }

            public void onError() {
                System.err.println("[ERROR: LUMINOSITY SENSOR] Possible timeout");
            }

        });
    }

	public void setBulbLuminosity(int lum) {
            // ASYNC Set luminosity sensor bulb luminosity for coherency
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: LUMINOSITY SENSOR] Bulb luminosity set response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: LUMINOSITY SENSOR] Possible timeout");
                }

            }, "bulb=" + lum, MediaTypeRegistry.TEXT_PLAIN);
        
	}

}
