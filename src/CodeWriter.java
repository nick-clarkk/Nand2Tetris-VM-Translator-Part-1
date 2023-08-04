import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private int arithmeticJumpFlag;
    private PrintWriter outputStream;

    // DESCRIPTION: Creates an output stream for file output.
    // PRE-CONDITION: N/A. This is done independently.
    // POST-CONDITION: Output stream to file is created
    public CodeWriter(File fileOut) {
        try {
            outputStream = new PrintWriter(fileOut);
            arithmeticJumpFlag = 0;
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // DESCRIPTION: Format for add, sub, and or.
    // PRE-CONDITION: An arithmetic command is called for or, add, or sub
    // POST-CONDITION: Returns a format for add, sub, and, or.
    private String arithmetic1() {
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n";
    }

    // DESCRIPTION: General format for greater than, less than, and equals.
    // PRE-CONDITION: Given parameter is of type JLE, JGT, or JEQ.
    // POST-CONDITION: Returns a general format for JLE, JGT, JEQ.
    private String arithmetic2(String type)
    {
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "D=M-D\n" + "@FALSE" + arithmeticJumpFlag + "\n" + "D;" + type + "\n" +
                "@SP\n" + "A=M-1\n" + "M=-1\n" + "@CONTINUE" + arithmeticJumpFlag + "\n" + "0;JMP\n" + "(FALSE" + arithmeticJumpFlag +
                ")\n" + "@SP\n" + "A=M-1\n" + "M=0\n" + "(CONTINUE" + arithmeticJumpFlag + ")\n";
    }

    // DESCRIPTION: Translates the given arithmetic command to assembly code.
    // PRE-CONDITION: cmd is a valid arithmetic command.
    // POST-CONDITION: Command is translated into assembly.
    public void writeArithmetic(String cmd) {
        switch (cmd) {
            case "add" -> outputStream.print(arithmetic1() + "M=M+D\n");

            case "sub" -> outputStream.print(arithmetic1() + "M=M-D\n");

            case "and" -> outputStream.print(arithmetic1() + "M=M&D\n");

            case "or" -> outputStream.print(arithmetic1() + "M=M|D\n");

            case "gt" -> {
                outputStream.print(arithmetic2("JLE")); //not less than or equal to
                arithmeticJumpFlag++;
            }

            case "lt" -> {
                outputStream.print(arithmetic2("JGE")); //not greater than or equal to
                arithmeticJumpFlag++;
            }

            case "eq" -> {
                outputStream.print(arithmetic2("JNE")); //not greater/less than
                arithmeticJumpFlag++;
            }

            case "not" -> outputStream.print("@SP\nA=M-1\nM=!M\n");

            case "neg" -> outputStream.print("D=0\n@SP\nA=M-1\nM=D-M\n");

            default ->
                    throw new IllegalArgumentException("Error: Can't call writeArithmetic() for a non-arithmetic command");
        }
    }

    // DESCRIPTION: General format for pushing local, that, this, push, temp, static, pointer, argument.
    // PRE-CONDITION: A call for a push for local, that, this, push, static, pointer, argument.
    // POST-CONDITION: If it is a pointer, read the data stored in THIS or THAT. If static, read the data stored in that address.
    private String pushFormat(String segment, int index, boolean isDirect) {
        String noPointerCode = (isDirect)?   "" : "@" + index + "\n" + "A=D+A\nD=M\n";

        return "@" + segment + "\n" + "D=M\n"+ noPointerCode + "@SP\n" + "A=M\n" +
                "M=D\n" + "@SP\n" + "M=M+1\n";
    }

    // DESCRIPTION: General format for popping local, that, this, push, temp, static, pointer, argument.
    // PRE-CONDITION: A call for a pop for local, that, this, push, static, pointer, argument.
    // POST-CONDITION: If it is a pointer, store the address of THIS or THAT. If it is static, store the index address
    private String popFormat(String fragment, int index, boolean isDirect) {
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

        return "@" + fragment + "\n" + noPointerCode + "@R13\n" + "M=D\n" + "@SP\n" +
                "AM=M-1\n" + "D=M\n" + "@R13\n" + "A=M\n" + "M=D\n";
    }

    // DESCRIPTION: Translates either PUSH or POP commands to assembly code.
    // PRE-CONDITION: Given parameters are for a PUSH/POP command.
    // POST-CONDITION: Assembly translation has been written to the outputFile if given command is valid, else throws an IllegalArgumentException.
    public void writePushPop(int command, String segment, int index)
    {
        if (command == Parser.PUSH)
        {
            //Can't do a switch statement due to multiple conditions for pointer; switch statement can only handle one boolean condition at a time.
            if (segment.equals("constant"))
                outputStream.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            else if (segment.equals("local"))
                outputStream.print(pushFormat("LCL",index,false));

            else if (segment.equals("argument"))
                outputStream.print(pushFormat("ARG",index,false));

            else if (segment.equals("this"))
                outputStream.print(pushFormat("THIS",index,false));

            else if (segment.equals("that"))
                outputStream.print(pushFormat("THAT",index,false));

            else if (segment.equals("temp"))
                outputStream.print(pushFormat("R5", index + 5,false));

            else if (segment.equals("pointer") && index == 0)
                outputStream.print(pushFormat("THIS",index,true));

            else if (segment.equals("pointer") && index == 1)
                outputStream.print(pushFormat("THAT",index,true));

            else if (segment.equals("static"))
                outputStream.print(pushFormat(String.valueOf(16 + index),index,true));
        }

        else if(command == Parser.POP)
        {
            //Can't do a switch statement due to multiple conditions for pointer; switch statement can only handle one boolean condition at a time.
            if (segment.equals("local"))
                outputStream.print(popFormat("LCL",index,false));

            else if (segment.equals("argument"))
                outputStream.print(popFormat("ARG",index,false));

            else if (segment.equals("this"))
                outputStream.print(popFormat("THIS",index,false));

            else if (segment.equals("that"))
                outputStream.print(popFormat("THAT",index,false));

            else if (segment.equals("temp"))
                outputStream.print(popFormat("R5", index + 5,false));

            else if (segment.equals("pointer") && index == 0)
                outputStream.print(popFormat("THIS",index,true));

            else if (segment.equals("pointer") && index == 1)
                outputStream.print(popFormat("THAT",index,true));

            else if (segment.equals("static"))
                outputStream.print(popFormat(String.valueOf(16 + index),index,true));
        }

        else
            throw new IllegalArgumentException("Error: Can't call writePushPop() for a non-push/pop command.");
    }

    // DESCRIPTION:	Closes outputFile stream.
    // PRE-CONDITION: File stream is open.
    // POST-CONDITION: File stream is closed.
    public void close() {
        outputStream.close();
    }
}
