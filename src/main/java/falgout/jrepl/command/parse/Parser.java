package falgout.jrepl.command.parse;

public interface Parser<I, O> {
    public O parse(I input);
}
