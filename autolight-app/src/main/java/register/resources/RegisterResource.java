package register.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import register.Register;
import smartDevices.SmartDevice;
import uilities.OutputWindow;

/* CoAP resource for registration service */ 
public class RegisterResource extends CoapResource {

    /* Constructor */
    public RegisterResource(String name) {
        super(name);
    }

    /* ------------------------------------------------------ */

    /* Handles get request respondig with registred devices number */
    public void handleGET(CoapExchange exchange) {

        OutputWindow.getLog().println("[INFO: Register Resource] handling get request...");

        Response response = new Response(ResponseCode.CONTENT);
        String payload = "Total Devices Registred = " + SmartDevice.getCount() + "\n";

        response.setPayload(payload);
        exchange.respond(response);
    }

    /* Handles put request executing device registration */
    public void handlePUT(CoapExchange exchange) {

        /* INFO: in my implementation registration is IDEMPOTENT because 
           a duplicate request for an already registred device do not 
           produce changes, this is why is used a PUT insted of a POST */ 

        String[] payload = exchange.getRequestText().split("@");

        OutputWindow.getLog().println("[INFO: Register Resource] handling put...");

        boolean success = false;

        if (payload.length == 2) {

            String type = payload[0];
            String ip = payload[1];

            success = Register.addToRegister(type, ip);
        } 

        if (success) {
            /* ACKs with a CHANGED responce if registration is succesful */
            Response response = new Response(ResponseCode.CHANGED);
            exchange.respond(response);
        } else {
            Response response = new Response(ResponseCode.BAD_REQUEST);
            exchange.respond(response);
        }

    }

}
