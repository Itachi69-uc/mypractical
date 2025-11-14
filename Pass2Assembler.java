import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Pass2Assembler {

    static Map<String, Integer> mot = new HashMap<>();
    static {
        mot.put("STOP", 0);
        mot.put("ADD", 1);
        mot.put("SUB", 2);
        mot.put("MULT", 3);
        mot.put("MOVER", 4);
        mot.put("MOVEM", 5);
        mot.put("COMP", 6);
        mot.put("BC", 7);
        mot.put("DIV", 8);
        mot.put("READ", 9);
        mot.put("PRINT", 10);
    }

    static Map<String, Integer> regMap = new HashMap<>();
    static {
        regMap.put("BREG", 1);
        regMap.put("CREG", 2);
    }

    static Map<Integer, Integer> symbolAddr = new HashMap<>();
    static Map<Integer, Integer> literalAddr = new HashMap<>();

    public static void main(String[] args) throws IOException {

        Path icPath = Paths.get("intermediate.txt");
        Path symPath = Paths.get("symbol_table.txt");
        Path litPath = Paths.get("literal_table.txt");

        if (!Files.exists(icPath)) {
            System.out.println("intermediate.txt not found");
            return;
        }

        if (Files.exists(symPath)) loadSymbolTable(symPath);
        if (Files.exists(litPath)) loadLiteralTable(litPath);

        List<String> icLines = Files.readAllLines(icPath);
        List<String> objectLines = new ArrayList<>();

        Pattern tokenPattern = Pattern.compile("\\(([^)]+)\\)");

        for (String raw : icLines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(":");
            String lcStr = parts.length > 1 ? parts[0].trim() : "";

            Matcher m = tokenPattern.matcher(line);
            List<String> tokens = new ArrayList<>();
            while (m.find()) tokens.add(m.group(1).trim());
            if (tokens.isEmpty()) continue;

            String first = tokens.get(0);
            String[] fparts = first.split(",");
            String cls = fparts[0].trim();
            String codeStr = fparts.length > 1 ? fparts[1].trim() : "";

            if (cls.equalsIgnoreCase("AD")) continue;

            else if (cls.equalsIgnoreCase("DL")) {
                if (codeStr.equals("01")) {
                    int n = 1;
                    if (tokens.size() >= 2 && tokens.get(1).startsWith("C,"))
                        n = Integer.parseInt(tokens.get(1).substring(2));
                    for (int i = 0; i < n; i++)
                        objectLines.add(lcStr + "    00 00 000");
                }
                else if (codeStr.equals("02")) {
                    int val = 0;
                    for (String t : tokens)
                        if (t.startsWith("C,"))
                            val = Integer.parseInt(t.substring(2));
                    objectLines.add(lcStr + "    DATA " + val);
                }
                continue;
            }

            else if (cls.equalsIgnoreCase("IS")) {

                int opcode = Integer.parseInt(codeStr);
                int regField = 0;
                int memAddr = 0;

                for (int i = 1; i < tokens.size(); i++) {
                    String[] tk = tokens.get(i).split(",", 2);
                    String tcls = tk[0].trim();
                    String tval = tk.length > 1 ? tk[1].trim() : "";

                    if (tcls.equals("R")) {
                        try { regField = Integer.parseInt(tval); }
                        catch (Exception e) { regField = regMap.getOrDefault(tval.toUpperCase(), 0); }
                    }
                    else if (tcls.equals("S")) {
                        int idx = Integer.parseInt(tval);
                        memAddr = symbolAddr.getOrDefault(idx, 0);
                    }
                    else if (tcls.equals("L")) {
                        int idx = Integer.parseInt(tval);
                        memAddr = literalAddr.getOrDefault(idx, 0);
                    }
                    else if (tcls.equals("C")) {
                        memAddr = Integer.parseInt(tval);
                    }
                }

                String obj = String.format("%02d %01d %03d", opcode, regField, memAddr);
                objectLines.add(lcStr + "    " + obj);
            }
        }

        Files.write(Paths.get("object_code.txt"), objectLines);
        System.out.println("Object code generated.");
    }

    static void loadSymbolTable(Path symPath) throws IOException {
        List<String> lines = Files.readAllLines(symPath);
        Pattern row = Pattern.compile("^\\s*(\\d+)\\s+(\\S+)\\s+(\\S+).*");
        for (String l : lines) {
            Matcher m = row.matcher(l);
            if (m.find()) {
                int idx = Integer.parseInt(m.group(1));
                String addrStr = m.group(3);
                int addr = addrStr.equals("-") ? 0 : Integer.parseInt(addrStr);
                symbolAddr.put(idx, addr);
            }
        }
    }

    static void loadLiteralTable(Path litPath) throws IOException {
        List<String> lines = Files.readAllLines(litPath);
        Pattern row = Pattern.compile("^\\s*(\\d+)\\s+(\\S+)\\s+(\\S+).*");
        for (String l : lines) {
            Matcher m = row.matcher(l);
            if (m.find()) {
                int idx = Integer.parseInt(m.group(1));
                String addrStr = m.group(3);
                int addr = addrStr.equals("-") ? 0 : Integer.parseInt(addrStr);
                literalAddr.put(idx, addr);
            }
        }
    }
}
