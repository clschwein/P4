import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

/**
 * Database manager for keeping track of sequence memory
 * and free blocks.  Allows for several interface methods
 * using the Handle class to determine which bytes are
 * sequences.
 */
public class DatabaseManager {

	/**
	 * File pointer to our byte array on disk.  Used to
	 * store and access sequences based on give Handles,
	 * with given offsets and lengths.
	 */
	RandomAccessFile file;

	/**
	 * Linked List for keeping track of all free memory
	 * blocks.  Each block is represented by a Handle,
	 * with a given offset and length.
	 */
	LinkedList<Handle> free;

	/**
	 * Basic constructor for the DatabaseManager class.
	 * Will initialize all member fields appropriately.
	 */
	public DatabaseManager(String fileName) {
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
		
		free = new LinkedList<Handle>();
	}

	/**
	 * Method to insert a given sequence into the first
	 * free memory block.  If there are no free memory
	 * blocks of sufficient size, will create a new one
	 * and add it to the end of the file.
	 * 
	 * @param sequence - the sequence to insert
	 * @param length - the length of the given sequence
	 * @return - the Handle for the given sequence
	 */
	public Handle insert(String sequence, int length) {
		// Calculate number of bytes needed to store this sequence
		int bytesNeeded = (int) Math.ceil((double)(length / 4.0));
		
		// Check for any free blocks with sufficient size
		for (Handle freeBlock: free) {
			int offset = freeBlock.getOffset();
			if (freeBlock.getLength() >= bytesNeeded) {
				// Attempt to write to the free block
				try {
					file.seek(offset);
					file.write(buildByteArray(sequence, bytesNeeded));
				} catch (IOException e) {
					System.err.println("Problem writing to file. See stack trace for details.");
					e.printStackTrace();
					return null;
				}
				
				// Clean up list of free blocks
				if (freeBlock.getLength() == bytesNeeded) {
					free.remove(freeBlock);
				}
				else {
					free.set(free.indexOf(freeBlock), new Handle(offset + bytesNeeded, freeBlock.getLength() - bytesNeeded));
				}
				return new Handle(offset, bytesNeeded);
			}
		}
		
		// No valid free space so append to end of file
		int oldLength = -1;
		try {
			oldLength = (int) file.length();
			Handle fb = freeBlockAtEnd();
			if (fb != null) {
				// If free block at end, extend length to only amount we need and remove free block
				file.setLength(fb.getOffset() + bytesNeeded);
				file.seek(fb.getOffset());
				free.remove(fb);
			}
			else {
				file.setLength(oldLength + bytesNeeded);
				file.seek(oldLength);
			}
			file.write(buildByteArray(sequence, bytesNeeded));
		} catch (IOException e) {
			System.err.println("Problem writing to file. See stack trace for details.");
			e.printStackTrace();
		}
		
		return new Handle(oldLength, bytesNeeded);
	}

	/**
	 * Determines if there is a free block of memory at the end of the file.
	 * 
	 * @return - the Handle object of the free block at the end of the file,
	 *           or null if none
	 */
	private Handle freeBlockAtEnd() {
		for (Handle h: free) {
			try {
				if (file.length() - h.getLength() == h.getOffset()) {
					return h;
				}
			} catch (IOException e) {
				System.err.println("Could not find file length.");
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Builds a byte array of the given sequence represented in binary.
	 * 
	 * @param sequence - the String sequence to build from
	 * @param bytesNeeded - the number of bytes needed to represent the sequence
	 * @return - a byte array that should be written to the file
	 */
	private byte[] buildByteArray(String sequence, int bytesNeeded) {
		byte[] array = new byte[bytesNeeded];
		int currentByte = 0, count = 0;
		// Build bytes one character at a time
		for (int i = 0; i < sequence.length(); i++) {
			int mod = i % 4;
			switch (mod) {
			case 0:
				currentByte = (getCharValue(sequence.charAt(i))) << 6;
				break;
			case 1:
				currentByte |= (getCharValue(sequence.charAt(i))) << 4;
				break;
			case 2:
				currentByte |= (getCharValue(sequence.charAt(i))) << 2;
				break;
			case 3:
				currentByte |= getCharValue(sequence.charAt(i));
				array[count] = (byte) currentByte;
				count++;
				break;
			}
		}
		
		// Makes sure we set the last byte, in case 4 does not divide sequence.length()
		if (count == bytesNeeded - 1) {
			array[count] = (byte) currentByte;
		}
		
		return array;
	}

	/**
	 * Returns the binary representation for the given character.
	 * 
	 * @param c - the character to convert to binary
	 * @return - the binary value of the given character
	 */
	private int getCharValue(char c) {
		c = Character.toUpperCase(c);
		if (c == 'A')
			return 0b00;
		if (c == 'C')
			return 0b01;
		if (c == 'G')
			return 0b10;
		if (c == 'T')
			return 0b11;
		System.err.println(c + " is not a valid character for this sequence.");
		return -1;
	}

	/**
	 * Method to remove a sequence from the database.
	 * Creates a new free memory block in the place of
	 * the removed sequence.
	 * 
	 * @param handle - the given Handle for the sequence
	 */
	public void remove(Handle handle) {
		// Check to see where our handle should go
		// in the list to maintain order
		for (Handle h : free) {
			if (handle.getOffset() < h.getOffset()) {
				free.add(free.indexOf(h), handle);
				mergeFreeBlocks();
				return;
			}
		}
		
		// If we don't find a prior handle, then
		// just add this one to the end
		free.add(handle);
		mergeFreeBlocks();
	}

	/**
	 * Merges adjacent free memory blocks together.
	 */
	private void mergeFreeBlocks() {
		// For each free block, check it against the next block
		for (int i = 0; i < free.size() - 1; i++) {
			Handle current = free.get(i), next = free.get(i + 1);
			
			// If they are adjacent, merge them together
			if (current.getOffset() + current.getLength() == next.getOffset()) {
				int newLength = current.getLength() + next.getLength();
				free.remove(next);
				free.set(i, new Handle(current.getOffset(), newLength));
				i--;
			}
		}
	}

	/**
	 * Method to retrieve a DNA sequence using a given
	 * handle.  Will give the bytes in memory regardless
	 * of whether or not they have meaning (i.e. has no
	 * error checking).
	 * 
	 * @param handle - the given Handle for the sequence
	 * @return - the sequence in the memory location
	 */
	public String getEntry(Handle handle) {
		// Fetch the bytes from the file
		byte[] bytes = new byte[handle.getLength()];
		try {
			file.seek(handle.getOffset());
			file.read(bytes);
		} catch (IOException e) {
			System.err.println("Cannot read byte sequence for given handle.");
			e.printStackTrace();
		}
		
		// Convert the bytes to a string sequence
		String output = "";
		for (byte b: bytes) {
			output += getStrFromBin(b);
		}
		
		return output;
	}

	/**
	 * Returns the binary representation for the given character.
	 * 
	 * @param c - the character to convert to binary
	 * @return - the binary value of the given character
	 */
	private String getStrFromBin(byte b) {
		// Get out each 2-bit value
		int[] charsInByte = {(b & 0xC0) >> 6, (b & 0x30) >> 4, (b & 0x0C) >> 2, (b & 0x03)};
		
		// Convert bit-pairs to respective characters
		String output = "";
		for (int c: charsInByte) {
			if (c == 0)
				output += "A";
			else if (c == 1)
				output += "C";
			else if (c == 2)
				output += "G";
			else if (c == 3)
				output += "T";
		}
		
		return output;
	}

	/**
	 * Method to produce a string representation of all
	 * free memory blocks.
	 * 
	 * @return - all elements of the linked list free
	 */
	public String toString() {
		// Check if there are any free blocks
		if (free.size() <= 0) {
			return "Free Blocks:\nNone";
		}
		
		// Output for each free block
		String output = "Free Blocks:";
		int count = 1;
		for (Handle handle : free) {
			output += "\n[Block " + count + "]";
			output += " Starting byte location: " + handle.getOffset();
			output += ", Size: " + handle.getLength() + " byte(s)";
			count++;
		}
		
		return output;
	}
}