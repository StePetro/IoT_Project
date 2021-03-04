import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

import register.Register;
import smartDevices.Bulb;
import smartDevices.LuminositySensor;
import smartDevices.PresenceSensor;
import uilities.*;

public class AutoLightApp {

	public static void main(String[] args) {

		OutputWindow log = new OutputWindow("AutoLight App LOG");
		SwingUtilities.invokeLater(log);

		Register.start(AppOptions.registerIP);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String command = "";

		System.out.println("Welcome to AutoLight App!\nDigit '!ls' for command list...");

		while (true) {

			try {
				command = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (command.equals("!exit") || command.equals("!quit")) {
				System.out.println("See you soon, byeee");
				System.exit(1);
			}

			if (command.equals("!ls")) {
				System.out.println("Command list:");
				System.out.println("- !exit|!quit: to exit");
				System.out.println("- !reg: lists the registered devices and their IPs");
				System.out.println("- !mode [manual|auto]: to pass to manual/automatic mode");
				System.out.println("- !des_lum [value]: to set desired luminosity value");
				System.out.println("- !sw [Bulb IP] [ON|OFF]: to switch bulb on or off");
				System.out.println("- !sw ALL [ON|OFF]: to switch all bulbs on or off");
				System.out.println("- !lum [Bulb IP] [+|-] [value]: increase/decrease bulb luminosity value");
				System.out.println("- !lum [Bulb IP] [value]: set bulb luminosity value");
				System.out.println("- !lum [ALL] [value]: set all bulbs luminosity value");
				continue;
			}

			if (command.equals("!reg")) {
				Register.refreshRegister();
				System.out.println("Registred BULBS:");
				for (String ip : Bulb.getIPs()) {
					System.out.println("- " + ip);
				}
				System.out.println("Registred PRESENCE SENSORS:");
				for (String ip : PresenceSensor.getIPs()) {
					System.out.println("- " + ip);
				}
				System.out.println("Registred LUMINOSITY SENSORS:");
				for (String ip : LuminositySensor.getIPs()) {
					System.out.println("- " + ip);
				}
				continue;
			}

			String[] complexCommand = command.split(" ");

			if (complexCommand[0].equals("!mode")) {
				if (complexCommand[1].equals("auto")) {
					AppOptions.manualMode = false;
					continue;
				}
				if (complexCommand[1].equals("manual")) {
					AppOptions.manualMode = true;
					continue;
				}
				System.out.println("Invalid mode...");
				continue;
			}

			if (complexCommand[0].equals("!des_lum")) {
				try {
					int lum = Integer.parseInt(complexCommand[1]);
					if (lum >= 0 && lum <= 100) {
						AppOptions.desiredLum = lum;
						continue;
					}
				} catch (Exception ex) {
				}
				System.out.println("Invalid luminosity value...");
				continue;
			}

			if (complexCommand[0].equals("!sw") && complexCommand[1].equals("ALL")) {
				if (complexCommand[2].equals("ON") || complexCommand[2].equals("OFF")) {
					Bulb.setAllSwitches(complexCommand[2]);
					continue;
				}
				System.out.println("Status non valid...");
				continue;
			}

			if (complexCommand[0].equals("!sw")) {
				if ((complexCommand[2].equals("ON") || complexCommand[2].equals("OFF"))
						&& Bulb.getIPs().contains(complexCommand[1])) {
					for (Bulb b : Register.getRegistredBulbs()) {
						if (b.getIP().equals(complexCommand[1])) {
							b.getSwitchResource().set(complexCommand[2]);
							break;
						}
					}
					continue;
				}
				System.out.println("Bulb not found or command not valid...");
				continue;
			}

			if (complexCommand[0].equals("!lum") && complexCommand[1].equals("ALL")) {
				try {
					int lum = Integer.parseInt(complexCommand[2]);
					if (lum > 0 && lum <= 100) {
						Bulb.SetAllLuminosities(lum);
						continue;
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}
				System.out.println("Invalid luminosity value...");
				continue;
			}

			if (complexCommand[0].equals("!lum") && (complexCommand[2].equals("+") || complexCommand[2].equals("-"))) {
				try {
					int lum = Integer.parseInt(complexCommand[3]);
					if (Bulb.getIPs().contains(complexCommand[1])) {
						for (Bulb b : Register.getRegistredBulbs()) {
							if (b.getIP().equals(complexCommand[1])) {
								if (complexCommand[2].equals("+")) {
									if (!((b.getLuminosityResource().getValue() + lum) > 100)) {
										b.getLuminosityResource().increase(lum);
									} else {
										b.getLuminosityResource().set(100);
										System.out.println("Over 100, set to 100...");
									}
									break;
								}
								if (complexCommand[2].equals("-")) {
									if (!(b.getLuminosityResource().getValue() - lum < 0)) {
										b.getLuminosityResource().decrease(lum);
									} else {
										b.getLuminosityResource().set(0);
										System.out.println("Below 0, set to 0...");
									}
									break;
								}
							}
						}
						continue;
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}
				System.out.println("Invalid luminosity value or bulb not found...");
				continue;
			}

			if (complexCommand[0].equals("!lum")) {
				try {
					int lum = Integer.parseInt(complexCommand[2]);
					if (Bulb.getIPs().contains(complexCommand[1]) && lum >= 0 && lum <= 100) {
						for (Bulb b : Register.getRegistredBulbs()) {
							if (b.getIP().equals(complexCommand[1])) {
								b.getLuminosityResource().set(lum);
								break;
							}
						}
						continue;
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}
				System.out.println("Invalid luminosity value or bulb not found...");
				continue;
			}

			System.out.println("Command not valid...");

		}

	}

}
