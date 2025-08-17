package edu.polyu.report;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static edu.polyu.util.Utility.DEBUG;
import static edu.polyu.util.Utility.file2bugs;
import static edu.polyu.util.Utility.file2report;
import static edu.polyu.util.Utility.file2row;
import static edu.polyu.util.Utility.failedReportPaths;

/**
 * Description: CodeNavi report processing class that extends the base Report class
 * Author: Kiro
 * Date: 2025/7/24
 */
public class CodeNaviReport extends Report {

    public CodeNaviReport(String filePath) {
        super(filePath);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("CodeNavi Report: " + this.filePath + "\n");
        for(int i = 0; i < violations.size(); i++) {
            str.append(violations.get(i) + "\n");
        }
        return str.toString();
    }

    /**
     * Parse CodeNavi XML reports and integrate results into global data structures
     * Variable seedFolderPath contains sub seed folder name
     * seedFolderPath is the absolute path
     * @param seedFolderPath The absolute path to the seed folder
     * @param reportPath The path to the CodeNavi XML report file
     */
    public static void readCodeNaviResultFile(String seedFolderPath, String reportPath) {
        if (DEBUG) {
            System.out.println("CodeNavi Detection Result FileName: " + reportPath);
        }
        File reportFile = new File(reportPath);
        if(!reportFile.exists() || reportFile.length() == 0) {
            return;
        }
        
        HashMap<String, Report> path2report = new HashMap<>();
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(reportFile);
            Element root = document.getRootElement();
            
            // Navigate to errors element
            Element errorsElement = root.element("errors");
            if (errorsElement == null) {
                if (DEBUG) {
                    System.out.println("No errors element found in CodeNavi report: " + reportPath);
                }
                return;
            }
            
            List<Element> errorElements = errorsElement.elements("error");
            for (Element errorElement : errorElements) {
                Element defectInfo = errorElement.element("defectInfo");
                if (defectInfo == null) {
                    continue;
                }
                
                Element fileNameElement = defectInfo.element("fileName");
                if (fileNameElement == null) {
                    continue;
                }
                
                String fileName = fileNameElement.getText();
                if (fileName == null || fileName.trim().isEmpty()) {
                    continue;
                }
                
                // Create violation with generic bug type since CodeNavi doesn't specify bug types in the XML
                Violation violation = new CodeNaviViolation(defectInfo, "CODENAVI_DEFECT");
                
                // Use the absolute file path from the XML
                String filePath = fileName;
                
                if (path2report.containsKey(filePath)) {
                    path2report.get(filePath).addViolation(violation);
                } else {
                    Report report = new CodeNaviReport(filePath);
                    report.addViolation(violation);
                    path2report.put(filePath, report);
                }
            }
        } catch (DocumentException e) {
            if (DEBUG) {
                System.out.println("Failed to parse CodeNavi report: " + reportPath);
                e.printStackTrace();
            }
            failedReportPaths.add(reportPath);
            return;
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println("Unexpected error parsing CodeNavi report: " + reportPath);
                e.printStackTrace();
            }
            failedReportPaths.add(reportPath);
            return;
        }
        
        // Integrate parsed results into global data structures
        for (Report report : path2report.values()) {
            if (!file2row.containsKey(report.getFilePath())) {
                file2row.put(report.getFilePath(), new ArrayList<>());
                file2bugs.put(report.getFilePath(), new HashMap<>());
            }
            for (Violation violation : report.getViolations()) {
                file2row.get(report.getFilePath()).add(violation.getBeginLine());
                HashMap<String, List<Integer>> bug2cnt = file2bugs.get(report.getFilePath());
                if (!bug2cnt.containsKey(violation.getBugType())) {
                    bug2cnt.put(violation.getBugType(), new ArrayList<>());
                }
                bug2cnt.get(violation.getBugType()).add(violation.getBeginLine());
            }
        }
    }

    /**
     * Parse a single CodeNavi XML report file for a specific seed file
     * @param seedFile The seed file being analyzed
     * @param reportPath The path to the CodeNavi XML report file
     */
    public static void readSingleCodeNaviResultFile(File seedFile, String reportPath) {
        if (DEBUG) {
            System.out.println("CodeNavi Detection Result FileName: " + reportPath);
        }
        File reportFile = new File(reportPath);
        if(!reportFile.exists() || reportFile.length() == 0) {
            return;
        }
        
        String seedPath = seedFile.getAbsolutePath();
        String seedFolderPath = seedFile.getParent();
        
        if (file2report.containsKey(seedPath)) {
            System.out.println("Repeat process: " + seedPath);
            System.out.println("Report Path: " + reportPath);
            System.exit(-1);
        }
        
        file2row.put(seedPath, new ArrayList<>());
        file2bugs.put(seedPath, new HashMap<>());
        Report report = new CodeNaviReport(seedPath);
        file2report.put(seedPath, report);
        
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(reportFile);
            Element root = document.getRootElement();
            
            // Navigate to errors element
            Element errorsElement = root.element("errors");
            if (errorsElement == null) {
                if (DEBUG) {
                    System.out.println("No errors element found in CodeNavi report: " + reportPath);
                }
                return;
            }
            
            List<Element> errorElements = errorsElement.elements("error");
            for (Element errorElement : errorElements) {
                Element defectInfo = errorElement.element("defectInfo");
                if (defectInfo == null) {
                    continue;
                }
                
                Element fileNameElement = defectInfo.element("fileName");
                if (fileNameElement == null) {
                    continue;
                }
                
                String fileName = fileNameElement.getText();
                if (fileName == null || fileName.trim().isEmpty()) {
                    continue;
                }
                
                // Create violation with generic bug type since CodeNavi doesn't specify bug types in the XML
                Violation violation = new CodeNaviViolation(defectInfo, "CODENAVI_DEFECT");
                
                // Verify that the file path matches the seed file being processed
                String filePath = fileName;
                if(!filePath.equals(seedPath)) {
                    if (DEBUG) {
                        System.out.println("Seed Path: " + seedPath);
                        System.out.println("File Path: " + filePath);
                        System.out.println();
                    }
                }
                report.addViolation(violation);
            }
        } catch (DocumentException e) {
            if (DEBUG) {
                System.out.println("Failed to parse CodeNavi report: " + reportPath);
                e.printStackTrace();
            }
            failedReportPaths.add(reportPath);
            return;
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println("Unexpected error parsing CodeNavi report: " + reportPath);
                e.printStackTrace();
            }
            failedReportPaths.add(reportPath);
            return;
        }
        
        // Integrate parsed results into global data structures
        for (Violation violation : report.getViolations()) {
            file2row.get(seedPath).add(violation.getBeginLine());
            HashMap<String, List<Integer>> bug2cnt = file2bugs.get(seedPath);
            if (!bug2cnt.containsKey(violation.getBugType())) {
                bug2cnt.put(violation.getBugType(), new ArrayList<>());
            }
            bug2cnt.get(violation.getBugType()).add(violation.getBeginLine());
        }
    }
}