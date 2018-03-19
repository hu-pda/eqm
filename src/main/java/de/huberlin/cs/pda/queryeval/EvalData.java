package de.huberlin.cs.pda.queryeval;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EvalData {

    private String queryName;
    private int truePositives;
    private List<String> falsePositivesList;
    private List<String> falseNegativesList;

    public EvalData(String queryName){
        this.queryName = queryName;
        falsePositivesList = new ArrayList<>();
        falseNegativesList = new ArrayList<>();
    }

    public String getQueryName() {
        return queryName;
    }

    public void addFalsePositive(String match){
        falsePositivesList.add(match);
    }

    public void addFalseNegative(String match){
        falseNegativesList.add(match);
    }

    public void addTruePositive(){
        truePositives++;
    }


    public int getTruePositives() {
        return truePositives;
    }

    public int getFalsePositives() {
        return falsePositivesList.size();
    }

    public int getFalseNegatives() {
        return falseNegativesList.size();
    }

    /**
     * Recall: tp / (tp+fn)
     * Percentage of actual matches we find.
     * @return
     */
    public double getRecall(){
        return truePositives / (1.0*(truePositives + falseNegativesList.size()));
    }

    /**
     * Precision: tp / (tp+fp)
     * Percentage of our found matches that are actual matches.
     * @return
     */
    public double getPrecision(){
        return truePositives / (1.0*(truePositives + falsePositivesList.size()));
    }


    /**
     * F1 Score: 2 / (1/recall + 1/precision)
     */
    public double getF1score(){
        return 2 / (1.0*(1/getRecall() + 1/getPrecision()));
    }


    /**
     * Gives a list of all matches the evaluated query found, that were not contained in the base truth query matches (False Positives)
     * @return
     */
    public List<String> listFalsePositives(){
        return falsePositivesList;
    }


    /**
     * Gives a list of all base truth query matches that the evaluated query did not match (False Negatives).
     * @return
     */
    public List<String> listFalseNegatives(){
        return falseNegativesList;
    }


    public String getResults(){
        StringBuilder strBld = new StringBuilder();

        strBld.append("Precision: ");
        strBld.append(getPrecision());
        strBld.append("\nRecall: ");
        strBld.append(getRecall());
        strBld.append("\nF1 Score: ");
        strBld.append(getF1score());
        strBld.append("\n\nTrue Positives: ");
        strBld.append(truePositives);
        strBld.append("\nFalse Positives: ");
        strBld.append(getFalsePositives());
        strBld.append("\nFalse Negatives: ");
        strBld.append(getFalseNegatives());

        return strBld.toString();
    }

    public String getResultsForFile(){
        StringBuilder strBld = new StringBuilder();

        strBld.append(queryName);
        strBld.append("\t");
        strBld.append(truePositives);
        strBld.append("\t");
        strBld.append(getFalsePositives());
        strBld.append("\t");
        strBld.append(getFalseNegatives());
        strBld.append("\t");
        strBld.append(getPrecision());
        strBld.append("\t");
        strBld.append(getRecall());
        strBld.append("\t");
        strBld.append(getF1score());
        strBld.append("\n");

        return strBld.toString();
    }

    public void writeResultFiles(String directory) throws IOException{
        Path path = Paths.get(directory + "/results-" + queryName + ".dat");
        byte[] strToBytes = getResults().getBytes();
        Files.write(path, strToBytes);

        FileWriter fpWriter = new FileWriter(directory + "/false-positives/fp-" + queryName + ".dat");
        for(String str: falsePositivesList) {
            fpWriter.write(str + "\n\n");
        }
        fpWriter.close();

        FileWriter fnWriter = new FileWriter(directory + "/false-negatives/fn-" + queryName + ".dat");
        for(String str: falseNegativesList) {
            fnWriter.write(str + "\n\n");
        }
        fnWriter.close();
    }

}
