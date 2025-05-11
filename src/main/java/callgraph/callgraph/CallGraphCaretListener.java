package callgraph.callgraph;

import callgraph.callgraph.browser.BrowserManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class CallGraphCaretListener implements CaretListener {
    private final Project project;
    private final ToolWindow toolWindow;

    public CallGraphCaretListener(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (toolWindow.isVisible()) {
            Editor editor = event.getEditor();
            
            // Only process events from editors in the current project
            if (editor.getProject() != null && editor.getProject().equals(project)) {
                PsiMethod method = Utils.getMethodAtCaret(project, editor);

                if (method != null) {
                    BrowserManager.getInstance(project).setGenerateMessage("+FOR " + method.getName());
                } else {
                    BrowserManager.getInstance(project).setGenerateMessage("-PLACE YOUR CARET ON A METHOD");
                }
            }
        }
    }
}
