/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specminers.evaluation;

import com.google.common.base.Predicates;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.reflections.ReflectionUtils;
import static org.reflections.ReflectionUtils.withModifier;
import specminers.ExecutionArgsHelper;
import specminers.JavaFileParsingHelper;

/**
 *
 * @author otmarpereira
 */
public class PradelRefSpecsExtender {

    private final static String JFLAP_PATH_OPTION = "-j";
    private final static String SOURCE_CODE_PATH_OPTION = "-s";
    private final static String HELP_OPTION = "-h";
    private final static String OUTPUT_OPTION = "-o";
    private final static String PUBLIC_API_OUTPUT_FOLDER = "-p";

    public static void main(String[] args) throws IOException {
        // Sample execution args:  -s "E:\openjdk-6-src-b33-14_oct_2014.tar\jdk\src\share\classes\java\net" -j "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\jflap\net" -p "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\java.net public API" -o "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\jflap_extended\net_v2.1"
        // another example -s "E:\openjdk-6-src-b33-14_oct_2014.tar\jdk\src\share\classes\java\\util" -j "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\jflap\\util" -p "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\java.util public API" -o "C:\Users\Otmar\dropbox\SpecMining\dataset\specs\jflap_extended\\util_v2.0"
        // or -s "/Users/otmarpereira/Documents/openjdk6-b33/jdk/src/share/classes/java/net/" -j "/Users/otmarpereira/Documents/mute_dataset/specs/jflap/net" -o "/Users/otmarpereira/Documents/mute_dataset/specs/jflap_extended/net"
        // -s "/Users/otmarpereira/Documents/openjdk6-b33/jdk/src/share/classes/java/net/" -j "/Users/otmarpereira/Documents/mute_dataset/specs/jflap/net" -o "/Users/otmarpereira/Documents/cores/specs/java6/Initial_Specs/Pradels_Specs/jflap/"
        // for util -s "/Users/otmarpereira/Documents/openjdk6-b33/jdk/src/share/classes/java/util/" -j "/Users/otmarpereira/Documents/mute_dataset/specs/jflap/util" -o "/Users/otmarpereira/Dropbox/SpecMining/dataset/specs/jflap_extended/util_v2.1/"
        Map<String, String> options = ExecutionArgsHelper.convertArgsToMap(args);

        if (options.containsKey(HELP_OPTION)) {
            ExecutionArgsHelper.displayHelp(Arrays.asList(
                    "In order to execute this program options:",
                    "-j <PATH> : Where to recursivelly search for jflap files equivalent to Pradel's reference automata.",
                    "-s <PATH> : Folder where source code corresponding to the FSM model can be found.",
                    "-o <PATH> : Folder where automata with extended transitions will be saved.",
                    "-p <PATH> : Folder where public API methods of corresponding package will be saved."
            ));
        }

        if (validateInputArguments(options)) {
            extendedOriginalSpecification(options);
        }
    }

    private static boolean validateInputArguments(Map<String, String> programOptions) {
        boolean ok = true;
        if (!programOptions.containsKey(JFLAP_PATH_OPTION)) {
            System.err.println("You must use the -j option to inform a valid path where to search for original jflap files.");
            ok = false;
        } else {
            File f = new File(programOptions.get(JFLAP_PATH_OPTION));

            if (!f.exists()) {
                System.err.println("The supplied jflap files path does not exist.");
                ok = false;
            }
        }

        if (!programOptions.containsKey(SOURCE_CODE_PATH_OPTION)) {
            System.err.println("You must use the -s option to inform a valid path where to search for classes'source code.");
            ok = false;
        } else {
            File f = new File(programOptions.get(SOURCE_CODE_PATH_OPTION));

            if (!f.exists()) {
                System.err.println("The supplied source code path does not exist.");
                ok = false;
            }
        }

        if (!programOptions.containsKey(OUTPUT_OPTION)) {
            System.out.println("WARNING: No output file informed. Specification will be printed on standard output.");
        }

        return ok;
    }

    static Set<String> getAllMethodsViaJavaReflection(String fullClassName) {
        try {
            Class<?> cls = Class.forName(fullClassName);
            //List<Method> classMethods = Arrays.asList(cls.getMethods());
            Set<Method> classMethods = ReflectionUtils.getAllMethods(cls, Predicates.not(withModifier(Modifier.PUBLIC)));

            Set<String> signatures = classMethods.stream().map(
                    m -> String.format("%s.%s()", fullClassName, m.getName())
            ).collect(Collectors.toSet());

            return signatures;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JflapFileManipulator.class.getName()).log(Level.SEVERE, null, ex);
            // throw new RuntimeException(ex);
            return new HashSet<>();
        }
    }

    static Map<String, Set<String>> getClassesPublicMethods(String sourceCodeRootPath) throws IOException {
        Map<String, Set<String>> publicAPI = new HashMap<>();
        File javaFilesFolder = new File(sourceCodeRootPath);
        String[] extensions = new String[]{"java"};

        List<File> files = FileUtils.listFiles(javaFilesFolder, extensions, true).stream().collect(Collectors.toList());
        Map<String, String> classesParents = new HashMap<>();

        for (File sourceFile : files) {
            GetMethodsViaRegexExtractor extractor = new GetMethodsViaRegexExtractor(sourceFile);

            Set<String> result = new HashSet<>(extractor.getAllMethods());

            publicAPI.put(extractor.getFullClassName(), result);

            if (extractor.isPublicClass()) {
                Set<String> reflectionResults = getAllMethodsViaJavaReflection(extractor.getFullClassName());
                publicAPI.get(extractor.getFullClassName()).addAll(reflectionResults);
            }
            String baseClass = extractor.getBaseClass();
            if (baseClass != null) {
                Optional<File> baseClassFile = files.stream().filter(f -> f.getName().equals(baseClass + ".java")).findFirst();
                if (baseClassFile.isPresent()) {
                    classesParents.put(extractor.getFullClassName(), JavaFileParsingHelper.getFullClassName(baseClassFile.get()));
                }
            }
        }

        classesParents.keySet().stream().forEach((childClass) -> {
            String parentClass = classesParents.get(childClass);
            Set<String> inheritedMethods = publicAPI.get(parentClass);
            inheritedMethods = inheritedMethods.stream().map(sig -> sig.replace(parentClass, childClass))
                    .collect(Collectors.toSet());
            publicAPI.get(childClass).addAll(inheritedMethods);
        });

        return publicAPI;
    }

    private static void extendedOriginalSpecification(Map<String, String> options) throws IOException {
        Map<String, Set<String>> publicAPI = getClassesPublicMethods(options.get(SOURCE_CODE_PATH_OPTION));

        File originalSpecsFolder = new File(options.get(JFLAP_PATH_OPTION));
        String[] extensions = new String[]{"jff"};
        File outputDir = null;

        if (options.containsKey(OUTPUT_OPTION)) {
            outputDir = new File(options.get(OUTPUT_OPTION));
        }

        if (options.containsKey(PUBLIC_API_OUTPUT_FOLDER)) {
            File apiFolder = new File(options.get(PUBLIC_API_OUTPUT_FOLDER));

            if (apiFolder.exists()) {
                publicAPI.keySet().stream()
                        .forEach(k -> {
                            File clazzAPI = new File(apiFolder, k + "_public_methods.txt");
                            try {
                                FileUtils.writeLines(clazzAPI, publicAPI.get(k));
                            } catch (IOException ex) {
                                Logger.getLogger(PradelRefSpecsExtender.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
            }
        } else {

            List<File> originalSpecFiles = FileUtils.listFiles(originalSpecsFolder, extensions, true).stream().collect(Collectors.toList());

            for (File file : originalSpecFiles) {
                JflapFileManipulator jffManipulator = new JflapFileManipulator(file);
                jffManipulator.includeTransitions(publicAPI, false);
                String extendedSpecPath = Paths.get(outputDir.getPath(), file.getName().replace(".jff", "_package_extended.jff")).toFile().getAbsolutePath();
                jffManipulator.saveToFile(extendedSpecPath);
            }
        }
    }
}
