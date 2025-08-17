package edu.polyu.report;

import edu.polyu.util.Utility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for CodeNaviReport class
 * Tests parsing with valid and invalid XML files
 */
public class CodeNaviReportTest {

    private File tempReportFile;
    private String tempReportPath;

    @Before
    public void setUp() throws IOException {
        // Initialize global data structures
        Utility.file2row = new HashMap<>();
        Utility.file2bugs = new HashMap<>();
        Utility.file2report = new HashMap<>();
        Utility.failedReportPaths = new ArrayList<>();
        
        // Create temporary report file
        tempReportFile = File.createTempFile("codenavi_test", ".xml");
        tempReportPath = tempReportFile.getAbsolutePath();
    }

    @After
    public void tearDown() {
        // Clean up temporary file
        if (tempReportFile != null && tempReportFile.exists()) {
            tempReportFile.delete();
        }
        
        // Clear global data structures
        Utility.file2row.clear();
        Utility.file2bugs.clear();
        Utility.file2report.clear();
        Utility.failedReportPaths.clear();
    }

    @Test
    public void testValidXmlReport() throws IOException {
        // Create valid XML report
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>2</reportLine>\n" +
                "    <fileName>/absolute/path/to/test/file.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>5</reportLine>\n" +
                "    <fileName>/absolute/path/to/test/file.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, validXml);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify results
        assertTrue(Utility.file2row.containsKey("/absolute/path/to/test/file.java"));
        List<Integer> lines = Utility.file2row.get("/absolute/path/to/test/file.java");
        assertEquals(2, lines.size());
        assertTrue(lines.contains(2));
        assertTrue(lines.contains(5));
        
        assertTrue(Utility.file2bugs.containsKey("/absolute/path/to/test/file.java"));
        HashMap<String, List<Integer>> bugs = Utility.file2bugs.get("/absolute/path/to/test/file.java");
        assertTrue(bugs.containsKey("CODENAVI_DEFECT"));
        assertEquals(2, bugs.get("CODENAVI_DEFECT").size());
        
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testEmptyXmlReport() throws IOException {
        // Create empty XML report
        String emptyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, emptyXml);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results were added
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testMissingErrorsElement() throws IOException {
        // Create XML without errors element
        String xmlWithoutErrors = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                "</r>";
        
        writeToFile(tempReportFile, xmlWithoutErrors);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results were added and no failures recorded
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testMalformedXml() throws IOException {
        // Create malformed XML
        String malformedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>2</reportLine>\n" +
                "    <fileName>/path/to/file.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n"; // Missing closing tag
        
        writeToFile(tempReportFile, malformedXml);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify failure was recorded
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.contains(tempReportPath));
    }

    @Test
    public void testNonExistentFile() {
        String nonExistentPath = "/non/existent/file.xml";
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", nonExistentPath);
        
        // Verify no results and no failures (method returns early for non-existent files)
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testEmptyFile() throws IOException {
        // Create empty file
        writeToFile(tempReportFile, "");
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results (method returns early for empty files)
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testMissingDefectInfo() throws IOException {
        // Create XML with error but no defectInfo
        String xmlMissingDefectInfo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, xmlMissingDefectInfo);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results were added (error is skipped)
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testMissingFileName() throws IOException {
        // Create XML with defectInfo but no fileName
        String xmlMissingFileName = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>2</reportLine>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, xmlMissingFileName);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results were added (error is skipped)
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testEmptyFileName() throws IOException {
        // Create XML with empty fileName
        String xmlEmptyFileName = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>2</reportLine>\n" +
                "    <fileName></fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, xmlEmptyFileName);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify no results were added (error is skipped)
        assertTrue(Utility.file2row.isEmpty());
        assertTrue(Utility.file2bugs.isEmpty());
        assertTrue(Utility.failedReportPaths.isEmpty());
    }

    @Test
    public void testSingleFileReport() throws IOException {
        // Test readSingleCodeNaviResultFile method
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>3</reportLine>\n" +
                "    <fileName>/absolute/path/to/test/file.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, validXml);
        
        File seedFile = new File("/absolute/path/to/test/file.java");
        CodeNaviReport.readSingleCodeNaviResultFile(seedFile, tempReportPath);
        
        // Verify results
        String seedPath = seedFile.getAbsolutePath();
        assertTrue(Utility.file2row.containsKey(seedPath));
        assertTrue(Utility.file2bugs.containsKey(seedPath));
        assertTrue(Utility.file2report.containsKey(seedPath));
        
        List<Integer> lines = Utility.file2row.get(seedPath);
        assertEquals(1, lines.size());
        assertTrue(lines.contains(3));
        
        Report report = Utility.file2report.get(seedPath);
        assertNotNull(report);
        assertTrue(report instanceof CodeNaviReport);
        assertEquals(1, report.getViolations().size());
    }

    @Test
    public void testMultipleFilesInReport() throws IOException {
        // Create XML with multiple files
        String multiFileXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                " <errors>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>2</reportLine>\n" +
                "    <fileName>/path/to/file1.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                "  <error>\n" +
                "   <defectInfo>\n" +
                "    <reportLine>10</reportLine>\n" +
                "    <fileName>/path/to/file2.java</fileName>\n" +
                "   </defectInfo>\n" +
                "  </error>\n" +
                " </errors>\n" +
                "</r>";
        
        writeToFile(tempReportFile, multiFileXml);
        
        CodeNaviReport.readCodeNaviResultFile("/test/seed/path", tempReportPath);
        
        // Verify both files are processed
        assertTrue(Utility.file2row.containsKey("/path/to/file1.java"));
        assertTrue(Utility.file2row.containsKey("/path/to/file2.java"));
        
        assertEquals(1, Utility.file2row.get("/path/to/file1.java").size());
        assertEquals(1, Utility.file2row.get("/path/to/file2.java").size());
        
        assertTrue(Utility.file2row.get("/path/to/file1.java").contains(2));
        assertTrue(Utility.file2row.get("/path/to/file2.java").contains(10));
    }

    private void writeToFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}