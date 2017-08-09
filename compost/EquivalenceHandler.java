package in.edu.ashoka.surf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.muse.util.UnionFindBox;
import edu.stanford.muse.util.Util;

public class EquivalenceHandler {
    Map<String, UnionFindBox<String>> stringToBox = new LinkedHashMap<>();

    public EquivalenceHandler(String equivalenceFile) {
        List<String> lines = new ArrayList<>();
        try { lines = Util.getLinesFromFile(equivalenceFile, true); }
        catch (Exception e) { Util.print_exception(e); }

        int lineNum = 0;
        for (String line: lines) {
            lineNum++;
            String[] equivs = line.split("=");
            Arrays.sort(equivs); // sort alphabetically
            if (equivs.length != 2) {
                SurfExcel.warn("Bad input at line# " + lineNum + "in " + equivalenceFile + ": " + line);
                continue;
            }

            String left = equivs[0], right = equivs[1];
            UnionFindBox<String> leftBox = stringToBox.get(left);
            if (leftBox == null) {
                leftBox = new UnionFindBox<String>(left);
                stringToBox.put(left, leftBox);
            }
            UnionFindBox<String> rightBox = stringToBox.get(right);
            if (rightBox == null) {
                rightBox = new UnionFindBox<String>(right);
                stringToBox.put(right, rightBox);
            }
            leftBox.unify(rightBox);
        }

        UnionFindBox.assignClassNumbers(stringToBox.values());
    }

    public String getClassNum(String s) {
        UnionFindBox<String> ufo = stringToBox.get(s);
        if (ufo == null)
            return "";
        return Integer.toString(ufo.classNum);
    }
}

