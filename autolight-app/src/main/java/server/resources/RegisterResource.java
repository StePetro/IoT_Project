package server.resources;

import smartDevices.*;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class RegisterResource extends CoapResource {
	// Register of devices connected to the app

	private ArrayList<SmartDevice> devices = new ArrayList<SmartDevice>();

	public ArrayList<SmartDevice> getDevices(){
		return devices;
	}

	public RegisterResource(String name) {

		super(name);
		setObservable(true);

	 }
	 
 	public void handleGET(CoapExchange exchange) {
 		
		Response response = new Response(ResponseCode.CONTENT);
		String payload = "Total Devices Registred = " + SmartDevice.count +"\n";
		 
		for( SmartDevice sd : devices){
			payload = payload + sd.toString() + "\n";
		}

 		response.setPayload(payload);
		exchange.respond(response);
	 }
	 
	public void handlePUT(CoapExchange exchange) {
		
		String type = exchange.getQueryParameter("type");
		String ip = exchange.getQueryParameter("ip");
		boolean success = false;

		System.out.println(type + " " + ip);

		if(type != null && ip != null){


			if(type.equals("bulb")){
				devices.add(new Bulb(ip));
				success = true;
			}

		}
				
		if(success){
			Response response = new Response(ResponseCode.CHANGED);	
			exchange.respond(response);
		}else{
			Response response = new Response(ResponseCode.BAD_REQUEST);	
			exchange.respond(response);
		}

 	}
}

