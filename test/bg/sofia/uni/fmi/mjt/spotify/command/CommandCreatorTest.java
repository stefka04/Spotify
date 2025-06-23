package bg.sofia.uni.fmi.mjt.spotify.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandCreatorTest {
    @Test
    void testNewCommand() {
        String input = "disconnect";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.DISCONNECT, new String[0]);

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandUnknownCommand() {
        String input = "logout";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.UNKNOWN, new String[0]);

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandWithOneArgument() {
        String input = "top 3";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.TOP, new String[]{"3"});

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandWithTwoArgument() {
        String input = "register \"stefka\" \"password\"";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.REGISTER, new String[]{"stefka", "password"});

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandWithEmptyArgument() {
        String input = "register \"stefka\" \"\"";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.REGISTER, new String[]{"stefka"});

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandWithBlankArgument() {
        String input = "register \"stefka\" \"             \"";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.REGISTER, new String[]{"stefka"});

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }

    @Test
    void testNewCommandWithThreeArgument() {
        String input = "add-song-to \"playlist\" \"song name\" \"singer name\"";
        Command resultCommand = CommandCreator.newCommand(input);
        Command expectedCommand = new Command(CommandType.ADD_SONG_TO, new String[]{"playlist", "song name","singer name" });

        assertEquals(expectedCommand.command(), resultCommand.command(), "Expected command: "
            + expectedCommand.command() + " but was: " + resultCommand.command());
        assertArrayEquals(expectedCommand.arguments(), resultCommand.arguments(), "Expected command arguments: "
            + Arrays.toString(expectedCommand.arguments()) + " but was: " + Arrays.toString(resultCommand.arguments()));
    }
}



