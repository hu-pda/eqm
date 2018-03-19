package de.huberlin.cs.pda.queryeval.plotting;

import de.huberlin.cs.pda.queryeval.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

public class Plotter {

    private final Logger logger = LoggerFactory.getLogger(Plotter.class);
    File plotScriptsDir;
    String targetDir;
    String[] queryNames;

    public Plotter(File plotScriptsDir, String basePath, String[] queryNames){
        this.plotScriptsDir = plotScriptsDir;
        this.targetDir = basePath;
        this.queryNames = queryNames;
    }

    public void plot(){
        File[] scripts = plotScriptsDir.listFiles();
        for(File script : scripts){
            try{
                logger.info(script.getAbsolutePath());
                String[] command = {"gnuplot",
                        "-e",
                        "resultfile='" + targetDir + "/results/results.dat" +   "'; " +
                                "outputfile='" + targetDir + "/plots/" + script.getName().replace(".gp", "") + ".eps';" +
                                "queries=" + queryNames.length + "; querynames='" + String.join(" ", queryNames) + "';",
                        script.getName()};
                logger.info(Arrays.toString(command));
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(plotScriptsDir);
                Process process = pb.start();


                InputStream is = process.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) {
                    logger.warn(line);
                }
            }catch(IOException e){
                logger.error(e.getMessage());
            }
        }
    }
}
