package pmedit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BatchMan {
	
	public static boolean maybeHasBatch(String moto,String boto){
		if( moto != null && boto != null){
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(boto.getBytes());
				String toto = Base64.encodeBytes(md.digest());
				return toto.equals(moto);
			} catch (NoSuchAlgorithmException e) {
			}
		}
		return false;		
	}
	
	public static boolean hasBatch(){
		String moto = Main.getPreferences().get("key", null);
		String boto = Main.getPreferences().get("email", null);
		return maybeHasBatch(moto, boto);
	}

}
