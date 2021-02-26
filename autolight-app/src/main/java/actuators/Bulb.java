package actuators;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Bulb{
    // To interact with bulbs using coap 

    public class BulbSwitch{
        // Represent switch resource

        private CoapClient client;

        protected BulbSwitch(String ip){
            client = new CoapClient("coap://" + ip + "/switch");
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

    }

    public class BulbLuminosity{
        // Represent luminosity resource

        private CoapClient client;

        protected BulbLuminosity(String ip){
            client = new CoapClient("coap://" + ip + "/luminosity");
        }

    }

    public BulbSwitch bswitch;
    public BulbLuminosity luminosity;

    public Bulb(String ip){

        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);

    }
    
}
