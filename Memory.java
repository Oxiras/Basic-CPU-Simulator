import java.io.*;
import java.util.Scanner;

/*
 * PROGRAM: Memory.java
 * @author: Moustapha Dieng
 * This program simulates main memory. It consists of 2000
 * integer entries and initializes by loading date from
 * a text file.
 **/
public class Memory {

	private int mem[] = new int[2000];	//Define main memory.

	/*
	 * Returns the value at the address.
	 * @param address: the address of the value to lookup.
	 */
	private int read(int address) {
		return mem[address];
	}

	/*
	 * Writes the data to the address.
	 * @param address: the address to write to.
	 * @param data: the value to write.
	 */
	private void write(int address, int data) {
		mem[address] = data;
	}
	
	/*
	 *  Main method
	 *  @param args: as string array.
	 */
	public static void main(String[] args) {
		// Create instance of Memory class.
		Memory memory =  new Memory();

		// Initialize Memory by loading values from a file.
		try {
			String filename = null;
			if(args.length > 0)
				filename = args[0];
			File file = new File(filename);
			int i = 0;
			Scanner inFile = new Scanner(file);
			while(inFile.hasNext()) {
				String value = inFile.next();
				char firstChar = value.charAt(0);
				switch(firstChar) {
					case '.':
						i = Integer.parseInt(value.substring(1));
						break;
					case '/':
						inFile.nextLine();
						break;
					default:
						memory.write(i, Integer.parseInt(value));
						inFile.nextLine();
						i++;
				}
			}
			inFile.close();
		}
		catch(FileNotFoundException | NullPointerException | NumberFormatException e) {
			System.out.println("File: " + e.getMessage() + "\nExiting...");
			System.exit(0);	
		}

		// Handles read and write cpu requests.
		Scanner cpuListener = new Scanner(System.in);
		while(cpuListener.hasNext()) { 
			String[] input = cpuListener.nextLine().split(",");
			if(Integer.parseInt(input[0]) == 0) 
				System.out.println(memory.read(Integer.parseInt(input[1])));
			else if(Integer.parseInt(input[0]) == 1)
				memory.write(Integer.parseInt(input[1]),Integer.parseInt(input[2]));
		}
		cpuListener.close();
	}
}
