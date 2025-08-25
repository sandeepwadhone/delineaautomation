import java.io.File;
import java.io.FileInputStream;

public class FileTest {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\ACER\\Documents\\sandeep\\secrets.xlsx";
        File file = new File(filePath);
        System.out.println("File exists: " + file.exists());
        System.out.println("File readable: " + file.canRead());
        try (FileInputStream fis = new FileInputStream(file)) {
            System.out.println("File opened successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}