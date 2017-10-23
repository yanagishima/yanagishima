package yanagishima.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.codehaus.jackson.map.ObjectMapper;

public class MigrateV9 {

    public static void main(String[] args) throws IOException {

        if(args.length != 2 ) {
            System.out.println("please specify source and destination directory");
            System.exit(1);
        }

        String srcDir = args[0];
        String destDir = args[1];

        if(Paths.get(destDir).toFile().exists()) {
            System.out.println("destination directory already exists");
            System.exit(1);
        }

        for(File file : findFile(srcDir)) {
            String filePath = file.getAbsolutePath();
            System.out.println("processing " + filePath);
            File yyyymmddDir = file.getParentFile();
            File datasourceDir = yyyymmddDir.getParentFile();
            String srcAbsolutePath = file.getAbsolutePath();
            Path srcPath = Paths.get(srcAbsolutePath);
            Paths.get(destDir).toFile().mkdir();
            Paths.get(destDir, datasourceDir.getName()).toFile().mkdir();
            Paths.get(destDir, datasourceDir.getName(), yyyymmddDir.getName()).toFile().mkdir();

            ObjectMapper mapper = new ObjectMapper();

            if(filePath.endsWith(".json")) {
                String destFileName = file.getName().replace(".json", ".tsv");
                Path destPath = Paths.get(destDir, datasourceDir.getName(), yyyymmddDir.getName(), destFileName);
                try(BufferedReader br = Files.newBufferedReader(srcPath);
                    BufferedWriter bw = Files.newBufferedWriter(destPath);
                    CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").withRecordSeparator(System.getProperty("line.separator")));) {
                    String line = br.readLine();
                    while (line != null) {
                        try {
                            List row = mapper.readValue(line, List.class);
                            csvPrinter.printRecord(row);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        line = br.readLine();
                    }
                } catch (Exception e) {
                    Files.delete(destPath);
                    System.out.println("error " + filePath);
                    e.printStackTrace();
                }
            } else if(filePath.endsWith(".err")) {
                String destFileName = file.getName();
                Path destPath = Paths.get(destDir, datasourceDir.getName(), yyyymmddDir.getName(), destFileName);
                Files.copy(Paths.get(file.getAbsolutePath()), destPath);
            } else {
                throw new RuntimeException("file must be json or err");
            }
        }

    }

    public static List<File> findFile(String absolutePath) throws IOException {
        return Files.walk(Paths.get(absolutePath))
                .map(path -> path.toFile())
                .filter(file -> file.isFile())
                .filter(file -> file.getName().endsWith(".json") || file.getName().endsWith(".err"))
                .collect(Collectors.toList());
    }



}
