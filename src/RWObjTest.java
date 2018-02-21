import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RWObjTest {
    private static final String XML_FILE = "objects.xml";
    private static final String JSON_FILE = "objects.json";
    private static final int NUM_SERV_REC = 4;
    private static final String XML_OBJ_BEGIN = "\t<";
    private static final String XML_OBJ_END = "\t</";
    private static final String XML_PARAM_BEGIN = "\t\t<";
    private static final String JSON_OBJ_BEGIN = "\t\"";
    private static final String JSON_PARAM_BEGIN = "\t\t\"";
    private Object[] objects;
    private FileWriter fw;

    public RWObjTest(boolean toXml) {
        try {
            if (toXml) {
                fw = new FileWriter(XML_FILE, false);
            } else {
                fw = new FileWriter(JSON_FILE, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(boolean toXML, Object...objects) {
        if (toXML) {
            writeXML(objects);
        } else {
            writeJSON(objects);
        }
    }

    private void writeXML(Object... objects) {
        if (objects.length == 0) return;
        try (PrintWriter pw = new PrintWriter(fw)) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            pw.println("<!DOCTYPE " + objects[0].getClass().getSimpleName() + "s>");
            pw.println("<" + objects[0].getClass().getSimpleName() + "s>");
            for (Object o : objects) {
                pw.println("\t<" + o.getClass().getSimpleName() + ">");
                Field[] fields = o.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    pw.println(XML_PARAM_BEGIN + fieldName + ">" + field.get(o) + "</" + fieldName + ">");
                }
                pw.println(XML_OBJ_END + o.getClass().getSimpleName() + ">");
            }
            pw.println("</" + objects[0].getClass().getSimpleName() + "s>");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void writeJSON(Object... objects) {
        if (objects.length == 0) return;
        try (PrintWriter pw = new PrintWriter(fw)) {
            pw.println("{");
            for (int j = 0; j < objects.length; j++) {
                pw.println(JSON_OBJ_BEGIN + objects[j].getClass().getSimpleName() + "\": {");
                Field[] fields = objects[j].getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    String fieldName = fields[i].getName();
                    pw.print("\t\t\"" + fieldName + "\": \"" + fields[i].get(objects[j]) + "\"");
                    if (i == fields.length - 1) {
                        pw.println();
                    } else {
                        pw.println(",");
                    }
                }
                if (j == objects.length - 1) {
                    pw.println("\t}");
                } else {
                    pw.println("\t},");
                }
            }
            pw.println("}");

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object[] read(boolean fromXML) {
        if (fromXML) {
            return read();
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(JSON_FILE), StandardCharsets.UTF_8);
            if (lines.size() > NUM_SERV_REC) {
                objects = new Object[lines.size() - NUM_SERV_REC];
            } else {
                System.out.println("No data!");
                return null;
            }
            int countObj = 0;
            for (String s : lines) {
                if (s.contains(JSON_OBJ_BEGIN) && !s.contains(JSON_PARAM_BEGIN)) {
                    countObj++;
                }
            }
            if (countObj == 0) return null;
            objects = new Object[countObj];
            for (int i = 0, j = 0; j < countObj; ) {
                String s = lines.get(i);
                if (s.contains(JSON_OBJ_BEGIN) && !s.contains(JSON_PARAM_BEGIN)) {
                    String objClass = s.substring(JSON_OBJ_BEGIN.length(), s.indexOf("\":"));
                    objects[j] = Class.forName(objClass).newInstance();
                    s = lines.get(++i);
                    while (s.contains(JSON_PARAM_BEGIN)) {
                        String fieldName = s.substring(JSON_PARAM_BEGIN.length(), s.indexOf("\":"));
                        Field field = objects[j].getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        String param = s.substring(s.indexOf("\": \"") + ("\": \"").length(), s.lastIndexOf("\""));
                        try {
                            field.set(objects[j], param);
                        } catch (IllegalArgumentException iae) {
                            field.set(objects[j], Double.parseDouble(param)); //
                        }
                        s = lines.get(++i);
                    }
                    j++;
                } else i++;
            }
        } catch (IllegalAccessException | IOException | InstantiationException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return objects;
    }

    private Object[] read() {
        int countObj = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get(XML_FILE), StandardCharsets.UTF_8);
            if (lines.size()> NUM_SERV_REC) {
                objects = new Object[lines.size() - NUM_SERV_REC];
            } else {
                System.out.println("No data!");
                return null;
            }
            for (String s : lines) {
                if (s.contains(XML_OBJ_END)) {
                    countObj++;
                }
            }
            if (countObj == 0) return null;
            objects = new Object[countObj];
            for(int i=0, j=0; j<countObj;) {
                String s = lines.get(i);
                if(s.contains(XML_OBJ_BEGIN) && !s.contains("\t\t") && !s.contains("/")) {
                    String objClass = s.substring(XML_OBJ_BEGIN.length(), s.indexOf(">"));
                    objects[j] = Class.forName(objClass).newInstance();
                    s = lines.get(++i);
                    while (s.contains(XML_PARAM_BEGIN)) {
                        String fieldName = s.substring(XML_PARAM_BEGIN.length(), s.indexOf(">"));
                        Field field = objects[j].getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        String param = s.substring(s.indexOf(">") + 1, s.indexOf("</"));
                        try {
                            field.set(objects[j], param);
                        } catch (IllegalArgumentException iae) {
                            field.set(objects[j], Double.parseDouble(param));
                        }
                        s = lines.get(++i);
                    }
                    j++;
                } else i++;

            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return objects;
    }
}
