package smartDevices;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class PresenceSensor extends SmartDevice {
    // To interact with presence sensors using coap

    private static int sensorsCount = 0;
    private CoapClient client;

    public PresenceSensor(String ip) {

        sensorsCount++;
        count++;
        client = new CoapClient("coap://[" + ip + "]/presence");

        CoapObserveRelation relation = client.observe(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                System.out.println("[INFO: Register] Presence sensor signals: " + content);
            }

            public void onError() {
                System.err.println("[ERROR: Register] Error in observing presence sensor");
            }
        });

    }

    public static int getCount() {
        return sensorsCount;
    }

    public String get() {
        // Get current presence status [T/F]
        CoapResponse response = client.get();
        return response.getResponseText();
    }

}
