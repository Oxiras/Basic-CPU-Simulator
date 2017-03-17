import java.io.*;
import java.util.Scanner;
import java.util.Random;

/*
 * PROGRAM: Processor.java
 * @author: Moustapha Dieng
 * This program simulates a basic processor. It communicates with
 * a memory program through IPC. Mimics a very basic idea of a
 * fetch-decode-execute cycle with software interrupts.
 */
public class Processor {

	// Field initialization 
	private int SP = 1000, IR = 0, AC = 0, X = 0, Y = 0;
	private static int PC = 0, userStack = 1000, systemStack = 2000, numOfInstructions = 0;
	private static boolean userMode = true, intFlag = false;

	/*
	 * This termStatus method returns the
	 * termination code of the child process.
	 * @param: exit value of the process
	 * @return: Processor termination code
	 */
	public String termStatus(int code) {
		String status = "Process termination code: " + code;
		return status;
	}
	
	/* This decode method decodes the instruction
	 * fetched from memory and executes it.
	 * @param: OutputStream, Scanner, PrintWriter, value (instruction).
	 */
	private void decode(OutputStream out, Scanner memReader, PrintWriter pw, int value) {
		++numOfInstructions;
		++PC;
		IR = value;

		// Handles instructions without operands.
		if (IR == 0 || IR == 6 ||IR == 8 || IR == 50 || IR >= 10 && IR <= 19 || IR >= 25 && IR <= 30) {
			switch (IR) {
				case 0:
					numOfInstructions--;
					break;
				case 6:
					AC = readFromMemory(out, memReader, pw, SP+X);
					break;
				case 8:
					Random rand = new Random();
					AC = rand.nextInt(100) + 1;
					break;
				case 10:
					AC += X;
					break;
				case 11:
					AC += Y;
					break;
				case 12:			
					AC -= X;
					break;
				case 13:		
					AC -= Y;
					break;
				case 14:				
					X = AC;
					break;
				case 15:			
					AC = X;
					break;
				case 16:		
					Y = AC;
					break;
				case 17:	
					AC = Y;
					break;
				case 18:				
					SP = AC;
					break;
				case 19:
					AC = SP;
					break;
				case 25:		
					X++;
					break;
				case 26:	
					X--;
					break;
				case 27:
					stackPush(out, memReader, pw, AC);
					break;
				case 28:
					AC = stackPop(out, memReader, pw);
					break;
				case 29:
					intHandler(out, memReader, pw);
					PC = 1500;
					break;
				case 30:
					intFlag = false;
					PC = stackPop(out, memReader, pw);
					SP = stackPop(out, memReader, pw);
					userMode = true;
					break;
				case 50:
					System.exit(0);	
			}
		}
		// Handles instructions with operands.
		else {
			int operand = readFromMemory(out, memReader, pw, PC);
			PC++;
			switch(IR) {
				case 1:
					AC = operand;
					break;
				case 2:
					AC = readFromMemory(out, memReader, pw, operand);
					break;
				case 3:
					int temp = readFromMemory(out, memReader, pw, operand);
					AC = readFromMemory(out, memReader, pw, temp);
					break;
				case 4:
					AC = readFromMemory(out, memReader, pw, (operand+X));	
					break;
				case 5:
					AC = readFromMemory(out, memReader, pw, (operand+Y));
					break;
				case 7:
					writeToMemory(out, memReader, pw, operand, AC);
					break;
				case 9:
					if(operand == 1)
						System.out.print(AC);
					else
						System.out.print((char)AC);
					break;
				case 20:
					PC = operand;
					break;
				case 21:
					if(AC == 0) {
						PC = operand;
					}
					else numOfInstructions--;
					break;
				case 22:
					if(AC != 0) {
						PC = operand;
					}
					else numOfInstructions--;
					break;
				case 23:
					stackPush(out, memReader, pw, PC);
					PC = operand;
					break;
				case 24:
					PC = stackPop(out, memReader, pw);
					break;
			}
		}
	}
	/*
	 * This accessControl method restricts access to
	 * the system segment of memory by the user.
	 * @param: address in memory.
	 */
 	private void accessControl(int address) {
		if(userMode && address >= 1000) {
			System.out.println("Memory access violation!");
			System.exit(0);
		}
	}
	
	/*
	 * This intHandler method handles both
	 * system calls and timer interrupts.
	 * @param: OutputStream, Scanner, PrintWriter.
	 */
	private void intHandler(OutputStream out, Scanner memReader, PrintWriter pw) {
		if(intFlag == false) {
			userMode = false;
			intFlag = true;
			userStack = SP;
			SP = systemStack;
			stackPush(out, memReader, pw, userStack);
			stackPush(out, memReader, pw, PC);
		}

	}
	
	/*
	 * This timerInt method calls the intHandler method and
	 * sets the program counter to the appropriate value.
	 * @param: OutputStream, Scanner, PrintWriter.
	 */
	private void timerInt(OutputStream out, Scanner memReader, PrintWriter pw) {
		intHandler(out, memReader, pw);
		PC = 1000;
	}
	
	/*
	 * This readFromMemory method returns the
	 * value at the memory address provided.
	 * @param: OutputStream, Scanner, PrintWriter, address.
	 */
	private int readFromMemory(OutputStream out, Scanner memReader, PrintWriter pw, int address) {
		accessControl(address);
		int value = 0;
		pw.printf("0,%d\n", address);
		pw.flush();
		if(memReader.hasNext()) {
			value = memReader.nextInt();
		}
		return value;
	}
	
	/*
	 * This writeToMemory method writes the 
	 * given value to the address provided in memory.
	 * @param: OutputStream, Scanner, PrintWriter, address, value.
	 */
	private void writeToMemory(OutputStream out, Scanner memReader, PrintWriter pw, int address, int value) {
		pw.printf("1,%d,%d\n", address, value);
		pw.flush();
	}
	
	/*
	 * This stackPush method pushes the
	 * given value to the stack.
	 * @param: OutputStream, Scanner, PrintWriter, value.
	 */
	private void stackPush(OutputStream out, Scanner memReader, PrintWriter pw, int value) {
		SP--;
		writeToMemory(out, memReader, pw, SP, value);
	}

	/*
	 * This stackPop method pops the last-in
	 * value from the stack and deletes value from memory.
	 * @param: OutputStream, Scanner, PrintWriter.
	 */
	private int stackPop(OutputStream out, Scanner memReader, PrintWriter pw) {
		int temp = readFromMemory(out, memReader, pw, SP);
		writeToMemory(out, memReader, pw, SP, 0);
		SP++;
		return temp;
	}
	
	/*
	 * Main method
	 * @param: args as a string array.
	 */
	public static void main(String[] args) {

		Processor processor = new Processor();
	
		try {
			// Verify file name is entered and exists.
			if(args.length == 0) {
				System.out.println("File name is required!");
				System.exit(0);
			}
			File file = new File(args[0]);  
			if (!file.exists()) {
				System.out.println("File: " + args[0] + " does not exist.");
				System.exit(0);
			}

			// Create Memory process.
			Process proc = Runtime.getRuntime().exec(new String[] {"java", "Memory", args[0], args[1]});
			// Open Input and Ouput streams for Memory process.
			InputStream in = proc.getInputStream();
			OutputStream out = proc.getOutputStream();
			// Misc setup: Memory reader/PrintWriter/Random.
			Scanner memReader = new Scanner(in);
			PrintWriter pw = new PrintWriter(out);
			Random rand = new Random();
			
			int timerFlag = Integer.parseInt(args[1]);	// Initialize timerFlag
			// Fetch cycle
			while(true) {
				// Check for timer interrupt.
				if(intFlag == false && numOfInstructions > 0 && (numOfInstructions % timerFlag == 0)) {
					processor.timerInt(out, memReader, pw);
				}
				// Instruction decode.
				int val = processor.readFromMemory(out, memReader, pw, PC);
				if(val != -1) 
					processor.decode(out, memReader, pw, val);
				else break;
			}
			memReader.close();

			// Wait until Memory process terminates.
			proc.waitFor();

			// Return exit value of Memory process.
			int exitVal = proc.exitValue();
			processor.termStatus(exitVal);
		}	
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
