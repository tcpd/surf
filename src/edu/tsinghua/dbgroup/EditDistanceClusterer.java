package edu.tsinghua.dbgroup;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.io.Serializable;
import java.util.Comparator;
import edu.tsinghua.dbgroup.*;

public class EditDistanceClusterer {
    private final EditDistanceJoiner mJoiner;
    static class SizeComparator implements Comparator<Set<Serializable>> {
        public int compare(Set<Serializable> o1, Set<Serializable> o2) {
            return o2.size() - o1.size();
        }
    }
    public EditDistanceClusterer(int threshold){
        mJoiner = new EditDistanceJoiner(threshold);
    }
    public void populate(String s){
        mJoiner.populate(s);
    }
    public List<Set<Serializable>> getClusters() {
        Map<Serializable, Set<Serializable>> clusterMap = new HashMap<>();
        ArrayList<EditDistanceJoinResult> results = mJoiner.getJoinResults();
        for (EditDistanceJoinResult item : results) {
            String a = item.src;
            String b = item.dst;
            if (a.equals(b)) continue;
            if (clusterMap.containsKey(a) && clusterMap.get(a).contains(b)) continue;
            if (clusterMap.containsKey(b) && clusterMap.get(b).contains(a)) continue;
            Set<Serializable> l1 = null;
            if (!clusterMap.containsKey(a)) {
                l1 = new TreeSet<>();
                l1.add(a);
                clusterMap.put(a, l1);
            } else {
                l1 = clusterMap.get(a);
            }
            l1.add(b);
            Set<Serializable> l2 = null;
            if (!clusterMap.containsKey(b)) {
                l2 = new TreeSet<>();
                l2.add(b);
                clusterMap.put(b, l2);
            } else {
                l2 = clusterMap.get(b);
            }
            l2.add(a);
        }
        Set<Set<Serializable>> clusters = new HashSet<>();
        for (Entry<Serializable, Set<Serializable>> e : clusterMap.entrySet()) {
            Set<Serializable> v = e.getValue();
            if (v.size() > 1) {
                clusters.add(v);
            }
        }
        List<Set<Serializable>> sortedClusters = new ArrayList<>(clusters);

        sortedClusters.sort(new SizeComparator());

        return sortedClusters;
    }
}
