import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

// On my honor:
//
// - I have not used source code obtained from another student,
// or any other unauthorized source, either modified or
// unmodified.
//
// - All source code and documentation used in my program is
// either my original work, or was derived by me from the
// source code published in the textbook for this course.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

/**
 * Main P4 class for Project 4.
 * 
 * This class contains the main method for this project, which does
 * several things.  First, it deals with the command line parameter
 * and usage.  Second, it attempts to open and read lines from the
 * input file.  Third, it handles the commands by using the HashTable
 * class and database manager.  Fourth, it outputs appropriate errors
 * and prints.
 * 
 * This project was completed on May 2, 2013.  It was developed on
 * Windows 7, using the Eclipse IDE, and complied with the standard
 * Java Programming Language Compiler (javac), from the Java Development
 * Kit.
 * 
 * @author Chris Schweinhart (schwein)
 * @author Nate Kibler (nkibler7)
 */
public class P4 {

	/**
	 * Constant string patterns for command matching.  These are
	 * used for regular expression matching with the commands
	 * given by the input file.  Java does not use case-insensitive
	 * RegEx, you liars.
	 */
	private static final String INSERT_PATTERN = "^ *(insert|INSERT) *[ACGT]+ *[0-9]+ *$";
	private static final String REMOVE_PATTERN = "^ *(remove|REMOVE) *[ACGT]+ *$";
	private static final String PRINT_PATTERN = "^ *(print|PRINT) *$";
	private static final String SEARCH_PATTERN = "^ *(search|SEARCH) *[ACGT]+ *$";
	
	/**
	 * Member field for HashTable table.  This table represents the
	 * memory handles for both sequence IDs and sequences, which are
	 * stored in memory.  For more information, look in the HashTable.java
	 * file.
	 */
	private static HashTable table;
	
	/**
	 * Member field for DatabaseManager.  This memory manager will
	 * keep track of the bytes in memory for each DNA sequence ID
	 * and for each DNA sequence.  Also keeps track of the free memory
	 * blocks.  For more information, look in the DatabaseManager.java
	 * file.
	 */
	private static DatabaseManager dbm;
	
	/**
	 * Main method to control data flow.  This function takes
	 * the command line parameter as input and calls a method
	 * to read from the input file.
	 * 
	 * @param args - the command line arguments
	 */
	public static void main(String[] args) {
				
		// Check for proper usage
		if (args.length != 4) {
			System.out.println("Usage:");
			System.out.println("P4 <command-file> <hash-file> <hash-table-size> <memory-file>");
			System.exit(0);
		}
		
		// Check the hash table size
		if (Integer.parseInt(args[2]) % 32 != 0) {
			System.out.println("Parameter hash-table-size must be a multiple of 32.");
			System.exit(0);
		}
		
		dbm = new DatabaseManager(args[3]);
		table = new HashTable(args[1], Integer.parseInt(args[2]), dbm);
		
		runCommands(args[0]);
	}
	
	/**
	 * This method will run through the given command file and
	 * execute the commands we find there.  For each of the
	 * four commands, a separate method is used for clarity.
	 * 
	 * @param fileName - the file name for the command file
	 */
	private static void runCommands(String fileName) {
		try {
			// Attempt to open the input file into a buffered reader
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			
			// Keep reading in commands until we reach the EOF
			String line;
			while ((line = in.readLine()) != null) {
				if (line.matches(INSERT_PATTERN)) {
					// Parse out the sequence id and length from the command line
					int begin = Math.max(line.indexOf("r"), line.indexOf("R")) + 2;
					int end = Math.max(Math.max(Math.max(line.lastIndexOf('A'),
							line.lastIndexOf('C')), line.lastIndexOf('G')),
							line.lastIndexOf('T')) + 1;
					String sequenceID = line.substring(begin, end).trim();
					int length = Integer.parseInt(line.substring(end).trim());
					
					insert(sequenceID, length, in.readLine().trim());
				} else if (line.matches(REMOVE_PATTERN)) {
					// Parse out the sequence id from the command line
					int index = Math.max(line.indexOf("v"), line.indexOf("V")) + 2;
					String sequenceID = line.substring(index).trim();
					
					remove(sequenceID);
				} else if (line.matches(PRINT_PATTERN)) {
					print();
				} else if (line.matches(SEARCH_PATTERN)) {
					// Parse out the sequence id from the command line
					int index = Math.max(line.indexOf("h"), line.indexOf("H")) + 1;
					String sequenceID = line.substring(index).trim();
					
					search(sequenceID);
				} else {
					continue;
				}
			}
			in.close();
		}  catch (FileNotFoundException e) {
			System.out.println("The input file could not be found.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Error reading from file.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * This method is used for the insert command.  It takes
	 * a sequence ID, length, and entry, then attempts to
	 * add these to the memory manager and hash table.
	 * If the hash table cannot take the new entry for
	 * whatever reason, we remove the ID and entry from
	 * the database manager as well.
	 * 
	 * @param sequenceID - the sequence ID in ACGT letters
	 * @param length - the length of the new entry
	 * @param entry - the entry in ACGT letters
	 */
	private static void insert(String sequenceID, int length, String entry) {
		// Check the length
		if (length <= 0) {
			System.out.println("Length less than zero.");
			return;
		}
		
		// Get ID from table
		Handle[] handles = table.search(sequenceID);
		
		// Check if it is already in the table
		if (handles != null) {
			System.out.println("Sequence " + sequenceID + " already in table.");
			System.out.println();
			return;
		}
		
		// Add both to the dbm
		handles = new Handle[2];
		handles[0] = dbm.insert(sequenceID, sequenceID.length());
		handles[1] = dbm.insert(entry, length);
		
		// Add both to the table
		boolean result = table.insert(sequenceID, handles[0], handles[1]);

		// Check if the table could take them
		if(!result) {
			dbm.remove(handles[0]);
			dbm.remove(handles[1]);
			System.out.println("SequenceID " + sequenceID + " cannot be stored in hash table.");
		} else {
			System.out.println("SequenceID " + sequenceID + " inserted in hash table.");
		}
		System.out.println();
	}
	
	/**
	 * This method is used for the remove command.  It takes
	 * a sequence ID, then searches the hash table for that ID.
	 * If it finds the proper ID via linear probing, then it
	 * proceeds to remove the ID and the associated entry
	 * from the hash table and the database manager.
	 * 
	 * @param sequenceID - the sequence ID in ACGT letters
	 */
	private static void remove(String sequenceID) {
		// Get ID from table
		Handle[] handles = table.search(sequenceID);
		
		// Check if it wasn't in the table
		if (handles == null) {
			System.out.println("Sequence " + sequenceID + " not found.");
			System.out.println();
			return;
		}
		
		// Get entry from table
		String entry = dbm.getEntry(handles[1]);
		
		// Remove sequenceID and entry from table and dbm
		table.remove(sequenceID);
		dbm.remove(handles[0]);
		dbm.remove(handles[1]);
		System.out.println("Sequence Removed " + sequenceID + ":");
		System.out.println(entry);
		System.out.println();
	}
	
	/**
	 * This method is used for the print command.  It will
	 * simply print out the table entries, using a Scanner
	 * to pick out the proper Handles for use in querying
	 * the database manager.  It then prints out all free
	 * memory blocks.
	 */
	private static void print() {
		// Output the table
		System.out.println(table);
		
		// Output free blocks
		System.out.println(dbm);
		System.out.println();
	}
	
	/**
	 * This method is used for the search command.  It takes
	 * a sequence ID, then searches the hash table for that ID.
	 * If it finds the proper ID via linear probing, then it
	 * proceeds to print out the associated sequence ID and
	 * entry from the database manager.  Otherwise, an error
	 * is printed.
	 * 
	 * @param sequenceID - the sequence ID in ACGT letters
	 */
	private static void search(String sequenceID) {
		// Get ID from table
		Handle[] handles = table.search(sequenceID);
		
		// Check if it wasn't in the table
		if (handles == null) {
			System.out.println("SequenceID " + sequenceID + " not found.");
			System.out.println();
			return;
		}
		
		// Get entry from table
		String entry = dbm.getEntry(handles[1]);

		System.out.println("Sequence found: " + entry);
		System.out.println();
	}
}