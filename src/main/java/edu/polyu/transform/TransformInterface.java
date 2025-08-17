package edu.polyu.transform;

import static edu.polyu.util.Utility.reg_sep;

import edu.polyu.analysis.TypeWrapper;
import edu.polyu.util.Utility;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for applying specific transforms to Java files and generating mutants
 */
public class TransformInterface {
    
    /**
     * Apply applicable transforms to a Java file and generate mutants
     * 
     * @param filePath Path to the Java file to transform
     * @return List of file paths to generated mutants
     */
    public static List<String> applyApplicableTransforms(String filePath) {
        List<String> mutantPaths = new ArrayList<>();
        
        try {
            // Initialize environment
            Utility.initEnv();

            String[] tokens = filePath.split(reg_sep);
            String folderName = tokens[tokens.length - 2];
            TypeWrapper wrapper = new TypeWrapper(filePath, folderName);
            
            int globalIterCount = 1;
            
            // Try each available transform
            for (Transform transform : Transform.getTransforms()) {
                try {
                    // Get all nodes that can be transformed
                    List<ASTNode> candidateNodes = transform.check(wrapper, wrapper.getCompilationUnit());
                    
                    if (candidateNodes.isEmpty()) {
                        continue; // Skip this transform if no applicable nodes
                    }
                    
                    System.out.println("Applying transform: " + transform.getIndex() + " to " + filePath);
                    
                    // Generate mutants for each candidate node
                    for (ASTNode node : candidateNodes) {
                        // Create a copy of the wrapper for this mutant
                        TypeWrapper mutantWrapper = wrapper.deepCopy();
                        
                        // Apply the transform
                        boolean success = transform.run(node, mutantWrapper, node, node);
                        
                        if (success) {
                            // Generate mutant file name
                            String mutantPath = generateMutantPath(filePath, globalIterCount);
                            
                            // Set the mutant path
                            mutantWrapper.setFilePath(mutantPath);
                            
                            // Rewrite and save the mutant
                            mutantWrapper.rewriteJavaCode();
                            if (mutantWrapper.writeToJavaFile()) {
                                mutantPaths.add(mutantPath);
                                System.out.println("Generated mutant: " + mutantPath + " using " + transform.getIndex());
                                globalIterCount++;
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error applying transform " + transform.getIndex() + ": " + e.getMessage());
                    // Continue with next transform
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return mutantPaths;
    }
    
    /**
     * Apply a specific transform to a Java file and generate mutants
     * 
     * @param filePath Path to the Java file to transform
     * @param transformName Name of the transform to apply (e.g., "LoopConversion1", "AddBrackets")
     * @return List of file paths to generated mutants
     */
    public static List<String> applyTransform(String filePath, String transformName) {
        List<String> mutantPaths = new ArrayList<>();
        
        try {
            // Initialize environment
            Utility.initEnv();
            
            // Get the transform instance
            Transform transform = Transform.name2transform.get(transformName);
            if (transform == null) {
                System.err.println("Transform not found: " + transformName);
                return mutantPaths;
            }
            
            // Create TypeWrapper from the file
            TypeWrapper wrapper = new TypeWrapper(filePath);
            
            // Get all nodes that can be transformed
            List<ASTNode> candidateNodes = transform.check(wrapper, wrapper.getCompilationUnit());
            
            if (candidateNodes.isEmpty()) {
                System.out.println("No applicable nodes found for transform: " + transformName);
                return mutantPaths;
            }
            
            // Generate mutants for each candidate node
            int iterCount = 1;
            for (ASTNode node : candidateNodes) {
                // Create a copy of the wrapper for this mutant
                TypeWrapper mutantWrapper = wrapper.deepCopy();
                
                // Apply the transform
                boolean success = transform.run(node, mutantWrapper, node, node);
                
                if (success) {
                    // Generate mutant file name
                    String mutantPath = generateMutantPath(filePath, iterCount);
                    
                    // Set the mutant path
                    mutantWrapper.setFilePath(mutantPath);
                    
                    // Rewrite and save the mutant
                    mutantWrapper.rewriteJavaCode();
                    if (mutantWrapper.writeToJavaFile()) {
                        mutantPaths.add(mutantPath);
                        System.out.println("Generated mutant: " + mutantPath);
                        iterCount++;
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error applying transform " + transformName + " to " + filePath);
            e.printStackTrace();
        }
        
        return mutantPaths;
    }
    
    /**
     * Apply multiple transforms to a Java file
     * 
     * @param filePath Path to the Java file to transform
     * @param transformNames List of transform names to apply
     * @return List of file paths to generated mutants
     */
    public static List<String> applyTransforms(String filePath, List<String> transformNames) {
        List<String> allMutantPaths = new ArrayList<>();
        
        for (String transformName : transformNames) {
            List<String> mutantPaths = applyTransform(filePath, transformName);
            allMutantPaths.addAll(mutantPaths);
        }
        
        return allMutantPaths;
    }
    
    /**
     * Get all available transform names
     * 
     * @return List of available transform names
     */
    public static List<String> getAvailableTransforms() {
        return new ArrayList<>(Transform.name2transform.keySet());
    }
    
    /**
     * Generate mutant file path with the pattern "filename_iter"
     * 
     * @param originalPath Original file path
     * @param iteration Iteration number
     * @return Generated mutant file path
     */
    private static String generateMutantPath(String originalPath, int iteration) {
        File originalFile = new File(originalPath);
        String parentDir = originalFile.getParent();
        String fileName = originalFile.getName();
        
        // Remove .java extension
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        // Create mutant name with iteration
        String mutantName = baseName + "_iter" + iteration + extension;
        
        return new File(parentDir, mutantName).getAbsolutePath();
    }
    
    /**
     * Clean up generated mutants in a directory
     * 
     * @param directoryPath Directory containing mutants
     * @param baseFileName Base file name (without extension)
     */
    public static void cleanupMutants(String directoryPath, String baseFileName) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith(baseFileName + "_iter") && fileName.endsWith(".java")) {
                    if (file.delete()) {
                        System.out.println("Deleted mutant: " + fileName);
                    }
                }
            }
        }
    }
    
    /**
     * Main method for command-line usage
     * Usage: java TransformInterface <file_path> [transform_name]
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TransformInterface <file_path> [transform_name]");
            System.err.println("  file_path: Path to the Java file to transform");
            System.err.println("  transform_name: Optional specific transform name");
            System.err.println("Available transforms:");
            for (String transform : getAvailableTransforms()) {
                System.err.println("  - " + transform);
            }
            System.exit(1);
        }
        
        String filePath = args[0];
        List<String> mutantPaths;
        
        if (args.length > 1) {
            // Apply specific transform
            String transformName = args[1];
            mutantPaths = applyTransform(filePath, transformName);
        } else {
            // Apply all applicable transforms
            mutantPaths = applyApplicableTransforms(filePath);
        }
        
        System.out.println("Generated " + mutantPaths.size() + " mutants:");
        for (String mutantPath : mutantPaths) {
            System.out.println("  " + mutantPath);
        }
    }
}