/**
 * The program divides the text into files by month and day
 *
 * for starting in Windows command line:
 *   java -Dfile.encoding=UTF-8 FileSZSplitter
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

class FileSZSplitter {
    static final String PATH = "C:/Users/lamp/AndroidExercises/SlovaZivota/app/src/main/assets/2021/";
    static final String FILE_NAME = "C:/Users/lamp/AndroidExercises/SlovaZivota/source_texts/Slova_zivota_2021.txt";
    static final String[] DAYS = {"pondělí", "úterý", "středa", "čtvrtek", "pátek", "sobota", "neděle"};
    static final String[] MONTHS = {"ledna", "února", "března", "dubna", "května", "června",
        "července", "srpna", "září", "října", "listopadu", "prosince"};

    public static void main(String[] args) throws IOException {
        int numMonth = -1;
        int numDay = 0;
        int dayOfWeek = -1;
        StringBuffer sb = new StringBuffer();
        List<String> dayList = Arrays.asList(DAYS);
        List<String> monthList = Arrays.asList(MONTHS);
        List<String> lines = Files.readAllLines(Paths.get(FILE_NAME), StandardCharsets.UTF_8);

        for (String line: lines) {
            String[] fields = line.split(" "); // sobota – 29. prosince
            if (fields.length > 3) {
                String day = fields[0];
                String month = fields[3];
                if (dayList.contains(day) && monthList.contains(month)) {
                    // checking the day of the week
                    if (dayOfWeek < 0) {
                        dayOfWeek = dayList.indexOf(day);
                    } else {
                        dayOfWeek = dayOfWeek == 6 ? 0 : dayOfWeek + 1;
                    }
                    if (!day.equals(DAYS[dayOfWeek])) {
                        System.out.printf("Error is %s %s %s\n", day, fields[2], month);
                        return;
                    }
                    System.out.println(line); // title of current day
                    if (numMonth > -1) {
                        sb.delete(0, sb.indexOf("\n\n") + 2);
                        String fileName = PATH + Integer.toString(numMonth) + "/" + Integer.toString(numDay) + ".txt";
                        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
                            writer.append(sb.toString().trim());
                        }
                        sb.setLength(0); // clear
                    }
                    numDay = (int) Float.parseFloat(fields[2]);
                    numMonth = monthList.indexOf(month);
                    File folder = new File(PATH + Integer.toString(numMonth));
                    folder.mkdirs();
                }
            }
            sb.append(line.trim() + "\n");
        }
    }
}
