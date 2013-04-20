// TODO Javadoc
public class HashTable {
	
	// TODO Private member fields w/ Javadoc
	
	// TODO Javadoc
	public HashTable(String fileName, int size) {
		// TODO Implement
	}
	
	// TODO Javadoc
	public boolean insert(String sequenceID, Handle IDHandle, Handle entryHandle) {
		// TODO Implement
		return false;
	}
	
	// TODO Javadoc
	public Handle getIDHandle(String sequenceID, int offset) {
		// TODO Implement
		return null;
	}
	
	// TODO Javadoc
	public Handle getEntryHandle(String sequenceID, int offset) {
		// TODO Implement
		return null;
	}
	
	// TODO Javadoc
	public void remove(String sequenceID, int offset) {
		// TODO Implement
	}
	
	/**
	 * Method to produce a string representation of all
	 * hash table entries.  Each entry will have the slot
	 * in the hash table as well as the handle for the
	 * sequence ID.
	 * 
	 * @return - all elements stored in hash table
	 */
	public String toString() {
		// TODO Implement
		return "NYI";
	}
	
	/**
	 * This is the given sfold algorithm for determining
	 * hash table indices.  This comes straight from the
	 * assignment page on Scholar.
	 * 
	 * @param s - the given sequence of ACGT letters
	 * @param M - the size of the hash table
	 * @return - the index for the hash table
	 */
	public int sfold(String s, int M) {
		int intLength = s.length() / 4;
		int sum = 0;
		for (int j = 0; j < intLength; j++) {
			char c[] = s.substring(j * 4, (j * 4) + 4).toCharArray();
			long mult = 1;
			for (int k = 0; k < c.length; k++) {
				sum += c[k] * mult;
				mult *= 256;
			}
		}
		
		char c[] = s.substring(intLength * 4).toCharArray();
		long mult = 1;
		for (int k = 0; k < c.length; k++) {
		  sum += c[k] * mult;
		  mult *= 256;
		}
		
		sum = (sum * sum) >> 8;
		return (Math.abs(sum) % M);
	}
}
