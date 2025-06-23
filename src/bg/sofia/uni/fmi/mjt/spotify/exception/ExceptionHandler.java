package bg.sofia.uni.fmi.mjt.spotify.exception;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExceptionHandler {
    private static final String ERRORS_FILE_PATH = "errors.txt";
    private static final String SPACE = " ";

    public void handleException(Exception e, String userEmail) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ERRORS_FILE_PATH, true))) {
            bufferedWriter.write("User: " + userEmail + SPACE);
            String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(SPACE));
            bufferedWriter.write("Exception stack trace:  " + stackTrace);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("A problem occurred while writing to a file", ex);
        }
    }
}
