package server;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapServer;

import server.resources.RegisterResource;
import smartDevices.*;

public class Server extends CoapServer {

    private RegisterResource register;
    public static Server self;

    public Server(){

        register = new RegisterResource("register");
        this.add(register);
        self = this;

    }

    public ArrayList<SmartDevice> getDevices(){
        return register.getDevices();
    }
}
