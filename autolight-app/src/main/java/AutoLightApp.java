import java.io.IOException;

import register.Register;

public class AutoLightApp {

	public static void main(String[] args) {

		Register reg = new Register("fd00::202:2:2:2");

		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
