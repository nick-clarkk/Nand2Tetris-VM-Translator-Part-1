import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class VMTranslator {
    public static void main(String[] args) {

        //Instance variables
        ArrayList<File> vmFiles = new ArrayList<>();

        String inputFileName, fileOutPath = "";
        File inputFile;
        File outputFile;
        CodeWriter writer;
        Scanner keyboard = new Scanner(System.in);

        //Get input file from the console
        System.out.print("\nEnter the assembly file with a .vm extension >> ");
        inputFileName = keyboard.nextLine();

        //Close the keyboard
        keyboard.close();

        inputFile = new File(inputFileName);

        //Check to see if it is a .vm file
        if(inputFile.isFile()) {
            String path = inputFile.getAbsolutePath();

            if(!Parser.getExtension(path).equals(".vm"))
                throw new IllegalArgumentException(".vm extension is required.");

            vmFiles.add(inputFile);
            //Define outputFile path
            fileOutPath = inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().lastIndexOf(".")) + ".asm";
        }

        outputFile = new File(fileOutPath);
        writer = new CodeWriter(outputFile);

        //Parse through the given .vm file
        for(File f : vmFiles) {
            Parser parser = new Parser(f);
            int type;

            while(parser.hasMoreCommands()) {
                parser.advance();
                type = parser.getCommandType();

                if(type == Parser.ARITHMETIC)
                    writer.writeArithmetic(parser.getArgument1());
                else if(type == Parser.POP || type == Parser.PUSH)
                    writer.writePushPop(type, parser.getArgument1(), parser.getArgument2());
            }
        }

        //Save the file
        writer.close();
        System.out.println("\nDone");
    }
}