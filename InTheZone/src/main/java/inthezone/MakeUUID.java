package inthezone;

import java.util.UUID;

/**
 * A command line utility to make JAVA compatible UUIDs.
 * */
public class MakeUUID {
	public static void main(String args[]) {
		System.out.println(UUID.randomUUID().toString());
	}
}

