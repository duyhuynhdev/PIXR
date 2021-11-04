package au.edu.rmit.blockchain.crypto.pixr.algorithms;

import au.edu.rmit.blockchain.crypto.common.data.PIXRDataAccess;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public class Environment {
    private final PIXRDataAccess dataAccess = new PIXRDataAccess();

    public ImmutableList<ImmutableList<String>> getDataSets(DataSize size, int number) throws IOException {
        return dataAccess.loadBySet(size.getValue(), number);
    }

    public ImmutableList<ImmutableList<String>> getDataSets(int size, int number) throws IOException {
        return dataAccess.loadBySet(size, number);
    }

    @BeforeAll
    @AfterAll
    static void clean() {
        System.gc();
    }
}
