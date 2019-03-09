package edu.tsinghua.dbgroup;
import java.lang.Math;
import java.util.*;
import java.util.concurrent.*;

import edu.tsinghua.dbgroup.*;
class EditDistanceJoiner {
    private List<String> mStrings;
    private final TreeMap<Integer, ArrayList<HashMap<String, ArrayList<Integer>>>> mGlobalIndex;
    private final int mThreshold;
    private int[][][] mDistanceBuffers;
    private final int mNumThreads;
    private int mMaxLength;
    private ArrayList<FilteredRawResult> mRawResults;
    static class UnfilteredResult {
        int dstId;
        int dstMatchPos;
        int srcMatchPos;
        int gramLen;
    }
    static class FilteredRawResult {
        int srcId;
        int dstId;
        int similarity;
    }
    private EditDistanceJoiner(int threshold, int numThreads) {
        mGlobalIndex = new TreeMap<>();
        mStrings = new ArrayList<>();
        mMaxLength = 0;
        mThreshold = threshold;
        if (numThreads <= 0) {
            mNumThreads = 0;
        } else if (numThreads > Runtime.getRuntime().availableProcessors()) {
            mNumThreads = Runtime.getRuntime().availableProcessors();
        } else {
            mNumThreads = numThreads;
        }
    }
    public EditDistanceJoiner(int threshold) {
        this(threshold, Runtime.getRuntime().availableProcessors());
    }
    private int calculateEditDistanceWithThreshold(String s1, int start1, int l1,
                                                   String s2, int start2, int l2, int threshold, int[][] distanceBuffer){
        if (threshold < 0) {
            return 0;
        }
        if (threshold == 0) {
            String sub1 = s1.substring(start1, start1 + l1);
            String sub2 = s2.substring(start2, start2 + l2);
            return sub1.equals(sub2) ? 0 : 1;
        }
        if (l1 == 0) {
            return l2;
        }
        if (l2 == 0) {
            return l1;
        }
        for (int j = 1; j <= l1; j++) {
            int start = Math.max(j - threshold, 1);
            int end = Math.min(l2, j + threshold);
            if (j - threshold - 1 >= 1) {
                distanceBuffer[j - threshold - 1][j] = threshold + 1;
            }
            for (int i = start; i <= end; i++) {
                if (s1.charAt(start1 + j - 1) == s2.charAt(start2 + i - 1)) {
                    distanceBuffer[i][j] = distanceBuffer[i - 1][j - 1];
                } else {
                    distanceBuffer[i][j] = Math.min(distanceBuffer[i - 1][j - 1] + 1,
                        Math.min(distanceBuffer[i - 1][j] + 1, distanceBuffer[i][j - 1] + 1));
                }
            }
            if (end < l2)
                distanceBuffer[end + 1][j] = threshold + 1;
            boolean earlyTerminateFlag = true;
            for (int i = start; i <= end; i++) {
                if (distanceBuffer[i][j] <= threshold) {
                    earlyTerminateFlag = false;
                    break;
                }
            }
            if (earlyTerminateFlag)
                return threshold + 1;
        }
        return distanceBuffer[l2][l1];
    }
    private void indexStringById(int stringId){
        String stringIndexing = mStrings.get(stringId);
        int l = stringIndexing.length();//3 3 2
        if (!mGlobalIndex.containsKey(l)) {
            int strLen = 0;
            ArrayList<HashMap<String, ArrayList<Integer>>> subIndex = new ArrayList<>();
            while (strLen < mThreshold + 1) { 
                subIndex.add(new HashMap<>());
                strLen++;
            }
            mGlobalIndex.put(l, subIndex);
        }
        for (int i = 0; i < mThreshold + 1; i++) {
            int gramLen = getGramLen(l, i);
            int startPos = getGramPos(l, i);
            String gram = stringIndexing.substring(startPos, startPos + gramLen);
            if (mGlobalIndex.get(l).get(i).containsKey(gram)) {
                mGlobalIndex.get(l).get(i).get(gram).add(stringId);
            } else {
                ArrayList<Integer> invertedList = new ArrayList<>();
                invertedList.add(stringId);
                mGlobalIndex.get(l).get(i).put(gram, invertedList);
            }
        }
    }
    private void initEditDistanceBuffer(){
        mDistanceBuffers = new int[mNumThreads + 1][mMaxLength][mMaxLength];
        for (int n = 0; n <= mNumThreads; n++) {
            for (int i = 0; i < mMaxLength; i++) {
                mDistanceBuffers[n][0][i] = i;
                mDistanceBuffers[n][i][0] = i;
            }
        }
    }
    public ArrayList<EditDistanceJoinResult> getJoinResults() {
        if(mStrings.size() == 0){
            return new ArrayList<>();
        }
        long resultsBeforeRefiningNum = 0;
        long resultsRefinedNum = 0;
        long mainTid = Thread.currentThread().getId();
        initEditDistanceBuffer();
        mStrings = new ArrayList<>(new TreeSet<>(mStrings));
        mStrings.sort((o1, o2) -> compareString(o1, o2));
        ArrayList<EditDistanceJoinResult> mResults = new ArrayList<>();
        mRawResults = new ArrayList<>();
        int srcId = 1;
        ThreadPoolExecutor executor = null;
        if(mNumThreads != 0) {
             executor = new ThreadPoolExecutor(mNumThreads, mNumThreads, 0L,
                                       TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(3000),
                                          new ThreadPoolExecutor.CallerRunsPolicy());
        }
        indexStringById(0);
        while(srcId < mStrings.size()){
            int srcLen = mStrings.get(srcId).length();
            ArrayList<UnfilteredResult> resultsBeforeRefining = new ArrayList<>();
            getResultsFromIndex(srcId, resultsBeforeRefining);
            resultsBeforeRefiningNum += resultsBeforeRefining.size();
            final int currentId = srcId;
            if (mNumThreads != 0) {
                executor.submit(() -> {
                    long tid = Thread.currentThread().getId();
                    int[][] buffer = mDistanceBuffers[(int)(tid % mNumThreads)];
                    if (tid == mainTid) {
                        buffer = mDistanceBuffers[mNumThreads];
                    }
                    ArrayList<FilteredRawResult> resultsRefined = null;
                    synchronized(buffer) {
                        resultsRefined = refineResults(currentId, resultsBeforeRefining, buffer);
                    }
                    synchronized(mRawResults){
                        mRawResults.addAll(resultsRefined);
                    }
                });
            } else {
                int[][] buffer = mDistanceBuffers[0];
                ArrayList<FilteredRawResult> resultsRefined = null;
                resultsRefined = refineResults(currentId, resultsBeforeRefining, buffer);
                mRawResults.addAll(resultsRefined);
            }
            mGlobalIndex.subMap(0, true, Math.max(1, srcLen - mThreshold), false).clear();
            indexStringById(srcId);
            srcId++;
        }
        if(mNumThreads != 0) {
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        mRawResults.sort((o1, o2) -> {
            if (o1.srcId < o2.srcId)
                return -1;
            if (o1.srcId > o2.srcId)
                return 1;
            if (o1.dstId < o2.dstId)
                return -1;
            if (o1.dstId > o2.dstId)
                return 1;
            return 0;
        });
        for(FilteredRawResult rawResult : mRawResults) {
            EditDistanceJoinResult r = new EditDistanceJoinResult();
            r.src = mStrings.get(rawResult.srcId);
            r.dst = mStrings.get(rawResult.dstId);
            r.similarity = rawResult.similarity;
            mResults.add(r);
        }
        return mResults;
    }
    private void getResultsFromIndex(int srcId, ArrayList<UnfilteredResult> resultsBeforeRefining){
        String src = mStrings.get(srcId);
        int srcLen = src.length();
        for (int dstLen = Math.max(srcLen - mThreshold, mThreshold + 1);
          dstLen <= mGlobalIndex.lastKey();
          dstLen++) {
            if(!mGlobalIndex.containsKey(dstLen)){
                continue;
            }
            int delta = srcLen - dstLen;
            for (int gramNo = 0; gramNo <= mThreshold; gramNo++) {
                int candidateGramPos = getGramPos(dstLen, gramNo);
                int candidateGramLen = getGramLen(dstLen, gramNo);
                int startPos = Math.max(Math.max(candidateGramPos - gramNo, 
                    candidateGramPos + delta + gramNo - mThreshold), 0);
                int endPos = Math.min(Math.min(candidateGramPos + gramNo, 
                    candidateGramPos + delta - gramNo + mThreshold), srcLen - candidateGramLen);
                for (; startPos <= endPos; startPos++) {
                    String gram = src.substring(startPos, startPos + candidateGramLen);
                    ArrayList<Integer> invertedList = mGlobalIndex.get(dstLen).get(gramNo).get(gram);
                    if (invertedList != null) {
                        for (Integer anInvertedList : invertedList) {
                            int dstId = anInvertedList;
                            UnfilteredResult t = new UnfilteredResult();
                            t.dstId = dstId;
                            t.dstMatchPos = candidateGramPos;
                            t.srcMatchPos = startPos;
                            t.gramLen = candidateGramLen;
                            resultsBeforeRefining.add(t);
                        }
                    }
                }
            }
        }
        resultsBeforeRefining.sort((a, b) -> {
            if (a.dstId < b.dstId)
                return -1;
            if (a.dstId > b.dstId)
                return 1;
            return 0;
        });
    }
    private int filterCandidate(String src, String dst, int srcMatchPos, int dstMatchPos, int len,
        int[][] distanceBuffer){
        int srcRightLen = src.length() - srcMatchPos - len;
        int dstRightLen = dst.length() - dstMatchPos - len;
        int leftThreshold = mThreshold - Math.abs(srcRightLen - dstRightLen);
        int leftDistance = calculateEditDistanceWithThreshold(src, 0, srcMatchPos,
            dst, 0, dstMatchPos, 
            leftThreshold, distanceBuffer);
        if (leftDistance > leftThreshold) {
            return -1;
        }
        int rightThreshold = mThreshold - leftDistance;
        int rightDistance = calculateEditDistanceWithThreshold(
            src, srcMatchPos + len, src.length() - srcMatchPos - len, 
            dst, dstMatchPos + len, dst.length() - dstMatchPos - len,
            rightThreshold, distanceBuffer);
        if (rightDistance > rightThreshold) {
            return -1;
        }
        return leftDistance + rightDistance;
    }
    private ArrayList<FilteredRawResult> refineResults(int srcId, 
        ArrayList<UnfilteredResult> resultsBeforeRefining, int[][] distanceBuffer){
        ArrayList<FilteredRawResult> resultsRefined = new ArrayList<>();
        HashSet<Integer> matchStringIds = new HashSet<>();
        for (UnfilteredResult t : resultsBeforeRefining) {
            int dstId = t.dstId;
            if(matchStringIds.contains(dstId)){
                continue;
            }
            int dstMatchPos = t.dstMatchPos;
            int srcMatchPos = t.srcMatchPos;
            String dst = mStrings.get(dstId);
            String src = mStrings.get(srcId);
            int len = t.gramLen;
            int distance = filterCandidate(src, dst, srcMatchPos, dstMatchPos, len, distanceBuffer);
            if(distance != -1){
                FilteredRawResult r = new FilteredRawResult();
                r.srcId = dstId;
                r.dstId = srcId;
                r.similarity = distance;
                resultsRefined.add(r);
                matchStringIds.add(dstId);
            }
        }
        return resultsRefined;
    }
    public void populate(String s) {
        if(s.length() > mThreshold){
            mStrings.add(s);
            mMaxLength = Math.max(mMaxLength, s.length());
        }
    }
    public void populate(List<String> strings){
        for(String s : strings){
            populate(s);
        }
    }
    static private int compareString(String o1, String o2) {
        if (o1.length() > o2.length()) {
            return 1;
        } else if (o1.length() < o2.length()) {
            return -1;
        }
        return o1.compareTo(o2);
    }
    private int getGramPos(int strLen, int gramNo){
        int shortGramLen = strLen / (mThreshold + 1);
        int longGramOffset = gramNo - (mThreshold + 1 - strLen % (mThreshold + 1));
        if(longGramOffset > 0){
            return shortGramLen * gramNo + longGramOffset;
        }
        return shortGramLen * gramNo;
    }
    private int getGramLen(int strLen, int gramNo){
        int shortGramLen = strLen / (mThreshold + 1);
        if(gramNo + strLen % (mThreshold + 1) >= mThreshold + 1){
            return shortGramLen + 1;
        }
        return shortGramLen;
    }

}
