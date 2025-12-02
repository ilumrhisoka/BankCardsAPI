import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CodeCollector {

    // Имя выходного файла
    private static final String OUTPUT_FILE = "project_code.txt";

    // Какие расширения ищем
    private static final Set<String> EXTENSIONS = new HashSet<>(Arrays.asList(
            ".java", ".xml", ".yml", ".yaml", ".properties", ".sql", ".gradle", ".html", "Dockerfile"
    ));

    // Какие папки игнорируем
    private static final Set<String> IGNORED_DIRS = new HashSet<>(Arrays.asList(
            "target", "build", ".git", ".idea", ".mvn", ".vscode", "node_modules"
    ));

    public static void main(String[] args) {
        Path startPath = Paths.get("."); // Текущая директория
        Path outputPath = Paths.get(OUTPUT_FILE);

        System.out.println("Начинаю сборку кода...");

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    // Если папка в списке игнорируемых - пропускаем её и всё содержимое
                    if (IGNORED_DIRS.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();

                    // Пропускаем сам файл сборщика и выходной файл
                    if (fileName.equals("CodeCollector.java") || fileName.equals(OUTPUT_FILE)) {
                        return FileVisitResult.CONTINUE;
                    }

                    // Проверяем расширение
                    if (isTargetExtension(fileName)) {
                        try {
                            writeFileContent(writer, file);
                            System.out.println("Добавлен: " + file);
                        } catch (IOException e) {
                            System.err.println("Ошибка чтения файла: " + file + " -> " + e.getMessage());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("\nГотово! Весь код сохранен в файл: " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isTargetExtension(String fileName) {
        for (String ext : EXTENSIONS) {
            if (fileName.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static void writeFileContent(BufferedWriter writer, Path file) throws IOException {
        writer.write("================================================================================\n");
        writer.write("ФАЙЛ: " + file.toAbsolutePath() + "\n");
        writer.write("================================================================================\n\n");

        // Читаем все строки и пишем в выходной файл
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            // Если вдруг файл не в UTF-8, пробуем прочитать как есть (на всякий случай)
            writer.write("[ОШИБКА ЧТЕНИЯ ИЛИ НЕВЕРНАЯ КОДИРОВКА]\n");
        }

        writer.newLine();
        writer.newLine();
    }
}