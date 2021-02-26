package resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class RegisterResource extends CoapResource {
	public RegisterResource(String name) {
		super(name);
		setObservable(true);
 	}
 	public void handleGET(CoapExchange exchange) {
 		
 		Response response = new Response(ResponseCode.CONTENT);
 		
 		if( exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_XML ) {
 			response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_XML);
 			response.setPayload("<value>10</value>");
 		} else if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
 			response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
 			response.setPayload("{\"value\":\"10\"}"); 			
 		} else {
 			response.setPayload("Hello");
 		}
 		
		exchange.respond(response);
 	}
	public void handlePOST(CoapExchange exchange) {
		
		byte[] request = exchange.getRequestPayload();
		
		String s = new String(request);
				
		Integer value = Integer.parseInt(s);
		
		Double resp = Math.sqrt(value);
		
		s=Double.toString(resp);
		
		Response response = new Response(ResponseCode.CONTENT);
		
		response.setPayload(s);
				
		exchange.respond(response);
 	}
}

