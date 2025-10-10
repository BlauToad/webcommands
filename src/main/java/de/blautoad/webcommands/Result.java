package de.blautoad.webcommands;

// Result.java
public class Result {
    private String commandResult;
    private String commandResultText;
    public int debug;

    public Result(String commandResult, String commandResultText) {
        this.commandResult = commandResult;
        this.commandResultText = commandResultText;
    }

    public String getCommandResult() {
        return commandResult;
    }

    public String getCommandResultText() {
        return commandResultText;
    }
}
