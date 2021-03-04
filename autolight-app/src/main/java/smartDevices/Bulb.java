package smartDevices;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import register.Register;
import uilities.OutputWindow;

public class Bulb extends SmartDevice {
    // To interact with bulbs using coap

    private static int bulbCount = 0;
    private static ArrayList<String> IPs = new ArrayList<String>();

    private BulbSwitch bswitch;
    private BulbLuminosity luminosity;
    private String ip;

    public Bulb(String ip) {

        this.ip = ip;
        bulbCount++;
        SmartDevice.increaseCount();
        bswitch = new BulbSwitch(ip);
        luminosity = new BulbLuminosity(ip);
        IPs.add(ip);

    }

    public static ArrayList<String> getIPs() {
        return IPs;
    }

    public static void refreshCount() {
        bulbCount = 0;
    }

    public static void setAllSwitches(String status) {

        ArrayList<Bulb> bulbs = Register.getRegistredBulbs();

        for (Bulb bulb : bulbs) {
            bulb.getSwitchResource().set(status);
        }

    }

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

    public static void setAllToDesiredLuminosity(int actualLum, int desiredLum) {

        if (Bulb.getCount() > 0) {

            ArrayList<Bulb> bulbs = Register.getRegistredBulbs();
            ArrayList<LuminositySensor> lumSensors = Register.getRegistredLuminositySensors();
            int totalLum = 0;
            int externalMeanLum = 0;

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
                /* for coherency */
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

    public class BulbLuminosity {
        // Represent luminosity resource

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
