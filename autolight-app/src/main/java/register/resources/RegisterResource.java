package register.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import register.Register;
import smartDevices.SmartDevice;
import uilities.OutputWindow;

public class RegisterResource extends CoapResource {

    public RegisterResource(String name) {
        super(name);
    }

    public void handleGET(CoapExchange exchange) {

        OutputWindow.getLog().println("[INFO: Register Resource] handling get request...");

        Response response = new Response(ResponseCode.CONTENT);
        String payload = "Total Devices Registred = " + SmartDevice.getCount() + "\n";

        response.setPayload(payload);
        exchange.respond(response);
    }

    public void handlePUT(CoapExchange exchange) {

        String[] payload = exchange.getRequestText().split("@");

        OutputWindow.getLog().println("[INFO: Register Resource] handling put...");

        boolean success = false;

        if (payload.length == 2) {

            String type = payload[0];
            String ip = payload[1];

            success = Register.addToRegister(type, ip);
        } 

        if (success) {
            Response response = new Response(ResponseCode.CHANGED);
            exchange.respond(response);
        } else {
            Response response = new Response(ResponseCode.BAD_REQUEST);
            exchange.respond(response);
        }

    }

}
