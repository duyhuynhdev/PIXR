package au.edu.rmit.blockchain.crypto.pixr.algorithms;

import au.edu.rmit.blockchain.crypto.common.utils.CsvFileManager;
import au.edu.rmit.blockchain.crypto.common.utils.Setting;
import au.edu.rmit.blockchain.crypto.common.utils.Util;
import au.edu.rmit.blockchain.crypto.common.utils.measurement.Dynamometer;
import au.edu.rmit.blockchain.crypto.pixr.algorithms.results.PIXRDistinctVectorFinderResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistinctVectorTest extends Environment {

    private int twoPow(int n) {
        return (int) Math.pow(2, n);
    }

    @Test
    void runDVExperiment() throws IOException, NotMatchException {
        TestResult result = new TestResult();
        for (Integer size : new Integer[]{twoPow(10),
                twoPow(12),
                twoPow(14),
                twoPow(16),
                twoPow(18),
                twoPow(19),
                twoPow(20),
                twoPow(21),
                twoPow(22),
                twoPow(23)}) {
            System.out.println("::::DATA SET " + size + "::::");
            var dataSets = getDataSets(size, 1);
            for (int id = 0; id < dataSets.size(); id++) {
                simulate(result, dataSets.get(id), id + 1, new DistinctVectorExtraction());
            }
        }
        result.exportResult(Setting.DISTINCT_VECTOR_HASH);
    }

    private void simulate(TestResult result, List<String> txs, int setNo, PIXRDistinctVectorStrategy strategy) {
        System.out.println("******************************************************************");
        // Server part
        PIXRDistinctVectorFinderResult message = server(txs, strategy);
        // Client part
        int txIdx = new Random().nextInt(txs.size());
        String vTx = txs.get(txIdx);
        System.out.println("::::Picking random tx in transaction list to check::::");
        client(message, vTx, txIdx);
        System.out.println("******************************************************************");
        result.addResult(message, setNo);
    }

    private PIXRDistinctVectorFinderResult server(List<String> txs, PIXRDistinctVectorStrategy strategy) {
        System.out.println("::::SERVER::::");
        System.out.println("\tNumber of transaction:" + txs.size());
        System.out.println("\t>>> Start to build distinct vectors");
        var result = Dynamometer.measure(() -> {
            PIXRDistinctVectorFinder finder = new PIXRDistinctVectorFinder(strategy, Setting.HASH_CODE_LENGTH);
            for (String tx : txs) {
                finder.put(Util.parseHex2Binary(tx));
            }
            return finder.find();
        });
        var message = result.getResult();
        message.setEncodeTime(result.getDuration());
        System.out.println("\t<<< FINISHED");
        System.out.println("\tVector length:" + message.getVectors().get(0).length());
        System.out.println("\tStart position:" + message.getPositions().get(0));
        System.out.println("\tMessage size (start-position-length + list-vector-length):" + message.measureResultSize() + " bits");
        return message;
    }

    private void client(PIXRDistinctVectorFinderResult message, String checkTxHash, long realIdx) {
        System.out.println("::::CLIENT::::");
        System.out.println("\tChecked transaction: txHash = " + checkTxHash);
        System.out.println("\t>>> Start to extract a vector from tx and check its index in the list");
        var result = Dynamometer.measure(() -> {
            int startPosition = message.getPositions().get(0);
            int length = message.getVectors().get(0).length();
            String vector = Util.parseHex2Binary(checkTxHash).substring(startPosition, startPosition + length);
            return message.getVectors().indexOf(vector);
        });
        System.out.println("\t<<< FINISHED");
        int idx = result.getResult();
        message.setDecodeTime(result.getDuration());
        System.out.println("\tReal Idx: " + realIdx);
        if (idx == realIdx) {
            System.out.println("\tTx was found correctly in position: " + idx);
        } else {
            System.out.println("\tTx idx was not correct with value: " + idx);
        }
    }

    static class TestResult {
        private final List<String[]> results = new ArrayList<>();

        public void exportResult(String path) throws IOException, NotMatchException {
            CsvFileManager csvFileManager = new CsvFileManager(Setting.RESULT_HOME + path,
                    "transaction_size", "set_no", "initial_length", "encode_run_time", "decode_run_time", "vector_length", "start_position", "message_size", "firstFindSteps", "totalSteps");
            csvFileManager.write(results);
        }

        public void addResult(PIXRDistinctVectorFinderResult result, int setNo) {
            results.add(new String[]{
                    String.valueOf(result.getTxSize()),
                    String.valueOf(setNo),
                    String.valueOf(2 * (int) Util.log2(result.getTxSize())),
                    String.valueOf(result.getEncodeTime()),
                    String.valueOf(result.getDecodeTime()),
                    String.valueOf(result.getVectors().get(0).length()),
                    String.valueOf(result.getPositions().get(0)),
                    String.valueOf(result.measureResultSize()),
                    String.valueOf(result.getFirstFindSteps()),
                    String.valueOf(result.getTotalSteps())
            });
        }
    }
}
