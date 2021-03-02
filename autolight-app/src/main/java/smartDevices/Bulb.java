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
        SmartDevice.increaseCount();
        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);

    }

    public static void refreshCount(){
        bulbCount = 0;
    }

    public static void setAllSwitches(String status) {

        ArrayList<SmartDevice> register = Register.getRegistredDevices();

        for (SmartDevice device : register) {
            if (device.getClass() == Bulb.class) {
                Bulb bulb = (Bulb) device;
                bulb.getSwitchResource().set(status);
            }
        }

    }

    public static void setAllToDesiredLuminosity(int actualLum, int desiredLum) {

        ArrayList<SmartDevice> register = Register.getRegistredDevices();
        int newLum = 0;
        

        for (SmartDevice device : register) {
            if (device.getClass() == Bulb.class) {
                Bulb bulb = (Bulb) device;
                int bulbLum = bulb.getLuminosityResource().getValue();
                if(actualLum<desiredLum){
                    if(bulbLum+desiredLum-actualLum < 100){
                        newLum = bulbLum+desiredLum-actualLum;
                    }else{
                        newLum = 100;
                    }
                }else{
                    if(bulbLum-(actualLum-desiredLum) > 0){
                        newLum = bulbLum-(actualLum-desiredLum);
                    }else{
                        newLum = 0;
                    }
                }
                bulb.getLuminosityResource().set(newLum);
                System.out.println("B "+bulbLum+" D "+desiredLum+" A "+actualLum+" N "+newLum);
            }
        }

        for (SmartDevice device : register) {
            /* for coherency */
            if (device.getClass() == LuminositySensor.class) {
                LuminositySensor ls = (LuminositySensor) device;
                ls.setBulbLuminosity(newLum);
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

        public void toggle() {
            // ASYNC switch toggle
            client.post(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Switch toggle response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "", MediaTypeRegistry.TEXT_PLAIN);
        }

        public void get() {
            // ASYNC Get current bulb status [on/off]
            client.get(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Switch get response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            });
        }

        public void set(String status) {
            // ASYNC Set bulb on or off
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Switch set response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "status=" + status, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    public class BulbLuminosity {
        // Represent luminosity resource

        private CoapClient client;
        private int value;

        protected BulbLuminosity(String ip) {
            client = new CoapClient("coap://[" + ip + "]/luminosity");
            get();
        }

        public int getValue() {
            return value;
        }

        public void increase(int amount) {
            // ASYNC Decrease luminosity
            client.post(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Luminosity increase response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            },"+=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public void decrease(int amount) {
            // ASYNC Increase luminosity
            client.post(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Luminosity decrease response: " + content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            },"-=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public void get() {
            // ASYNC Get current bulb luminosity
            client.get(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] Luminosity value is: " + content);
                    value = Integer.parseInt(content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            });            
        }

        public void set(int amount) {
            // ASYNC Set bulb luminosity
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    System.out.println("[INFO: BULB " + ip + "] New luminosity value: " + content);
                    value = Integer.parseInt(content);
                }

                public void onError() {
                    System.err.println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            },"lum=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

}
