import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.lang.reflect.Field;

// for Windows:
// java -Dfile.encoding=UTF-8 FileSZSplitter

class FileSZSplitter {
    static final String PATH = "C:/Users/lamp/AndroidExercises/SlovaZivota/app/src/main/assets/2021/";
    static final String FILE_NAME = "C:/Users/lamp/AndroidExercises/SlovaZivota/source_texts/Slova_zivota_2021.txt";
    static final String[] DAYS = {"pondělí", "úterý", "středa", "čtvrtek", "pátek", "sobota", "neděle"};
    static final String[] MONTHS = {"ledna", "února", "března", "dubna", "května", "června",
        "července", "srpna", "září", "října", "listopadu", "prosince"};

    public static void main(String[] args) throws IOException {
        int numMonth = -1;
        int numDay = -1;
        StringBuffer sb = new StringBuffer();
        List<String> monthsList = Arrays.asList(MONTHS);
        List<String> lines = Files.readAllLines(Paths.get(FILE_NAME), StandardCharsets.UTF_8);

        for (String line: lines) {
            String[] fields = line.split(" "); // sobota – 29. prosince
            if (fields.length > 3) {
                String month = fields[3];
                if (monthsList.contains(month)) {
                    System.out.println(line);
                    //System.out.println(Arrays.toString(fields));
                    if (numMonth > -1) {
                        sb.delete(0, sb.indexOf("\n\n") + 2);
                        String fileName = PATH + Integer.toString(numMonth) + "/" + Integer.toString(numDay) + ".txt";
                        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
                            writer.append(sb.toString().trim());
                        }
                        sb.setLength(0);
                    }
                    numDay = (int) Float.parseFloat(fields[2]);
                    numMonth = monthsList.indexOf(month);
                    File folder = new File(PATH + Integer.toString(numMonth));
                    folder.mkdirs();
                }
            }
            sb.append(line.trim() + "\n");
        }
    }
}
