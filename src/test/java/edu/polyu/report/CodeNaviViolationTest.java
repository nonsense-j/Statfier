package edu.polyu.report;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CodeNaviViolation class
 * Tests XML parsing with various input formats
 */
public class CodeNaviViolationTest {

    @Test
    public void testValidXmlParsing() throws DocumentException {
        // Create a valid XML element
        String xmlString = "<defectInfo><reportLine>5</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "TEST_BUG");
        
        assertEquals("TEST_BUG", violation.getBugType());
        assertEquals(5, violation.getBeginLine());
        assertEquals("[CodeNavi] TEST_BUG at line 5", violation.toString());
    }

    @Test
    public void testMissingReportLineElement() throws DocumentException {
        // Create XML element without reportLine
        String xmlString = "<defectInfo><fileName>/path/to/file.java</fileName></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "TEST_BUG");
        
        assertEquals("TEST_BUG", violation.getBugType());
        assertEquals(-1, violation.getBeginLine());
        assertEquals("[CodeNavi] TEST_BUG at line -1", violation.toString());
    }

    @Test
    public void testInvalidLineNumber() throws DocumentException {
        // Create XML element with invalid line number
        String xmlString = "<defectInfo><reportLine>invalid</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "TEST_BUG");
        
        assertEquals("TEST_BUG", violation.getBugType());
        assertEquals(-1, violation.getBeginLine());
    }

    @Test
    public void testEmptyLineNumber() throws DocumentException {
        // Create XML element with empty line number
        String xmlString = "<defectInfo><reportLine></reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "TEST_BUG");
        
        assertEquals("TEST_BUG", violation.getBugType());
        assertEquals(-1, violation.getBeginLine());
    }

    @Test
    public void testWhitespaceLineNumber() throws DocumentException {
        // Create XML element with whitespace line number
        String xmlString = "<defectInfo><reportLine>  10  </reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "TEST_BUG");
        
        assertEquals("TEST_BUG", violation.getBugType());
        assertEquals(10, violation.getBeginLine());
    }

    @Test
    public void testNullBugType() throws DocumentException {
        // Test with null bug type
        String xmlString = "<defectInfo><reportLine>3</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, null);
        
        assertEquals("UNKNOWN", violation.getBugType());
        assertEquals(3, violation.getBeginLine());
        assertEquals("[CodeNavi] UNKNOWN at line 3", violation.toString());
    }

    @Test
    public void testZeroLineNumber() throws DocumentException {
        // Test with zero line number
        String xmlString = "<defectInfo><reportLine>0</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "ZERO_LINE_BUG");
        
        assertEquals("ZERO_LINE_BUG", violation.getBugType());
        assertEquals(0, violation.getBeginLine());
    }

    @Test
    public void testNegativeLineNumber() throws DocumentException {
        // Test with negative line number
        String xmlString = "<defectInfo><reportLine>-5</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "NEGATIVE_LINE_BUG");
        
        assertEquals("NEGATIVE_LINE_BUG", violation.getBugType());
        assertEquals(-5, violation.getBeginLine());
    }

    @Test
    public void testLargeLineNumber() throws DocumentException {
        // Test with large line number
        String xmlString = "<defectInfo><reportLine>999999</reportLine></defectInfo>";
        Document document = DocumentHelper.parseText(xmlString);
        Element defectInfo = document.getRootElement();
        
        CodeNaviViolation violation = new CodeNaviViolation(defectInfo, "LARGE_LINE_BUG");
        
        assertEquals("LARGE_LINE_BUG", violation.getBugType());
        assertEquals(999999, violation.getBeginLine());
    }
}