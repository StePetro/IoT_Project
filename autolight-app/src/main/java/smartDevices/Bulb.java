package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import register.Register;
import uilities.OutputWindow;

/* Every instance represent a real bulb device 
   accessible via CoAP */
public class Bulb extends SmartDevice {

    /* Bulb class variables */

    private static int bulbCount = 0;

    // array of IPs for a quick access 
    private static ArrayList<String> IPs = new ArrayList<String>();

    /* Single Bulb variables */
    private BulbSwitch bswitch;
    private BulbLuminosity luminosity;
    private String ip;
    private int id;

    /* ------------------------------------------------------ */

    /* Constructor */
    public Bulb(String ip) {

        this.ip = ip;
        id = bulbCount;
        bulbCount++;
        SmartDevice.increaseCount();
        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);
        IPs.add(ip);

    }

    /* ------------------------------------------------------ */
    /* Static functions involving all bulbs */

    public static ArrayList<String> getIPs() {
        return IPs;
    }

    public static void refreshCount() {
        bulbCount = 0;
    }

    /* Set all bulbs ON or OFF */
    public static void setAllSwitches(String status) {

        ArrayList<Bulb> bulbs = Register.getRegistredBulbs();

        for (Bulb bulb : bulbs) {
            bulb.getSwitchResource().set(status);
        }

    }

    /* Set all bulbs lum to passed value */
    public static void SetAllLuminosities(int amount) {

        ArrayList<Bulb> bulbs = Register.getRegistredBulbs();

        for (Bulb bulb : bulbs) {
            bulb.getLuminosityResource().set(amount);
        }

        for (LuminositySensor ls : Register.getRegistredLuminositySensors()) {
            /* for coherency */
            ls.setBulbLuminosity(amount);
        }

    }

    /* Set bulbs lum as to meet desired overhaul luminosity level */
    public static void setAllToDesiredLuminosity(int actualLum, int desiredLum) {

        if (Bulb.getCount() > 0) {

            ArrayList<Bulb> bulbs = Register.getRegistredBulbs();
            ArrayList<LuminositySensor> lumSensors = Register.getRegistredLuminositySensors();
            int totalLum = 0;
            int externalMeanLum = 0;

            /* Coherency mechanism explained in last section of the slides */
            for (Bulb bulb : bulbs) {
                int bulbLum = bulb.getLuminosityResource().getValue();
                int externLum = actualLum - bulbLum;
                externalMeanLum += externLum;
                int newLum = 0;
                if (externLum < desiredLum) {
                    newLum = desiredLum - externLum;
                } else {
                    newLum = 0;
                }
                totalLum += newLum;
            }

            int meanNewLuminosity = Math.round(totalLum / Bulb.getCount());

            for (LuminositySensor ls : lumSensors) {
                // Sensor set bulb luminosity as the mean af all new luminosities to be more
                // robust
                ls.setBulbLuminosity(meanNewLuminosity);
            }

            for (Bulb bulb : bulbs) {
                // Set bulb luminosity as the mean af all new luminosities to be more robust
                bulb.getLuminosityResource().set(meanNewLuminosity);
            }

            OutputWindow.getLog()
                    .println("[INFO: ALL BULBS] Actual luminosity is " + actualLum + " and estimated external is "
                            + Math.round(externalMeanLum / Bulb.getCount())
                            + " thus new bulb luminosity value will be set to " + meanNewLuminosity);
        }

    }

    /* ------------------------------------------------------ */
    /* Getter */

    public int getID() {
        return id;
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

    /* ------------------------------------------------------ */

    /* Represent the bulb's switch resource
       and all methods usable on it */
    public class BulbSwitch {

        // client connection to resource
        private CoapClient client;

        protected BulbSwitch(String ip) {
            client = new CoapClient("coap://[" + ip + "]/switch");
        }

        public void toggle() {
            // ASYNC switch toggle 
            client.post(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Switch toggle response: " + content);
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "", MediaTypeRegistry.TEXT_PLAIN);
        }

        public void get() {
            // ASYNC Get current bulb status [on/off]
            client.get(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Switch get response: " + content);
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            });
        }

        public void set(String status) {
            // ASYNC Set bulb on or off
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Switch set response: " + content);
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "status=" + status, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    /* ------------------------------------------------------ */

    /* Represent the bulb's luminosity resource
       and all methods usable on it */
    public class BulbLuminosity {

        // client connection to resource
        private CoapClient client;
        private int value = 0;

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
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Luminosity increase response: " + content);
                    if (!content.trim().equals("")) {
                        value = Integer.parseInt(content);
                    }
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "+=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public void decrease(int amount) {
            // ASYNC Increase luminosity
            client.post(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Luminosity decrease response: " + content);
                    if (!content.trim().equals("")) {
                        value = Integer.parseInt(content);
                    }
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "-=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

        public void get() {
            // ASYNC Get current bulb luminosity
            client.get(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] Luminosity value is: " + content);
                    if (!content.trim().equals("")) {
                        value = Integer.parseInt(content);
                    }
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            });
        }

        public void set(int amount) {
            // ASYNC Set bulb luminosity
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    String content = response.getResponseText();
                    OutputWindow.getLog().println("[INFO: BULB " + ip + "] New luminosity value: " + content);
                    if (!content.trim().equals("")) {
                        value = Integer.parseInt(content);
                    }
                }

                public void onError() {
                    OutputWindow.getLog().println("[ERROR: BULB " + ip + "] Possible timeout");
                }

            }, "lum=" + amount, MediaTypeRegistry.TEXT_PLAIN);
        }

    }

}
