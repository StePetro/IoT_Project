import actuators.*;

public class AutoLightApp {

	public static void main(String[] args) {
		Bulb b = new Bulb("[fd00::202:2:2:2]");

		System.out.println(b.bswitch.get());
		b.bswitch.toggle();
		System.out.println(b.bswitch.get());
		
	}

}
