package falgout.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Closeables {
    public static void closeAll(Closeable... closeables) throws IOException {
        Queue<IOException> exceptions = Collections.asLifoQueue(new LinkedList<>());
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException e) {
                exceptions.add(e);
            }
        }
        
        throwFirst(exceptions);
    }
    
    public static <X extends Throwable> void throwFirst(Iterable<? extends X> exceptions) throws X {
        Iterator<? extends X> i = exceptions.iterator();
        if (i.hasNext()) {
            X e = i.next();
            i.forEachRemaining(e::addSuppressed);
            
            throw e;
        }
    }
}
