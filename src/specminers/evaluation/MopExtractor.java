/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specminers.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import specminers.StringHelper;

/**
 *
 * @author Otmar
 */
public class MopExtractor {
    File mopSpecFile;
    private String extendedRegularExpression;
    private static final String ERE_PATTERN = "^ere[\\s\\t]+\\:[\\s\\t]+(.+)$";
    
    public MopExtractor(String mopFilePath){
        this(new File(mopFilePath));
    }
    
    
    public MopExtractor(File mopFile){
        this.mopSpecFile = mopFile;
    }
    
    private String getMatchingLine() throws IOException {
        return getLinesInFile()
                .stream()
                .filter(l -> l.matches(ERE_PATTERN))
                .findFirst()
                .orElse(null);
    }
    
    private List<String> getLinesInFile() throws IOException {
        return FileUtils.readLines(mopSpecFile)
                .stream()
                .map(l -> l.trim())
                .collect(Collectors.toList());
    }
        
    public boolean containsExtendedRegularExpression() throws IOException {
        return getMatchingLine() != null;
    }
    
    public String getExtendedRegularExpression() throws IOException {
        if (!containsExtendedRegularExpression())
            return null;
        else {
            if (StringUtils.isBlank(extendedRegularExpression)){
                String matchingLine = getMatchingLine(); 
                extendedRegularExpression =  StringHelper.extractSingleValueWithRegex(matchingLine, ERE_PATTERN, 1);
            }
            
            return extendedRegularExpression;
            
        }
    }
    
    
}
