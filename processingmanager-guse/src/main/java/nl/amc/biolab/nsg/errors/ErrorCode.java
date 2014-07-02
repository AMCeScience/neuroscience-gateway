/*
 * Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.


 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.amc.biolab.nsg.errors;

import java.util.HashMap;

/**
 * An error has two parts: the problem description and when it happened.
 * Each part is implemented using an enum that simplifies the implementation
 * of other error handling codes.
 * An integer encoding the error code can be made using the {@link 
 * #getIntValue() getIntValue()} and inversely an integer code can be
 * turned into an instance of this class using the static method {@link 
 * #decode(int) decode}.
 * For decoding of the integer encodings, internally two hashmaps 
 * are employed in order to enable reverse look-up on the enums.
 * 
 * @author m.jaghouri@amc.uva.nl
 */
public class ErrorCode {
/*
    public enum Problem { 
        None (0),
        In_XNAT_Download (1),
        In_Grid_Upload (2),
        In_XNAT_Upload (3),
        In_Grid_Download (4),
        In_File_Transfer (5),
        In_HTTP_Download (6),
        In_HTTP_Upload (7),
        In_SCP_Download (8),
        In_SCP_Upload (9),
        In_Workflow_Execution (20),
        In_CommandLine_Invocation (51),
        In_Catalog_Configuration (52),
        In_ASM_Call (53),
        Unknown (100);
        int code;
        private Problem(int code) { 
            this.code = code;
        }
        @Override public String toString() {
            return "Problem "+super.toString().replace("_", " ");
        }
    }
    */
    private static final HashMap<Integer, Problem> problemMap = new HashMap();
    private static final HashMap<Integer, During> occasionMap = new HashMap();
    
    /**
     * Describes the problem that has occurred.
     */
    public enum Problem { 
        None ("No error", 0),
        In_XNAT_Download ("Download from xnat failed.", 1),
        In_Grid_Upload ("Upload to grid failed.", 2),
        In_XNAT_Upload ("Upload to xnat failed.", 3),
        In_Grid_Download ("Download from grid failed.", 4),
        In_File_Transfer ("File transfer failed.", 5),
        In_HTTP_Download ("Http download failed.", 6),
        In_HTTP_Upload ("Http upload failed.", 7),
        In_SCP_Download ("Scp download failed.", 8),
        In_SCP_Upload ("Scp upload failed.", 9),
        In_Workflow_Execution ("Error in workflow execution.", 20),
        In_CommandLine_Invocation ("Command line invocation failed.", 51),
        In_Catalog_Configuration ("Inconsistent info in catalog or internal error.", 52),
        In_ASM_Call ("Error in ASM call.", 53),
        Unknown ("Unknown error.", 100);
        String description;
        int code;
        private Problem(String description, int code) { 
            this.description = description; 
            this.code = code;
            problemMap.put(code, this);
        }
        public String getDescription() {return description; }
    }
    
    /**
     * During which phase the problem has occurred.
     */
    public enum During {
        None ("an unknown time", 0),
        Submission_Time ("submission time", 1<<9),
        Execution_Time ("execution time", 1<<10),
        Upload_Time ("upload time", 1<<11);  
        String description;
        int code;
        private During(String description, int code) { 
            this.description = description; 
            this.code = code;
            occasionMap.put(code, this);
        }
        public String getDescription() { return description; }
    }

    Problem problem;
    During occasion;

    /**
     * creates an instance with the given parameters.
     * @param problem The problem description
     * @param occasion The phase during which the problem occurred
     */
    public ErrorCode(Problem problem, During occasion) {
        if (problem == null) {
            this.problem = Problem.None;
        } else {
            this.problem = problem;
        }
        if (occasion == null) {
            this.occasion = During.None;
        } else {
            this.occasion = occasion;
        }
    }
    
    /**
     * Reconstructs an ErrorCode object form an integer coding of the error.
     * 
     * @param code An integer coding of the error that should be typically created 
     * previously with the {@link #getIntValue() getIntValue()} method.
     * @return An ErrorCode object that has the same Problem and During values as 
     * the original error
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static ErrorCode decode(int code) {
        new ErrorCode(null, null);  // this line is needed to initialize the enum hashmaps
        final int pCode = code&0xFF;
        final int oCode = code&0xFFFFFF00;
        System.out.println("pCode="+pCode+problemMap.get(code&0xFF)+"; oCode="+oCode);
        return new ErrorCode(problemMap.get(pCode), occasionMap.get(oCode));
    }
    
    /**
     * Returns an integer encoding of the error such that can be stored in a DB.
     * This integer an be decoded later using {@link #decode(int) decode} method.
     * @return An integer encoding of the error.
     */
    public int getIntValue () {
        final int combinedCode = problem.code | occasion.code;
        System.out.println("Problem code "+problem.code+"; Occasion code: "+occasion.code+"; Combined code: "+combinedCode);
        return combinedCode;
    }

    public Problem getProblem() {
        return problem;
    }

    /**
     * Resets the problem description and returns the ErrorCode instance, 
     * enabling chaining of actions on ErrorCode instance.
     * @param problem New problem description
     * @return The ErrorCode instance with the new problem description
     */
    public ErrorCode setProblem(Problem problem) {
        this.problem = problem;
        return this;
    }
    
    /**
     * Resets the occasion during which the problem occurred. This is useful
     * for example if the method in which the problem occurs does not have 
     * enough information about the phase during which the problem occurred.
     * This method returns an instance of ErrorCode enabling chaining of actions.
     * @param during The phase during which the error has occurred
     * @return The ErrorCode instance with the new phase during which the problem occurred
     */
    public ErrorCode setOccasion(During during) {
        occasion = during;
        return this;
    }

    public During getOccasion() {
        return occasion;
    }
}
