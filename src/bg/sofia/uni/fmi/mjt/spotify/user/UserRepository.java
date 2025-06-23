package bg.sofia.uni.fmi.mjt.spotify.user;

import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class UserRepository implements UserRepositoryAPI {
    private static final String USERS_FILE_PATH = "resources\\Users.txt";
    private static final String SPACE = " ";
    private final Set<User> users;

    public UserRepository() {
        users = readUsersFromFile();
    }

    @Override
    public void save(String email, String password) {
        writeUserToFile(email, password);
        users.add(new User(email, password));
    }

    @Override
    public boolean exists(String email) {
        return users.stream()
            .map(User::email)
            .anyMatch(userEmail -> userEmail.equals(email));
    }

    @Override
    public String getUserPassword(String email) throws NotFoundUserException {
        return users.stream()
            .filter(user -> user.email().equals(email))
            .findFirst().orElseThrow(() -> new NotFoundUserException("User with email " + email + " not found"))
            .password();
    }

    private Set<User> readUsersFromFile() {
        Set<User> users = new HashSet<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of(USERS_FILE_PATH))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(SPACE);
                users.add(new User(tokens[0], tokens[1]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        }
        return users;
    }

    private void writeUserToFile(String email, String password) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(USERS_FILE_PATH, true))) {
            bufferedWriter.write(email + SPACE);
            bufferedWriter.write(password + System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }
}
