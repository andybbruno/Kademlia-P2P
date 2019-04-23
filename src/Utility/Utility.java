package Utility;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import Start.Start;

public class Utility {
	public static String generateIP() {
		Random rand = new Random();
		return (rand.nextInt(255) + 1) + "." + rand.nextInt(255) + "." + rand.nextInt(255) + "." + rand.nextInt(255);
	}

	public static String generatePort() {
		Random rand = new Random();
		return "" + rand.nextInt(65535);
	}

	public static String generateID() {
		return getRandomBigInteger().toString();
	}

	private static BigInteger getRandomBigInteger() {
		Random rand = new Random();
		BigInteger upperLimit = BigInteger.valueOf(2).pow(Start.bit);
		BigInteger result;
		do {
			result = new BigInteger(upperLimit.bitLength(), rand);
		} while (result.compareTo(upperLimit) >= 0);
		return result;
	}

	public static String generateHASH(String IP_Port) {
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
}
