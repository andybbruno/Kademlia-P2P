import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author Andrea Bruno
 *
 */
public class Node {
	private String id;
	private String IP;
	private Integer port;
	private String[][] DHT;

	Node() throws NoSuchAlgorithmException {
		this.IP = generateIP();
		this.port = generatePort();
		this.id = sha1(IP + ":" + port);
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

	public String getID() {
		return new String(this.id);
	}

	public String toString() {
		return this.IP + ":" + this.port;

	}
	
	public boolean equals(Node node) {
		return this.id == node.getID();
	}
}
