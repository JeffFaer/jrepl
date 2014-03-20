package falgout.jrepl.command.parse;

public interface Parser<I, R> {
    public R parse(I input);
}
