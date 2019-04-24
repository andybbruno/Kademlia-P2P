package Utility;

import java.math.BigInteger;
import java.util.Random;
import Start.Start;

/**
 * The Utility class contains all the procedures useful to the creation of a
 * Peer, such as a random IP, a random Port and a random ID.
 * 
 * N.B: Although Kademlia uses 160-bit SHA-1 IDs, as the Professor suggested,
 * hash functions were not used.
 * 
 * @author Andrea Bruno
 *
 */
public class Utility {
	/**
	 * @return an IP address
	 */
	public static String generateIP() {
		Random rand = new Random();
		return (rand.nextInt(255) + 1) + "." + rand.nextInt(255) + "." + rand.nextInt(255) + "." + rand.nextInt(255);
	}

	/**
	 * @return a port
	 */
	public static String generatePort() {
		Random rand = new Random();
		return "" + rand.nextInt(65535);
	}

	/**
	 * @return an ID in the m-bit space
	 */
	public static String generateID() {
		return getRandomBigInteger().toString();
	}

	
	/**
	 * @return a random BigInteger in the m-bit space
	 */
	private static BigInteger getRandomBigInteger() {
		Random rand = new Random();
		BigInteger upperLimit = BigInteger.valueOf(2).pow(Start.bit);
		BigInteger result;
		do {
			result = new BigInteger(upperLimit.bitLength(), rand);
		} while (result.compareTo(upperLimit) >= 0);
		return result;
	}
}
