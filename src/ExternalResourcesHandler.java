import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ExternalResourcesHandler {

    static String fileName = "";

    static ProcessBuilder processBuilder1 = new ProcessBuilder(
            "src/Resources/avrasm2.exe",
            "-v3",
            "-I",
            "src/Resources",
            "-fI",
            "-o"
    );

    static ProcessBuilder processBuilder2 = new ProcessBuilder(
            "src/Resources/avrdude.exe",
            "-c", "arduino",
            "-p", "m328p",
            "-P","COM3",
            "-b","115200",
            "-D",
            "-U"
    );

    public static void setName(String fileName){
        ExternalResourcesHandler.fileName = fileName;
    }

    public static void sendToMicroController() {
        processBuilder1.command().add("src/%s.hex".formatted(fileName));
        processBuilder1.command().add("src/%s.asm".formatted(fileName));

        processBuilder2.command().add("flash:w:\"src/%s.hex\":i".formatted(fileName));
        try {
            processBuilder1.redirectErrorStream(true);
            processBuilder2.redirectErrorStream(true);

            Process process = processBuilder1.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);

            Process process2 = processBuilder2.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process2.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            exitCode = process2.waitFor();
            System.out.println("Exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
