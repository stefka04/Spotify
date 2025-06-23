package bg.sofia.uni.fmi.mjt.spotify.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCreator {
    private static final char QUOTE = '"';
    private static final char SPACE = ' ';
    private static final String QUOTE_STR = "\"";
    private static final String EMPTY_STR = "";
    private static final String SPACE_STR = " ";
    private static final int COMMAND_INDEX = 0;
    private static final int FIRST_ARGUMENT_INDEX = 1;
    public static Command newCommand(String clientInput) {
        List<String> tokens = CommandCreator.getArgumentsOfCommand(clientInput);
        String clientCommand = tokens.get(COMMAND_INDEX);

        CommandType command =  Arrays.stream(CommandType.values())
            .filter(commandType -> commandType.toString().equalsIgnoreCase(clientCommand))
            .findFirst()
            .orElse(CommandType.UNKNOWN);

        String[] arguments = tokens.subList(FIRST_ARGUMENT_INDEX, tokens.size()).toArray(new String[]{});

        return new Command(command, arguments);
    }

    private static List<String> getArgumentsOfCommand(String input) {
        if (!input.contains(QUOTE_STR)) {
            String[] tokens = input.split(SPACE_STR);
            return Arrays.stream(tokens).toList();
        } else {
            return getTokensOfCommandWithQuotes(input);
        }
    }

    private static List<String> getTokensOfCommandWithQuotes(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean insideQuote = false;

        for (char ch : input.toCharArray()) {
            if (ch == QUOTE) {
                insideQuote = !insideQuote;
            }
            if (ch == SPACE && !insideQuote) {
                String currentArgument = getCurrentArgument(stringBuilder);
                if (currentArgument.isBlank()) {
                    continue;
                }
                tokens.add(currentArgument);
                stringBuilder.delete(0, stringBuilder.length());
            } else {
                stringBuilder.append(ch);
            }
        }

        String currentArgument = getCurrentArgument(stringBuilder);
        if (!currentArgument.isBlank()) {
            tokens.add(currentArgument);
        }

        return tokens;
    }

    private static String getCurrentArgument(StringBuilder stringBuilder) {
        return stringBuilder.toString().replace(QUOTE_STR, EMPTY_STR);
    }
}
