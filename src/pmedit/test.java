package pmedit;

import java.util.List;
import java.util.Arrays;

public class test {

	public static void main(String[] args) {
		
		List<String> l = Arrays.asList("1", "2", "3");
		for(String i: l){
			System.out.println(i);
		}
		l = null;
		for(String i: l){
			System.out.println(i);
		}

	}

}
