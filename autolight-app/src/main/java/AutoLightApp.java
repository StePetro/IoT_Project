import register.Register;
import smartDevices.Bulb;

public class AutoLightApp {

	public static void main(String[] args) {
		
		Register reg = new Register("fd00::202:2:2:2");
		Bulb b = (Bulb) reg.getRegistredDevices().get(0);
		b.getSwitchResource().toggle();

	}

}
