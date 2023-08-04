import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {

    //Constants
    public static final int ARITHMETIC = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF = 5;
    public static final int FUNCTION = 6;
    public static final int RETURN = 7;
    public static final int CALL = 8;

    public static final ArrayList<String> arithmeticCommands = new ArrayList<>(List.of("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"));

    //Instance variables
    private String arg1;
    private int argType, arg2;
    private Scanner commands;

    // DESCRIPTION: Creates a Parser object and cleans all lines of the file.
    // PRE-CONDITION: inputFile is a valid file stream that has been initialized.
    // POST-CONDITION: Each line of the .vm file is cleaned and stores the result in a string.
    public Parser(File inputFile) {
        argType = -1;
        arg1 = "";
        arg2 = -1;

        try {
            commands = new Scanner(inputFile);
            StringBuilder preprocessed = new StringBuilder();
            String line;

            while(commands.hasNext()) {
                line = removeComments(commands.nextLine()).trim();
                if(line.length() > 0)
                    preprocessed.append(line).append("\n");
            }
            commands = new Scanner(preprocessed.toString().trim());
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // DESCRIPTION: Checks if there are more commands to read.
    // PRE-CONDITION: inputFile stream has been created.
    // POST-CONDITION: Returns true if there are more lines on the inputFile, if not -> false
    public boolean hasMoreCommands() {
        return commands.hasNextLine();
    }

    // DESCRIPTION: Reads the next command from the inputFile, updates currentCmd.
    // PRE-CONDITION: hasMoreCommands() returned true when called.
    // POST-CONDITION: currentCmd has been updated to the line that was just read.
    public void advance() {
        String currentCmd = commands.nextLine();
        arg1 = "";
        arg2 = -1;

        String[] stringSegment = currentCmd.split(" ");

        if(stringSegment.length > 3)
            throw new IllegalArgumentException("Error: Too many arguments.");
        if(arithmeticCommands.contains(stringSegment[0])) {
            argType = ARITHMETIC;
            arg1 = stringSegment[0];
        }
        else if(stringSegment[0].equals("return")) {
            argType = RETURN;
            arg1 = stringSegment[0];
        }
        else {
            arg1 = stringSegment[1];

            switch (stringSegment[0]) {
                case "push" -> argType = PUSH;
                case "pop" -> argType = POP;
                case "label" -> argType = LABEL;
                case "if" -> argType = IF;
                case "goto" -> argType = GOTO;
                case "function" -> argType = FUNCTION;
                case "call" -> argType = CALL;
                default -> throw new IllegalArgumentException("Invalid Command Type.");
            }

            if(argType == PUSH || argType == POP || argType == FUNCTION || argType == CALL) {
                try {
                    arg2 = Integer.parseInt(stringSegment[2]);
                }catch(Exception e) {
                    throw new IllegalArgumentException("Error: arg2 is not of type int");
                }
            }
        }
    }

    // DESCRIPTION: Returns the command type for the current line in the parser.
    // PRE-CONDITION: VM commands have been read from a valid file stream.
    // POST-CONDITION: Returns the current command type else throws IllegalStateException
    public int getCommandType() {
        if(argType != -1)
            return argType;
        else
            throw new IllegalStateException("Error: No command");
    }

    // DESCRIPTION: Returns the first argument for the current command.
    // PRE-CONDITION: VM commands have been read in from a valid file stream.
    // POST-CONDITION: Returns arg1 if it is an Arithmetic command, else throws IllegalStateException.
    public String getArgument1() {
        if(getCommandType() != RETURN)
            return arg1;
        else
            throw new IllegalStateException("Error: Can't retrieve arg1 from 'RETURN' command type");
    }

    // DESCRIPTION: Returns the second argument for the current command.
    // PRE-CONDITION: VM commands have been read in from a valid file stream.
    // POST-CONDITION: Returns arg2 if it is a PUSH, POP, FUNCTION, or CALL command, else throws IllegalStateException.
    public int getArgument2() {
        if(argType == PUSH || argType == POP || argType == FUNCTION || argType == CALL)
            return arg2;
        else
            throw new IllegalStateException("Error: Can't retrieve arg2");
    }

    // DESCRIPTION: Gets the extension of the given file.
    // PRE-CONDITION: fileName is a valid file stream
    // POST-CONDITION: Returns the file's extension, else returns an empty string.
    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');

        if(index != -1)
            return fileName.substring(index);
        else {
            System.err.println("Error: File extension couldn't be found.");
            return "";
        }
    }

    // DESCRIPTION: Removes all comments from a line.
    // PRE-CONDITION: None. This is done automatically.
    // POST-CONDITION: The substring that comes after "//" has been removed.
    private static String removeComments(String line) {
        int index=  line.indexOf("//");

        if(index != -1)
            line = line.substring(0, index);
        return line;
    }

}
