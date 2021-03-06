/**
 * This class represents a handle for the memory manager.
 * It stores two values for retrieving a sequence from the
 * database: the offset and the length.
 */
public class Handle {
	
	/**
	 * The byte offset of a given sequence in memory.
	 */
	int offset;
	
	/**
	 * The length of a sequence in memory.  Stored in
	 * terms of letters, where 1 byte = 4 letters.
	 */
	int length;
	
	/**
	 * Basic constructor for the Handle class.
	 * 
	 * @param off - the integer offset for starting position
	 * @param len - the integer length for sequence
	 */
	public Handle(int off, int len) {
		offset = off;
		length = len;
	}
	
	/**
	 * Method to retrieve the offset.
	 * 
	 * @return - offset for starting position
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Method to retrieve the length.
	 * 
	 * @return - length for sequence
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Method to automate calculation of
	 * the number of bytes.
	 * 
	 * @return - the number of bytes needed
	 */
	public int getBytes() {
		return (length + 3) / 4;
	}
	
	/**
	 * Method to create a string representation of
	 * the Handle.  Puts it in [offset, length] format.
	 * 
	 * @return - a string for the Handle
	 */
	public String toString() {
		return "[" + offset + ", " + length + "]";
	}
	
	/**
	 * Method to compare two handle objects to
	 * determine if they are equal or not.
	 * 
	 * @param other - the other Handle for comparison
	 * @return - true if equal, false otherwise
	 */
	public boolean equals(Handle other) {
		return offset == other.getOffset() && length == other.getLength();
	}
}
