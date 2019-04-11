import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Andrea Bruno
 *
 */
public class Node {
	private int id;
	private String IP;
	private int port;
	int[][] DHT = new int[Main.bit][Main.kbuckets];

	Node(int id) throws NoSuchAlgorithmException {
		this.IP = generateIP();
		this.port = generatePort();
		// this.id = sha1(IP + ":" + port);
		this.id = id;
	}

	private static String generateIP() {
		Random rand = new Random();
		return (rand.nextInt(255) + 1) + "." + rand.nextInt(255) + "." + rand.nextInt(255) + "." + rand.nextInt(255);
	}

	private static Integer generatePort() {
		Random rand = new Random();
		return rand.nextInt(65535);
	}

	private static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	public int getID() {
		return this.id;
	}

	public String toString() {
		String bin_ID = String.format("%" + Main.bit + "s", Integer.toBinaryString(this.id)).replace(' ', '0');
		return "<" + this.id + "," + bin_ID + "," + this.IP + ":" + this.port + ">";

	}

	public boolean equals(Node node) {
		return this.id == node.getID();
	}
}
