import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class represents a hash table compatible with
 * disk operations.  Basically, the table has some
 * number of buckets, with a size given as a parameter
 * in the constructor as a multiple of 32.  This is
 * because each bucket is 512 bytes large, so it will
 * contain 32 entries.  The function sfold, given in
 * the spec on scholar, is used to determine the index
 * in the hash table.
 * 
 * @author Chris Schweinhart (schwein)
 * @author Nate Kibler (nkibler7)
 */
public class HashTable {
	
	/**
	 * File pointer to our byte array on disk.  Used to
	 * store and access sequences based on give Handles,
	 * with given offsets and lengths.
	 */
	private RandomAccessFile file;
	
	/**
	 * Integer variable designed to hold the size of the
	 * hash table.  It will be a multiple of 32, as each
	 * bucket must hold 32 values.
	 */
	private int size;
	
	/**
	 * This is the database manager for our project.
	 * We need to keep track of it in the hash table
	 * because sequence ID's used for searching are
	 * stored in the database instead of in the table
	 * itself.  Although this does not follow the best
	 * clean design, it works better than attempting
	 * to mediate between the dbm and the table in the
	 * main program.
	 */
	private DatabaseManager dbm;
	
	/**
	 * Basic constructor for the HashTable class.
	 * Will initialize all member fields appropriately.
	 * 
	 * @param fileName - the name of the file for our hash table
	 * @param sz - the size of our hash table, multiple of 32
	 * @param manager - the database manager for our entries
	 */
	public HashTable(String fileName, int sz, DatabaseManager manager) {
		try {
			file = new RandomAccessFile(fileName, "rw");
			// Make sure we are overwriting file.
			file.setLength(0);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find/create file.");
			System.exit(0);
		} catch (IOException e) {
			System.err.println("Could not overwrite file.");
			System.exit(0);
		}
		
		size = sz;
		dbm = manager;
	}
	
	/**
	 * Method to insert the given sequence ID into the
	 * first available slot.  Uses sfold to determine
	 * the hash table index, and linear probing to
	 * resolve collisions.  If the bucket is full, then
	 * the method will return false and fail to insert
	 * the sequence ID to the hash table.
	 * 
	 * @param sequenceID - the sequence ID to insert
	 * @param IDHandle - the associated handle for the ID
	 * @param entryHandle - the associated handle for the entry
	 * @return - true if successful, false otherwise
	 */
	public boolean insert(String sequenceID, Handle IDHandle, Handle entryHandle) {
		long idx = sfold(sequenceID, size);
		for (int i = 0; i < 32; i++) {
			if (getIDHandle(sequenceID, i) == null) {
				long writePos = idx + i;
				if (i >= 32 - (idx % 32)) {
					writePos -= 32;
				}
				try {
					file.seek(writePos * 16);
					file.writeInt(IDHandle.getOffset());
					file.writeInt(IDHandle.getLength());
					file.writeInt(entryHandle.getOffset());
					file.writeInt(entryHandle.getLength());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieves the associated ID handle for the sequence
	 * ID.  Uses the offset to determine the linear
	 * probing offset for sequential searching.
	 * 
	 * For example, using "ACGT" and 0 as parameters
	 * will return the handle associated with the sfold
	 * of "ACGT".  However, using "ACGT" and 2 will give
	 * the handle at index sfold("ACGT") + 2.
	 * 
	 * @param sequenceID - the sequence ID to fetch
	 * @param offset - the linear probing offset
	 * @return - the ID handle for our sequence ID
	 */
	private Handle[] getHandles(String sequenceID, int offset) {
		long sfold = sfold(sequenceID, size);
		long idx = sfold + offset;
		if (offset >= 32 - (sfold % 32)) {
			idx -= 32;
		}
		Handle[] handles = null;
		try {
			if (idx * 16 > file.length()) {
				return null;
			}
			file.seek(idx * 16);
			int idOff = file.readInt();
			int idLength = file.readInt();
			int entryOff = file.readInt();
			int entryLength = file.readInt();
			handles = new Handle[]{new Handle(idOff, idLength), 
					new Handle(entryOff, entryLength)};
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handles;
	}
	
	/**
	 * Removes both the ID and entry handles for the given
	 * sequence ID.  Uses the offset to determine the
	 * linear probing offset for sequential searching.
	 * 
	 * @param sequenceID - the sequence ID to remove
	 * @param offset - the linear probing offset
	 */
	public void remove(String sequenceID) {
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
	 * This method will take the given sequence ID and
	 * search the hash table for the entry that corresponds
	 * to our ID.  Uses sfold to find the initial index,
	 * then uses linear probing to continue the search.
	 * Returns null if nothing is found.
	 * 
	 * @param sequenceID - the sequence ID to search for
	 * @return - both the id and entry handles
	 */
	public Handle[] search(String sequenceID) {
		
		return null;
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
	long sfold(String s, int M) {
		int intLength = s.length() / 4;
		long sum = 0;
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
		return(Math.abs(sum) % M);
	}
}
