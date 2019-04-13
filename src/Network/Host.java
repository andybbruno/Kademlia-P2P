package Network;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Host {
	private String ID;
	private String IP_Address;
	private String port;

	Host() {
		this.IP_Address = generateIP();
		this.port = generatePort();
//		this.ID = "" + (new Random().nextInt(64));
		this.ID = generateID(this.IP_Address + ":" + this.port);
	}

	private String generateIP() {
		Random rand = new Random();
		return (rand.nextInt(255) + 1) + "." + rand.nextInt(255) + "." + rand.nextInt(255) + "." + rand.nextInt(255);
	}

	private String generatePort() {
		Random rand = new Random();
		return "" + rand.nextInt(65535);
	}

	private String generateID(String IP_Port) {
		try {
			// Static getInstance method is called with hashing SHA
			MessageDigest md = MessageDigest.getInstance("SHA-1");

			// digest() method called
			// to calculate message digest of an input
			// and return array of byte
			byte[] messageDigest = md.digest(IP_Port.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}

			return hashtext;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

	}

	public String toString() {
		return this.ID + "," + this.IP_Address + ":" + this.port;
	}

	public String getID() {
		return new String(this.ID);
	}

}
