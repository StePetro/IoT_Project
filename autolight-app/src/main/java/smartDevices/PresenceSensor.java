package smartDevices;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class PresenceSensor extends SmartDevice {
    // To interact with presence sensors using coap

    private static int sensorsCount = 0;
    private CoapClient client;
    private CoapObserveRelation observeRelation;

    public PresenceSensor(String ip) {

        sensorsCount++;
        count++;
        client = new CoapClient("coap://[" + ip + "]/presence");

        observeRelation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                if(content.equals("T")){
                    System.out.println("[INFO: Presence Sensor] Someone here, switching ON all lights...");
                    Bulb.setAll("ON");
                }
                if(content.equals("F")){
                    System.out.println("[INFO: Presence Sensor] No one here, switching OFF all lights...");
                    Bulb.setAll("OFF");                    
                }
            }

            public void onError() {
                System.err.println("[ERROR: Presence Sensor] Error in observing presence sensor");
            }
        });

    }

    public static int getCount() {
        return sensorsCount;
    }

    public CoapObserveRelation getObserveRelation() {
        return observeRelation;
    }

    public String get() {
        // Get current presence status [T/F]
        CoapResponse response = client.get();
        return response.getResponseText();
    }

}
