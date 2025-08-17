package edu.polyu;

import edu.polyu.util.Schedule;
import edu.polyu.util.Utility;

/**
 * Main entry point for Statfier application
 * Initializes the environment and executes the appropriate static analyzer
 */
public class Main {
    
    public static void main(String[] args) {
        // Initialize the environment (Requirement 3.1)
        Utility.initEnv();
        
        // Get the Schedule instance
        Schedule schedule = Schedule.getInstance();
        
        // Check which analyzer is enabled and execute accordingly (Requirement 4.1)
        if (Utility.PMD_MUTATION) {
            System.out.println("Starting PMD analysis...");
            schedule.executePMDTransform(Utility.SEED_PATH);
        } else if (Utility.SPOTBUGS_MUTATION) {
            System.out.println("Starting SpotBugs analysis...");
            schedule.executeSpotBugsTransform(Utility.SEED_PATH);
        } else if (Utility.CHECKSTYLE_MUTATION) {
            System.out.println("Starting CheckStyle analysis...");
            schedule.executeCheckStyleTransform(Utility.SEED_PATH);
        } else if (Utility.INFER_MUTATION) {
            System.out.println("Starting Infer analysis...");
            schedule.executeInferTransform(Utility.SEED_PATH);
        } else if (Utility.SONARQUBE_MUTATION) {
            System.out.println("Starting SonarQube analysis...");
            schedule.executeSonarQubeTransform(Utility.SEED_PATH);
        } else if (Utility.CODENAVI_MUTATION) {
            System.out.println("Starting CodeNavi analysis...");
            schedule.executeCodeNaviTransform(Utility.SEED_PATH);
        } else {
            System.err.println("No static analyzer is enabled. Please set one of the *_MUTATION flags to true in config.properties");
            System.exit(-1);
        }
        
        System.out.println("Analysis completed successfully.");
    }
}