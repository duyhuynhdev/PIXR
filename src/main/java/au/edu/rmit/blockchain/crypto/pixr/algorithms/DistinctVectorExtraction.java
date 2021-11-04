package au.edu.rmit.blockchain.crypto.pixr.algorithms;

import au.edu.rmit.blockchain.crypto.common.utils.Util;
import au.edu.rmit.blockchain.crypto.pixr.algorithms.results.PIXRDistinctVectorFinderResult;
import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.List;

public class DistinctVectorExtraction implements PIXRDistinctVectorStrategy {
    private static LinkedHashSet<String> distinctVectors = null;
    private static int bestStartPosition = 0;
    int totalSteps = 0;
    int firstFindSteps = 0;

    public DistinctVectorExtraction() {

    }

    private void reset() {
        totalSteps = 0;
        firstFindSteps = 0;
        distinctVectors = null;
        bestStartPosition = 0;
    }

    @Override
    public PIXRDistinctVectorFinderResult run(List<String> txBinaryList, int hashLength) throws NotMatchException {
        reset();
        int length = 2 * (int) Util.log2(txBinaryList.size());

        while (length <= hashLength) {
            int startPosition = 0;
            while (startPosition + length <= hashLength) {
                totalSteps++;
                firstFindSteps++;
                if (isUnique(startPosition, length, txBinaryList, true)) {
                    return binarySearchRun(length, length, List.copyOf(distinctVectors));
                }
                startPosition+=length;
            }
            length++;
        }
        throw new NotMatchException("Cannot find set of distinct vectors that match to inputted condition");
    }

    private PIXRDistinctVectorFinderResult binarySearchRun(int length, int hashLength, List<String> txBinaryList) throws NotMatchException {
        int first = 1;
        int last = hashLength;
        while (first <= last) {
            int startPosition = 0;
            while (startPosition + length <= hashLength) {
                totalSteps++;
                if (isUnique(startPosition, length, txBinaryList, false)) {
                    last = length - 1;
                    break;
                }
                startPosition += 1;
            }
            if (startPosition + length > hashLength) {
                first = length + 1;
            }
            length = (first + last) / 2;
        }
        if (distinctVectors == null)
            throw new NotMatchException("Cannot find set of distinct vectors that match to inputted condition");
        return new PIXRDistinctVectorFinderResult(firstFindSteps, totalSteps, txBinaryList.size(), hashLength,
                List.of(bestStartPosition), ImmutableList.copyOf(distinctVectors));
    }

    private boolean isUnique(int start, int length, List<String> txBinaryList, boolean updateStart) {
        LinkedHashSet<String> checkSet = new LinkedHashSet<>();
        for (String code : txBinaryList) {
            String subCode = code.substring(start, start + length);
            if (!checkSet.add(subCode)) {
                return false;
            }
        }
        // save current best setting
        distinctVectors = checkSet;
        if (updateStart) {
            bestStartPosition = start;
        } else {
            bestStartPosition += start;
        }
        return true;
    }
}
