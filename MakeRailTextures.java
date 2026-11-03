import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Делает текстуры player_detector_rail из ванильной detector_rail.
 *
 * Запуск (из корня проекта):   java MakeRailTextures.java
 * Необязательные аргументы:
 *   путь к jar или папке, где искать (можно несколько)
 *   --out=папка_куда_класть_png
 */
public class MakeRailTextures {

    static final String TEX_DIR = "assets/minecraft/textures/block/";
    static final String NAME = "player_detector_rail";

    public static void main(String[] args) throws Exception {
        Path out = Paths.get("src/main/resources/assets/teromod/textures/block");
        List<Path> roots = new ArrayList<>();
        for (String a : args) {
            if (a.startsWith("--out=")) {
                out = Paths.get(a.substring(6));
            } else {
                roots.add(Paths.get(a));
            }
        }
        if (roots.isEmpty()) {
            roots.add(Paths.get(System.getProperty("user.home"), ".gradle"));
            roots.add(Paths.get(".gradle"));
            String appdata = System.getenv("APPDATA");
            if (appdata != null) {
                roots.add(Paths.get(appdata, ".minecraft", "versions"));
            }
        }

        System.out.println("Searching for the game jar with vanilla textures (can take up to a minute)...");
        List<Path> found = findJars(roots);
        if (found.isEmpty()) {
            System.out.println("Could NOT find a jar containing the detector_rail texture.");
            System.out.println("Pass the path manually, for example:");
            System.out.println("  java MakeRailTextures.java \"C:\\Users\\YOU\\AppData\\Roaming\\.minecraft\\versions\\26.3\\26.3.jar\"");
            System.exit(1);
        }
        Path jar = found.get(0);
        System.out.println("Using textures from: " + jar);

        Files.createDirectories(out);
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            process(zip, "detector_rail", false, out.resolve(NAME + ".png"));
            process(zip, "detector_rail_on", true, out.resolve(NAME + "_on.png"));
        }
        System.out.println("Done. Files are in: " + out.toAbsolutePath());
    }

    // ---------- поиск jar ----------

    static List<Path> findJars(List<Path> roots) throws IOException {
        List<Path> result = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.exists(root)) continue;
            if (Files.isRegularFile(root)) {
                if (hasTexture(root)) result.add(root);
                continue;
            }
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(p -> p.toString().endsWith(".jar") && Files.isRegularFile(p))
                    .forEach(p -> {
                        if (hasTexture(p)) result.add(p);
                    });
            } catch (IOException | RuntimeException e) {
                // пропускаем недоступные папки
            }
        }
        result.sort(Comparator.comparingInt(MakeRailTextures::score).reversed());
        return result;
    }

    static int score(Path p) {
        String s = p.toString().toLowerCase();
        int sc = 0;
        if (s.contains("26.3")) sc += 2;
        if (s.contains("client")) sc += 1;
        return sc;
    }

    static boolean hasTexture(Path jar) {
        try (ZipFile z = new ZipFile(jar.toFile())) {
            return z.getEntry(TEX_DIR + "detector_rail.png") != null
                && z.getEntry(TEX_DIR + "detector_rail_on.png") != null;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    // ---------- обработка текстуры ----------

    static void process(ZipFile zip, String src, boolean on, Path dest) throws IOException {
        ZipEntry e = zip.getEntry(TEX_DIR + src + ".png");
        BufferedImage in;
        try (InputStream is = zip.getInputStream(e)) {
            in = ImageIO.read(is);
        }
        int size = in.getWidth(); // обычно 16
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                img.setRGB(x, y, in.getRGB(x, y));
            }
        }
        // всё считаем в сетке 16x16; для больших текстур масштабируем координаты
        int k = Math.max(1, size / 16);

        boolean[][] mask = findPlate(img, k);
        int count = 0, minX = 99, minY = 99, maxX = -1, maxY = -1;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (mask[y][x]) {
                    count++;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (count < 12) {
            System.out.println("  [" + src + "] plate not found, drawing my own 8x8 plate in the center");
            mask = new boolean[16][16];
            for (int y = 4; y < 12; y++) for (int x = 4; x < 12; x++) mask[y][x] = true;
            minX = 4; maxX = 11; minY = 4; maxY = 11;
            // заливаем её базовым серым, дальше перекрасится как обычная пластина
            for (int y = 4; y < 12; y++) {
                for (int x = 4; x < 12; x++) {
                    boolean edge = x == 4 || x == 11 || y == 4 || y == 11;
                    int g = edge ? 105 : 140;
                    fillBlock(img, k, x, y, rgb(g, g, g));
                }
            }
        } else {
            System.out.println("  [" + src + "] plate: x " + minX + ".." + maxX + ", y " + minY + ".." + maxY
                + " (" + count + " px)");
        }

        // перекрас: сохраняем светотень ванили, меняем оттенок на красный
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (!mask[y][x]) continue;
                recolorBlock(img, k, x, y, on);
            }
        }

        // человечек, по центру пластины
        drawPerson(img, k, minX, maxX, minY, maxY);

        ImageIO.write(img, "png", dest.toFile());
        System.out.println("  written: " + dest.getFileName());
    }

    /** Ищем серую пластину: заливка от центра по серым пикселям. */
    static boolean[][] findPlate(BufferedImage img, int k) {
        boolean[][] mask = new boolean[16][16];
        Deque<int[]> queue = new ArrayDeque<>();
        int[][] seeds = {{7, 7}, {8, 8}, {7, 8}, {8, 7}};
        for (int[] s : seeds) {
            if (isGray(img, k, s[0], s[1]) && !mask[s[1]][s[0]]) {
                mask[s[1]][s[0]] = true;
                queue.add(s);
            }
        }
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dd : d) {
                int nx = p[0] + dd[0], ny = p[1] + dd[1];
                // ограничиваем область, чтобы не залить боковые рельсы
                if (nx < 3 || nx > 12 || ny < 2 || ny > 13) continue;
                if (mask[ny][nx]) continue;
                if (!isGray(img, k, nx, ny)) continue;
                mask[ny][nx] = true;
                queue.add(new int[]{nx, ny});
            }
        }
        return mask;
    }

    static boolean isGray(BufferedImage img, int k, int x, int y) {
        int argb = img.getRGB(x * k, y * k);
        int a = (argb >>> 24) & 255;
        if (a < 200) return false;
        int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        int mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
        return mx - mn < 18;
    }

    static void recolorBlock(BufferedImage img, int k, int x, int y, boolean on) {
        for (int dy = 0; dy < k; dy++) {
            for (int dx = 0; dx < k; dx++) {
                int px = x * k + dx, py = y * k + dy;
                int argb = img.getRGB(px, py);
                int a = (argb >>> 24) & 255;
                int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
                double lum = 0.299 * r + 0.587 * g + 0.114 * b;
                double f = lum / 150.0;
                int nr, ng, nb;
                if (on) {
                    nr = clamp(240 * f);
                    ng = clamp(75 * f);
                    nb = clamp(60 * f);
                } else {
                    nr = clamp(175 * f);
                    ng = clamp(35 * f);
                    nb = clamp(35 * f);
                }
                img.setRGB(px, py, (a << 24) | (nr << 16) | (ng << 8) | nb);
            }
        }
    }

    static final String[] PERSON = {
        ".##.",
        "####",
        ".##.",
        ".##.",
        "#..#"
    };

    static void drawPerson(BufferedImage img, int k, int minX, int maxX, int minY, int maxY) {
        int w = maxX - minX + 1, h = maxY - minY + 1;
        int x0 = minX + (w - 4) / 2;
        int y0 = minY + (h - 5) / 2;
        int black = rgb(18, 18, 18);
        for (int row = 0; row < PERSON.length; row++) {
            for (int col = 0; col < 4; col++) {
                if (PERSON[row].charAt(col) == '#') {
                    int x = x0 + col, y = y0 + row;
                    if (x >= 0 && x < 16 && y >= 0 && y < 16) {
                        fillBlock(img, k, x, y, black);
                    }
                }
            }
        }
    }

    static void fillBlock(BufferedImage img, int k, int x, int y, int argb) {
        for (int dy = 0; dy < k; dy++) {
            for (int dx = 0; dx < k; dx++) {
                img.setRGB(x * k + dx, y * k + dy, argb);
            }
        }
    }

    static int rgb(int r, int g, int b) {
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }
}
