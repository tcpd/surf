package in.edu.ashoka.surf.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Counter {
    public static void main (String args[]) {
        long N = 10L * 1000 * 1000;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < N; i++)
            list.add ((int) (Math.random() * N));

        long start = System.currentTimeMillis();
        List<Integer> q = list.parallelStream().filter(x -> Math.log(x) > Math.log(N/2)).collect(Collectors.toList());
        System.out.println (q.size() + " in " + (System.currentTimeMillis() - start) + "ms");
    }
}
