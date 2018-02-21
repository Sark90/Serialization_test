import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class RWText {
    private static final String XML_FILE = "prices.xml";
    private static final String XML_NAME_PAR = "flowername=\"";
    private static final String XML_COLOR_PAR = "\" color=\"";
    private static final String XML_PRICE_PAR = "\">";
    private static final String XML_PRICE_PAR_END = "</price>";
    private static final int XML_NUM_SERV_REC = 4;

    private static final String JSON_FILE = "prices.json";
    private static final String JSON_NAME_PAR = "\t\t\"name\": \"";
    private static final String JSON_COLOR_PAR = "\t\t\"color\": \"";
    private static final String JSON_PRICE_PAR = "\t\t\"price\": \"";
    private static final int JSON_INDEX_DELTA = 2;
    private static final int JSON_LAST_INDEX_DELTA = 1;

    private Flower[] flowers;
    private String name, color;
    private double price;

    public void write(Flower... flowers) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(XML_FILE, false))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            pw.println("<!DOCTYPE prices>");
            pw.println("<prices>");
            for (Flower f : flowers) {
                pw.print("\t<price " + XML_NAME_PAR + f.getName() + XML_COLOR_PAR + f.getColor() + "\">");
                pw.println(f.getPrice() + XML_PRICE_PAR_END);
            }
            pw.println("</prices>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(boolean writeXML, Flower... flowers) {
        if (writeXML) {
            write(flowers);
        } else {
            try (PrintWriter pw = new PrintWriter(new FileWriter(JSON_FILE, false))) {
                pw.println("{");
                for (int i = 0; i < flowers.length; i++) {
                    pw.println("\t\"flower\": {");
                    pw.println(JSON_NAME_PAR + flowers[i].getName() + "\",");
                    pw.println(JSON_COLOR_PAR + flowers[i].getColor() + "\",");
                    pw.println(JSON_PRICE_PAR + flowers[i].getPrice() + "\"");
                    if (i == flowers.length - 1) {
                        pw.println("\t}");
                    } else {
                        pw.println("\t},");
                    }
                }
                pw.println("}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(XML_FILE), StandardCharsets.UTF_8);
            if (lines.size()> XML_NUM_SERV_REC) {
                flowers = new Flower[lines.size() - XML_NUM_SERV_REC];
            } else {
                System.out.println("No data!");
                return;
            }
            for(int i=0, j=0; i<lines.size(); i++){
                String s = lines.get(i);
                if(s.contains(XML_NAME_PAR)) {
                    name = s.substring((s.indexOf(XML_NAME_PAR) + XML_NAME_PAR.length()), s.indexOf(XML_COLOR_PAR));
                    color = s.substring((s.indexOf(XML_COLOR_PAR) + XML_COLOR_PAR.length()), s.indexOf(XML_PRICE_PAR));
                    String p = s.substring((s.indexOf(XML_PRICE_PAR) + XML_PRICE_PAR.length()), s.indexOf(XML_PRICE_PAR_END));
                    price = Double.parseDouble(p);
                    flowers[j++] = new Flower(name, color, price);
                }
            }
        } catch (NoSuchFileException nfe) {
            System.out.println(nfe);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(boolean readXML) {
        if (readXML) {
            read();
        } else {
            int countFlowers = 0;
            try {
                List<String> lines = Files.readAllLines(Paths.get(JSON_FILE), StandardCharsets.UTF_8);
                for (String s: lines) {
                    if(s.contains(JSON_NAME_PAR)) {
                        countFlowers++;
                    }
                }
                if (countFlowers == 0) {
                    System.out.println("No data!");
                    return;
                }
                flowers = new Flower[countFlowers];
                for(int i=0, j=0; i<lines.size(); i++) {
                    String s = lines.get(i);
                    if(s.contains(JSON_NAME_PAR)) {
                        name = s.substring(JSON_NAME_PAR.length(), s.length()-JSON_INDEX_DELTA);
                    }
                    if(s.contains(JSON_COLOR_PAR)) {
                        color = s.substring(JSON_COLOR_PAR.length(), s.length()-JSON_INDEX_DELTA);
                    }
                    if(s.contains(JSON_PRICE_PAR)) {
                        price = Double.parseDouble(s.substring(JSON_PRICE_PAR.length(), s.length()-JSON_LAST_INDEX_DELTA));
                        flowers[j++] = new Flower(name, color, price);
                    }
                }
            } catch (NoSuchFileException nfe) {
                System.out.println(nfe);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public double getPrice(String name, String color) {
        if (flowers==null || flowers.length==0 || flowers[0]==null) {
            System.out.println("Array not initialized!");
            return 0;
        }
        double p = 0;
        for(Flower f: flowers) {
            if(f.getName().equals(name) && f.getColor().equals(color)) {
                p = f.getPrice();
            }
        }
        if (p == 0) {
            System.out.print("flower not found, ");
        }
        return p;
    }
}
