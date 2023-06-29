package callgraph.callgraph;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.*;
import java.util.stream.Collectors;

public final class Utils {
    private Utils() {
    }

    private static final String[] colorPalette = {
            "#FF0000", "#FFA500", "#FFFF00", "#008000", "#00FFFF", "#0000FF",
            "#800080", "#FF00FF", "#FFC0CB", "#FFD700", "#8B4513", "#808080",
            "#FF69B4", "#00FF00", "#FF4500", "#000080", "#800000", "#FFDAB9",
            "#6495ED", "#FF6347", "#ADFF2F", "#9932CC", "#FA8072", "#F0E68C"
    };

    private static int colorIndex = 0;

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public static String getRandomColorFromPalette() {
        return colorPalette[colorIndex++ % colorPalette.length];
    }

    public static String getTextColorFromBackground(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);
        double yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000d;
        return (yiq >= 128) ? "#000000" : "#ffffff";
    }

    public static String darkenHexColor(String hexColor, double percent) {
        // Parse the hex color string to an RGB color
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        // Calculate the darker RGB values using the percentage
        red = (int) (red * (1 - percent));
        green = (int) (green * (1 - percent));
        blue = (int) (blue * (1 - percent));

        // Convert the darker RGB values back to a hex color string
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public static void writeToFile(String fileName, String content) throws IOException {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    public static PsiMethod getMethodAtCaret(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        if (element != null) {
            if (element.getParent() instanceof PsiMethod) {
                return (PsiMethod) element.getParent();
            } else if (element.getParent() instanceof PsiReference) {
                element = ((PsiReference) element.getParent()).resolve();
                if (element instanceof PsiMethod) {
                    return (PsiMethod) element;
                }
            }
        }

        return PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }
}
