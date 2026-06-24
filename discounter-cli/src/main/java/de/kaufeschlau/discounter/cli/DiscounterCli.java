package de.kaufeschlau.discounter.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "discounter",
        mixinStandardHelpOptions = true,
        subcommands = ListCommand.class)
public class DiscounterCli implements Runnable {

    public static void main(String[] args) {
        System.exit(new CommandLine(new DiscounterCli()).execute(args));
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
