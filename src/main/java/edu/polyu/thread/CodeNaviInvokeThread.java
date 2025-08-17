package edu.polyu.thread;

import edu.polyu.util.OSUtil;
import edu.polyu.util.Invoker;
import edu.polyu.util.Utility;

import static edu.polyu.util.Utility.DEBUG;
import static edu.polyu.util.Utility.CODENAVI_PATH;
import static edu.polyu.util.Utility.CODENAVI_CHECKER_DIR;

import java.io.File;

public class CodeNaviInvokeThread implements Runnable {

    private String seedFolderPath;
    private String seedFolderName;
    private String reportOutputPath;

    public CodeNaviInvokeThread(String seedFolderPath, String seedFolderName, String reportOutputPath) {
        this.seedFolderPath = seedFolderPath;
        this.seedFolderName = seedFolderName;
        this.reportOutputPath = reportOutputPath;
    }

    @Override
    public void run() {
        if(DEBUG) {
            System.out.println("CodeNavi Seed path: " + seedFolderPath + File.separator + seedFolderName);
            System.out.println("CodeNavi Report output path: " + reportOutputPath);
        }
        
        String[] invokeCommands = new String[3];
        if(OSUtil.isWindows()) {
            invokeCommands[0] = "cmd.exe";
            invokeCommands[1] = "/c";
        } else {
            invokeCommands[0] = "/bin/bash";
            invokeCommands[1] = "-c";
        }
        
        // get checker dsl dir, test_dir and 

        // Construct CodeNavi command according to requirements 2.1-2.7
        invokeCommands[2] = "java -Dfile.encoding=UTF-8 --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --enable-preview"
                + " -cp " + CODENAVI_PATH
                + " com.huawei.secbrella.kirin.Main"
                + " --pugin --language java --outputFormat xml"
                + " --dir " + this.seedFolderPath
                + " --checkerDir " + CODENAVI_CHECKER_DIR
                + " --output " + this.reportOutputPath;
        
        // Execute CodeNavi command and handle failures (Requirement 6.1)
        boolean hasExec = Invoker.invokeCommandsByZT(invokeCommands);
        if (!hasExec) {
            // Add failed command to failedToolExecution list (Requirement 6.1)
            Utility.failedToolExecution.add(invokeCommands[2]);
            if (DEBUG) {
                System.out.println("CodeNavi execution failed for: " + invokeCommands[2]);
            }
        }
    }
}