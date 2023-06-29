package callgraph.callgraph;

import callgraph.callgraph.browser.BrowserManager;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class CallGraphCaretListener implements CaretListener {
    private ToolWindow toolWindow;

    public CallGraphCaretListener(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (toolWindow.isVisible()) {
            PsiMethod method = Utils.getMethodAtCaret(event.getEditor().getProject());

            if (method != null) {
                BrowserManager.getInstance().setGenerateMessage("+FOR " + method.getName());
            } else {
                BrowserManager.getInstance().setGenerateMessage("-PLACE YOUR CARET ON A METHOD");
            }
        }
    }
}
