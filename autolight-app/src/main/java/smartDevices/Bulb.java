package smartDevices;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Bulb extends SmartDevice{
    // To interact with bulbs using coap 

    public static int bulbCount = 0; 

    public class BulbSwitch{
        // Represent switch resource

        private CoapClient client;

        protected BulbSwitch(String ip){
            client = new CoapClient("coap://[" + ip + "]/switch");
        }

        public CoapResponse toggle(){
            // Set on if off or off if on 
            return client.post("", MediaTypeRegistry.TEXT_PLAIN);
        }

        public String get(){
            // Get current bulb status [on/off]
            CoapResponse response = client.get();
            return response.getResponseText();
        }

        public CoapResponse set(String status){
            // Set bulb on or off
            return client.put("status="+status, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    public class BulbLuminosity{
        // Represent luminosity resource

        private CoapClient client;

        protected BulbLuminosity(String ip){
            client = new CoapClient("coap://[" + ip + "]/luminosity");
        }

        public CoapResponse increase(int amount){
            // Decrease luminosity
            return client.post("+="+amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public CoapResponse decrease(int amount){
            // Increase luminosity
            return client.post("-="+amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public String get(){
            // Get current bulb luminosity
            CoapResponse response = client.get();
            return response.getResponseText();
        }

        public CoapResponse set(int amount){
            // Set bulb luminosity
            return client.put("lum="+amount, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    public BulbSwitch bswitch;
    public BulbLuminosity luminosity;

    public Bulb(String ip){

        type = "bulb";
        id = bulbCount;
        bulbCount++;
        count++;
        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);

    }
    
}
