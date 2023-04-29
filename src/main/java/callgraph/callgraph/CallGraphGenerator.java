package callgraph.callgraph;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;

public class CallGraphGenerator {
    private final PsiMethod mainMethod;
    private final JSONArray nodes;
    private final JSONArray edges;
    private final JSONObject groups;

    public CallGraphGenerator(PsiMethod mainMethod) {
        this.mainMethod = mainMethod;
        this.nodes = new JSONArray();
        this.edges = new JSONArray();
        this.groups = new JSONObject();
    }

    public String generate() {
        clear();

        JSONObject mainNode = new JSONObject();
        mainNode.put("id", mainMethod.hashCode());
        mainNode.put("label", mainMethod.getName());
        mainNode.put("group", mainMethod.getContainingClass().getQualifiedName());
        mainNode.put("title", mainMethod.getContainingClass().getQualifiedName());
        mainNode.put("level", 0);
        mainNode.put("x", 0);
        mainNode.put("y", 0);
        mainNode.put("fixed", true);
        createGroupIfNotExists(mainMethod);

        nodes.add(mainNode);

        findAndAddCallers(mainMethod, 1);

        JSONObject graph = new JSONObject();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("groups", groups);

        return graph.toJSONString();
    }

    private void clear() {
        nodes.clear();
        edges.clear();
        groups.clear();
    }

    private void findAndAddCallers(PsiMethod method, int depth) {
        Collection<PsiReference> allReferences = MethodReferencesSearch.search(method).findAll();
        for (PsiReference reference : allReferences) {
            PsiElement element = reference.getElement();
            PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (caller == null || caller.equals(method) || !caller.getProject().equals(method.getProject())) continue;

            if (nodes.stream().noneMatch(node -> ((JSONObject) node).get("id").equals(caller.hashCode()))) {
                JSONObject callerNode = createMethodNode(caller, depth);
                nodes.add(callerNode);
                createGroupIfNotExists(caller);
            }

            JSONObject edge = new JSONObject();
            edge.put("from", caller.hashCode());
            edge.put("to", method.hashCode());

            edges.add(edge);

            findAndAddCallers(caller, depth + 1);
        }
    }

    @NotNull
    private static JSONObject createMethodNode(PsiMethod caller, int depth) {
        JSONObject callerNode = new JSONObject();
        callerNode.put("id", caller.hashCode());
        callerNode.put("label", caller.getContainingClass().getName() + "\n" + caller.getName());
        callerNode.put("group", caller.getContainingClass().getQualifiedName());
        callerNode.put("title", caller.getContainingClass().getQualifiedName() + "\n" + caller.getName());
        callerNode.put("level", depth);
        return callerNode;
    }

    private void createGroupIfNotExists(PsiMethod method) {
        if (!groups.containsKey(method.getContainingClass().getQualifiedName())) {
            JSONObject group = new JSONObject();

            JSONObject color = new JSONObject();
            String randomColorFromPalette = Utils.getRandomColorFromPalette();
            color.put("background", randomColorFromPalette);
            color.put("border", randomColorFromPalette);

            JSONObject font = new JSONObject();
            font.put("color", Utils.getTextColorFromBackground(randomColorFromPalette));

            group.put("color", color);
            group.put("font", font);

            groups.put(method.getContainingClass().getQualifiedName(), group);
        }
    }
}
