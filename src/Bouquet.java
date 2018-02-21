import java.io.*;

public class Bouquet implements Serializable {
    private static final String FILE = "prices";

    private static boolean xml = true; //== false - r/w to JSON
    private Flower[] flowers;

    public Bouquet(boolean toXml) {
        xml = toXml;
    }

    public void addFlowers(Flower...flowers) {
        this.flowers = flowers;
    }

    public void showFlowers() {
        if (flowers == null) return;
        for (Flower f: flowers) {
            System.out.println("Name: " + f.getName() + ", color: " + f.getColor() + ", price: " + f.getPrice());
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (flowers == null) return;
        new RWObj().write(xml, flowers);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Object[] objects = new RWObj().read(xml);
        flowers = new Flower[objects.length];
        for (int i=0; i<objects.length; i++) {
            flowers[i] = (Flower) objects[i];
        }
    }

    public void serialize() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) { //replace with another stream?
            oos.writeObject(this);
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(FILE)); //replace with another stream?
            Bouquet bouquetFromFile = (Bouquet) oin.readObject();
            bouquetFromFile.showFlowers();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
