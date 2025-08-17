package edu.polyu.report;

import org.dom4j.Element;

/**
 * Description: CodeNavi-specific violation class that extends the base Violation class
 * Author: Kiro
 * Date: 2025/7/24
 */
public class CodeNaviViolation extends Violation {

    /**
     * Constructor that parses XML elements to extract line numbers and bug types
     * @param xmlElement The XML element containing defect information
     * @param bugType The bug type for this violation
     */
    public CodeNaviViolation(Element xmlElement, String bugType) {
        // Extract line number from reportLine element
        Element reportLineElement = xmlElement.element("reportLine");
        if (reportLineElement != null) {
            try {
                this.beginLine = Integer.parseInt(reportLineElement.getText().trim());
            } catch (NumberFormatException e) {
                this.beginLine = -1; // Default value if parsing fails
            }
        } else {
            this.beginLine = -1; // Default value if element is missing
        }
        
        // Set the bug type
        this.bugType = bugType != null ? bugType : "UNKNOWN";
    }

    /**
     * Returns the bug type for this violation
     * @return The bug type string
     */
    @Override
    public String getBugType() {
        return this.bugType;
    }

    /**
     * Returns the line number where the violation begins
     * @return The line number
     */
    @Override
    public int getBeginLine() {
        return this.beginLine;
    }

    /**
     * String representation for debugging and logging
     * @return String representation of the violation
     */
    @Override
    public String toString() {
        return "[CodeNavi] " + this.bugType + " at line " + this.beginLine;
    }
}