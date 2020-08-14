package in.edu.ashoka.surf.test;

import edu.tsinghua.dbgroup.EditDistanceClusterer;
import in.edu.ashoka.surf.Dataset;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hangal on 8/12/17.
 */
class Test1 {

    private static final String path = "/Users/priyamgarrg21/Documents/Aditya/Internship@Ashoka/TCPD_GE_Delhi_2020-6-18.csv";
    public static void main(String args[]) throws IOException {
        Dataset d = Dataset.getDataset(path);
        Set<String> names = d.getRows().stream().map (r -> r.get("Candidate")).collect (Collectors.toSet());

        EditDistanceClusterer edc = new EditDistanceClusterer(5);
        names.forEach (edc::populate);
        List<Set<String>> clusters = (List) edc.getClusters();

        int i = 0;
        for (Set<String> cluster: clusters) {
            System.out.println ("Cluster " + i++ + " -------");
            for (String s: cluster)
                System.out.println (s);
        }
    }
}
