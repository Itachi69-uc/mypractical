import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Pass1Assembler {

    static class OpInfo {
        String cls;
        int code;
        int length;
        OpInfo(String c, int co, int l) { cls = c; code = co; length = l; }
    }

    static final Map<String, OpInfo> opTable = new HashMap<>();

    static class SymEntry {
        String name;
        Integer address;
        List<Integer> forwardRefs;

        SymEntry(String n) {
            name = n;
            forwardRefs = new ArrayList<>();
        }
    }

    static final Map<String, SymEntry> symTable = new LinkedHashMap<>();

    static class LitEntry {
        String literal;
        Integer address;
        LitEntry(String lit) { literal = lit; }
    }

    static final List<LitEntry> litTable = new ArrayList<>();
    static final List<Integer> poolTable = new ArrayList<>();

    static class ICLine {
        int lc;
        String repr;
        ICLine(int lc, String r) { this.lc = lc; repr = r; }
    }

    static final List<ICLine> intermediate = new ArrayList<>();
    static int LC = 0;

    public static void main(String[] args) throws IOException {
        initOpTable();
        List<String> lines = readInputLines();

        List<String> program = new ArrayList<>();
        for (String l : lines) {
            String t = l.trim();
            if (!t.isEmpty()) program.add(t);
        }

        poolTable.add(0);

        for (String rawLine : program) {
            String line = rawLine;
            int cpos = line.indexOf(';');
            if (cpos >= 0) line = line.substring(0, cpos).trim();
            if (line.isEmpty()) continue;

            List<String> tokens = tokenize(line);
            if (tokens.isEmpty()) continue;

            String label = null;
            String opcode;
            List<String> operands;

            String first = tokens.get(0);
            if (isOpOrDirective(first)) {
                opcode = first.toUpperCase();
                operands = tokens.size() > 1 ? tokens.subList(1, tokens.size()) : Collections.emptyList();
            } else {
                label = first;
                opcode = tokens.size() > 1 ? tokens.get(1).toUpperCase() : first.toUpperCase();
                operands = tokens.size() > 2 ? tokens.subList(2, tokens.size()) : Collections.emptyList();
            }

            if (label != null)
                defineSymbol(label, ("EQU".equalsIgnoreCase(opcode) ? null : LC));

            switch (opcode) {
                case "START": {
                    int startAddr = 0;
                    if (!operands.isEmpty()) {
                        try { startAddr = Integer.parseInt(stripQuotes(operands.get(0))); }
                        catch (NumberFormatException ignored) {}
                    }
                    LC = startAddr;
                    intermediate.add(new ICLine(LC, "(AD,01) (C," + LC + ")    ; START"));
                    break;
                }

                case "END":
                    assignLiteralsToPool();
                    intermediate.add(new ICLine(LC, "(AD,02)    ; END"));
                    break;

                case "LTORG":
                    assignLiteralsToPool();
                    intermediate.add(new ICLine(LC, "(AD,05)    ; LTORG"));
                    break;

                case "ORIGIN":
                    if (!operands.isEmpty()) {
                        int newLc = evaluateExpression(operands.get(0));
                        intermediate.add(new ICLine(LC, "(AD,03) (C," + newLc + ")    ; ORIGIN"));
                        LC = newLc;
                    }
                    break;

                case "EQU":
                    if (label != null && !operands.isEmpty()) {
                        int val = evaluateExpression(operands.get(0));
                        defineSymbol(label, val);
                        intermediate.add(new ICLine(LC, "(AD,04) (C," + val + ")    ; EQU " + label));
                    }
                    break;

                case "DS": {
                    int n = 1;
                    if (!operands.isEmpty()) {
                        try { n = Integer.parseInt(stripQuotes(operands.get(0))); }
                        catch (Exception ignored) {}
                    }
                    intermediate.add(new ICLine(LC, "(DL,01) (C," + n + ")    ; DS"));
                    LC += n;
                    break;
                }

                case "DC": {
                    String v = !operands.isEmpty() ? stripQuotes(operands.get(0)) : "0";
                    intermediate.add(new ICLine(LC, "(DL,02) (C," + v + ")    ; DC"));
                    LC += 1;
                    break;
                }

                default:
                    if (opTable.containsKey(opcode)) {
                        processImperative(opcode, operands);
                    } else {
                        intermediate.add(new ICLine(LC, "(??," + opcode + ") ; unknown"));
                        LC++;
                    }
                    break;
            }
        }

        assignLiteralsToPool();
        printIntermediate();
        printSymbolTable();
        printLiteralTable();
        printPoolTable();

        saveToFile("intermediate.txt", joinIntermediateLines());
        saveSymbolTable("symbol_table.txt");
        saveLiteralTable("literal_table.txt");
        savePoolTable("pool_table.txt");

        System.out.println("\nFiles saved!");
    }

    static void initOpTable() {
        opTable.put("STOP", new OpInfo("IS", 0, 1));
        opTable.put("ADD", new OpInfo("IS", 1, 1));
        opTable.put("SUB", new OpInfo("IS", 2, 1));
        opTable.put("MULT", new OpInfo("IS", 3, 1));
        opTable.put("MOVER", new OpInfo("IS", 4, 1));
        opTable.put("MOVEM", new OpInfo("IS", 5, 1));
        opTable.put("COMP", new OpInfo("IS", 6, 1));
        opTable.put("BC", new OpInfo("IS", 7, 1));
        opTable.put("DIV", new OpInfo("IS", 8, 1));
        opTable.put("READ", new OpInfo("IS", 9, 1));
        opTable.put("PRINT", new OpInfo("IS", 10, 1));

        opTable.put("START", new OpInfo("AD", 1, 0));
        opTable.put("END", new OpInfo("AD", 2, 0));
        opTable.put("ORIGIN", new OpInfo("AD", 3, 0));
        opTable.put("EQU", new OpInfo("AD", 4, 0));
        opTable.put("LTORG", new OpInfo("AD", 5, 0));

        opTable.put("DS", new OpInfo("DL", 1, 0));
        opTable.put("DC", new OpInfo("DL", 2, 0));
    }

    static List<String> tokenize(String line) {
        List<String> toks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;

        for (char ch : line.toCharArray()) {
            if (ch == '\'' || ch == '"') {
                inQuote = !inQuote;
                cur.append(ch);
            } else if (!inQuote && (ch == ' ' || ch == '\t' || ch == ',')) {
                if (cur.length() > 0) {
                    toks.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(ch);
            }
        }
        if (cur.length() > 0) toks.add(cur.toString());
        return toks;
    }

    static boolean isOpOrDirective(String token) {
        return opTable.containsKey(token.toUpperCase());
    }

    static void defineSymbol(String name, Integer addr) {
        SymEntry e = symTable.get(name);
        if (e == null) {
            e = new SymEntry(name);
            symTable.put(name, e);
        }
        if (addr != null) e.address = addr;
    }

    static void processImperative(String opcode, List<String> operands) {
        OpInfo info = opTable.get(opcode);
        StringBuilder repr = new StringBuilder();
        repr.append("(").append(info.cls).append(",")
            .append(String.format("%02d", info.code)).append(") ");

        for (String op : operands) {
            if (isRegister(op)) repr.append("(R,").append(regNumber(op)).append(") ");
            else if (isLiteral(op)) {
                int litIndex = addLiteral(op);
                repr.append("(L,").append(litIndex).append(") ");
            } else if (isNumeric(op)) repr.append("(C,").append(stripQuotes(op)).append(") ");
            else {
                SymEntry s = symTable.get(op);
                if (s == null) {
                    s = new SymEntry(op);
                    s.forwardRefs.add(LC);
                    symTable.put(op, s);
                } else if (s.address == null) s.forwardRefs.add(LC);

                int idx = getSymbolIndex(op);
                repr.append("(S,").append(idx).append(") ");
            }
        }

        intermediate.add(new ICLine(LC, repr.toString().trim()));
        LC += info.length > 0 ? info.length : 1;
    }

    static boolean isRegister(String t) {
        t = t.toUpperCase();
        return t.equals("BREG") || t.equals("CREG") || t.matches("R\\d+");
    }

    static int regNumber(String t) {
        t = t.toUpperCase();
        if (t.equals("BREG")) return 1;
        if (t.equals("CREG")) return 2;
        if (t.startsWith("R")) {
            try { return Integer.parseInt(t.substring(1)); }
            catch (Exception ignored) {}
        }
        return 0;
    }

    static boolean isLiteral(String t) { return t.startsWith("="); }

    static boolean isNumeric(String t) {
        String s = stripQuotes(t);
        try { Integer.parseInt(s); return true; }
        catch (Exception e) { return false; }
    }

    static String stripQuotes(String s) {
        s = s.trim();
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\"")))
            return s.substring(1, s.length() - 1);
        return s;
    }

    static int addLiteral(String lit) {
        for (int i = 0; i < litTable.size(); i++)
            if (litTable.get(i).literal.equals(lit)) return i;

        litTable.add(new LitEntry(lit));
        return litTable.size() - 1;
    }

    static void assignLiteralsToPool() {
        if (poolTable.isEmpty()) poolTable.add(0);

        int currentPoolStart = poolTable.get(poolTable.size() - 1);
        int idx = currentPoolStart;
        boolean anyAssigned = false;

        while (idx < litTable.size()) {
            LitEntry lit = litTable.get(idx);
            if (lit.address == null) {
                lit.address = LC;
                intermediate.add(new ICLine(LC, "(DL,02) (LIT," + lit.literal + ") ; literal placement"));
                LC++;
                anyAssigned = true;
            }
            idx++;
        }

        if (anyAssigned) poolTable.add(litTable.size());
    }

    static int evaluateExpression(String expr) {
        expr = expr.trim();
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+", 2);
            return getSymbolAddressOrConst(parts[0].trim())
                    + Integer.parseInt(stripQuotes(parts[1].trim()));
        }
        if (expr.contains("-")) {
            String[] parts = expr.split("\\-", 2);
            return getSymbolAddressOrConst(parts[0].trim())
                    - Integer.parseInt(stripQuotes(parts[1].trim()));
        }
        return getSymbolAddressOrConst(expr);
    }

    static int getSymbolAddressOrConst(String tok) {
        tok = tok.trim();
        if (isNumeric(tok)) return Integer.parseInt(stripQuotes(tok));
        SymEntry e = symTable.get(tok);
        if (e != null && e.address != null) return e.address;
        try { return Integer.parseInt(stripQuotes(tok)); }
        catch (Exception ex) { return 0; }
    }

    static int getSymbolIndex(String name) {
        int idx = 1;
        for (String k : symTable.keySet()) {
            if (k.equals(name)) return idx;
            idx++;
        }
        symTable.put(name, new SymEntry(name));
        return symTable.size();
    }

    static List<String> readInputLines() throws IOException {
        Path p = Paths.get("input.asm");
        if (Files.exists(p)) return Files.readAllLines(p);

        List<String> sample = Arrays.asList(
            "START 101",
            "READ N",
            "MOVER BREG, ONE",
            "MOVEM BREG, TERM",
            "AGAIN MULT BREG, TERM",
            "MOVER CREG, TERM",
            "ADD CREG, ONE",
            "MOVEM CREG, TERM",
            "COMP CREG, N",
            "BC AGAIN",
            "PRINT RESULT",
            "STOP",
            "N DS 1",
            "RESULT DS 1",
            "ONE DC '1'",
            "TERM DS 1",
            "END"
        );

        System.out.println("input.asm not found. Using default sample.");
        return sample;
    }

    static void printIntermediate() {
        System.out.println("\n--- Intermediate Code ---");
        for (ICLine ic : intermediate)
            System.out.printf("%04d : %s%n", ic.lc, ic.repr);
    }

    static void printSymbolTable() {
        System.out.println("\n--- Symbol Table ---");
        System.out.printf("%-5s %-15s %-8s %-15s%n", "Idx", "Symbol", "Address", "ForwardRefs");
        int idx = 1;
        for (SymEntry s : symTable.values()) {
            System.out.printf("%-5d %-15s %-8s %-15s%n",
                idx++, s.name, (s.address == null ? "-" : s.address), s.forwardRefs);
        }
    }

    static void printLiteralTable() {
        System.out.println("\n--- Literal Table ---");
        System.out.printf("%-5s %-12s %-8s%n", "Idx", "Literal", "Address");
        for (int i = 0; i < litTable.size(); i++) {
            LitEntry l = litTable.get(i);
            System.out.printf("%-5d %-12s %-8s%n", (i + 1), l.literal,
                (l.address == null ? "-" : l.address));
        }
    }

    static void printPoolTable() {
        System.out.println("\n--- Pool Table ---");
        System.out.printf("%-5s %-5s%n", "PoolIdx", "StartLitIdx");
        for (int i = 0; i < poolTable.size(); i++)
            System.out.printf("%-7d %-5d%n", i + 1, poolTable.get(i));
    }

    static String joinIntermediateLines() {
        StringBuilder sb = new StringBuilder();
        for (ICLine ic : intermediate)
            sb.append(String.format("%04d : %s%n", ic.lc, ic.repr));
        return sb.toString();
    }

    static void saveToFile(String name, String content) throws IOException {
        Files.write(Paths.get(name), content.getBytes());
    }

    static void saveSymbolTable(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-15s %-8s %-15s%n",
            "Idx", "Symbol", "Address", "ForwardRefs"));

        int idx = 1;
        for (SymEntry s : symTable.values()) {
            sb.append(String.format("%-5d %-15s %-8s %-15s%n",
                idx++, s.name, (s.address == null ? "-" : s.address), s.forwardRefs));
        }

        saveToFile(name, sb.toString());
    }

    static void saveLiteralTable(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-12s %-8s%n", "Idx", "Literal", "Address"));

        for (int i = 0; i < litTable.size(); i++) {
            LitEntry l = litTable.get(i);
            sb.append(String.format("%-5d %-12s %-8s%n",
                (i + 1), l.literal, (l.address == null ? "-" : l.address)));
        }

        saveToFile(name, sb.toString());
    }

    static void savePoolTable(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-5s%n", "PoolIdx", "StartLitIdx"));
        for (int i = 0; i < poolTable.size(); i++)
            sb.append(String.format("%-7d %-5d%n", i + 1, poolTable.get(i)));
        saveToFile(name, sb.toString());
    }
}
