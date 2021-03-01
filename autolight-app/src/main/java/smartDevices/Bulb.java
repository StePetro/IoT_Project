package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import register.Register;

public class Bulb extends SmartDevice {
    // To interact with bulbs using coap

    private static int bulbCount = 0;

    private BulbSwitch bswitch;
    private BulbLuminosity luminosity;
    private String ip;

    public Bulb(String ip) {

        this.ip = ip;
        bulbCount++;
        count++;
        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);

    }

    public static void setAll(String status) {

        ArrayList<SmartDevice> register = Register.getRegistredDevices();

        for (SmartDevice device : register) {
            if (device.getClass() == Bulb.class) {
                Bulb bulb = (Bulb) device;
                bulb.getSwitchResource().set(status);;
            }
        }

    }

    public BulbSwitch getSwitchResource() {
        return bswitch;
    }

    public String getIP() {
        return ip;
    }

    public BulbLuminosity getLuminosityResource() {
        return luminosity;
    }

    public static int getCount() {
        return bulbCount;
    }

    public class BulbSwitch {
        // Represent switch resource

        private CoapClient client;

        protected BulbSwitch(String ip) {
            client = new CoapClient("coap://[" + ip + "]/switch");
        }

        public CoapResponse toggle() {
            // Set on if off or off if on
            return client.post("", MediaTypeRegistry.TEXT_PLAIN);
        }

        public void get() {
            // ASYNC Get current bulb status [on/off]
            client.get(new CoapHandler() {
                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    if(!content.isBlank()){
                        System.out.println("[INFO: BULB " + ip + "] " + content);
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] ERROR");
                }
            });
        }

        public void set(String status) {
            // ASYNC Set bulb on or off
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] ERROR");
                }

            }, "status=" + status, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    public class BulbLuminosity {
        // Represent luminosity resource

        private CoapClient client;

        protected BulbLuminosity(String ip) {
            client = new CoapClient("coap://[" + ip + "]/luminosity");
        }

        public CoapResponse increase(int amount) {
            // Decrease luminosity
            return client.post("+=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public CoapResponse decrease(int amount) {
            // Increase luminosity
            return client.post("-=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public String get() {
            // Get current bulb luminosity
            CoapResponse response = client.get();
            return response.getResponseText();
        }

        public CoapResponse set(int amount) {
            // Set bulb luminosity
            return client.put("lum=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

}
