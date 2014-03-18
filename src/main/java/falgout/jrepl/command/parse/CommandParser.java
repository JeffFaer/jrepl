package falgout.jrepl.command.parse;

public interface CommandParser<I, O> {
    public O parse(I input);
}
