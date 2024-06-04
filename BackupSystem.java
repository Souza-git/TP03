import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class BackupSystem {

    public static void main(String[] args) {
        String sourceDir = "data"; 
        String backupRootDir = createBackupRootDirectory(); 

        Scanner scanner = new Scanner(System.in);

        // menu
        while (true) {
            System.out.println("1. Cria Backup");
            System.out.println("2. Recupera Backup");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    createBackup(sourceDir, backupRootDir);
                    break;
                case 2:
                    listBackupVersions(backupRootDir);
                    System.out.print("Enter the version to recover: ");
                    String version = scanner.nextLine();
                    recoverBackup(backupRootDir, version, sourceDir);
                    break;
                case 3:
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static String createBackupRootDirectory() {
        String backupDir = "backup_data";
        Path backupPath = Paths.get(backupDir);

        try {
            if (!Files.exists(backupPath)) {
                Files.createDirectory(backupPath);
                System.out.println("Backup root directory created at: " + backupPath.toAbsolutePath().toString());
            } else {
                System.out.println("Backup root directory already exists at: " + backupPath.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return backupPath.toAbsolutePath().toString();
    }

    public static void createBackup(String sourceDir, String backupRootDir) {
        String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String versionDir = backupRootDir + File.separator + dateStr;

        // Cria versao backup 
        new File(versionDir).mkdirs();

        // Comprime e backup
        try {
            compressDirectory(sourceDir, versionDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compressDirectory(String sourceDir, String targetDir) throws IOException {
        Files.walk(Paths.get(sourceDir)).forEach(path -> {
            if (Files.isRegularFile(path)) {
                try {
                    String targetFile = targetDir + File.separator + path.getFileName().toString() + ".lzw";
                    compressFile(path.toString(), targetFile);

                    // Calcula a taxa de compresao
                    long originalSize = Files.size(path);
                    long compressedSize = Files.size(Paths.get(targetFile));
                    double compressionRatio = 100.0 * (originalSize - compressedSize) / originalSize;
                    System.out.printf("File: %s, Original size: %d bytes, Compressed size: %d bytes, Compression ratio: %.2f%%\n",
                            path.getFileName().toString(), originalSize, compressedSize, compressionRatio);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void compressFile(String sourceFile, String targetFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
            byte[] inputBytes = byteStream.toByteArray();
            byte[] compressedBytes = LZW.codifica(inputBytes); // Usa o LZW

            try (OutputStream out = new FileOutputStream(targetFile)) {
                out.write(compressedBytes);
            }
        }
    }

    public static void recoverBackup(String backupRootDir, String version, String targetDir) {
        String versionDir = backupRootDir + File.separator + version;

        // Descomprime e recupera
        try {
            decompressDirectory(versionDir, targetDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressDirectory(String sourceDir, String targetDir) throws IOException {
        Files.walk(Paths.get(sourceDir)).forEach(path -> {
            if (Files.isRegularFile(path) && path.toString().endsWith(".lzw")) {
                try {
                    String targetFile = targetDir + File.separator + path.getFileName().toString().replace(".lzw", "");
                    decompressFile(path.toString(), targetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void decompressFile(String sourceFile, String targetFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
            byte[] inputBytes = byteStream.toByteArray();
            byte[] decompressedBytes = LZW.decodifica(inputBytes); // descomprime usando lzw

            try (OutputStream out = new FileOutputStream(targetFile)) {
                out.write(decompressedBytes);
            }
        }
    }

    public static void listBackupVersions(String backupRootDir) {
        File rootDir = new File(backupRootDir);
        File[] backupVersions = rootDir.listFiles(File::isDirectory);

        if (backupVersions != null && backupVersions.length > 0) {
            System.out.println("Available backup versions:");
            for (File version : backupVersions) {
                System.out.println(version.getName());
            }
        } else {
            System.out.println("No backup versions available.");
        }
    }
}
