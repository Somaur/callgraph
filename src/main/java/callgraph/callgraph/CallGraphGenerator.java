package callgraph.callgraph;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
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

        JSONObject mainNode = createMethodNode(mainMethod, 0);
        mainNode.put("shape", "circle");
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
        Collection<PsiReference> allReferences = ReferencesSearch.search(method).findAll();
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

    private JSONObject createMethodNode(PsiMethod method, int depth) {
        JSONObject callerNode = new JSONObject();
        callerNode.put("id", method.hashCode());
        callerNode.put("label", method.getContainingClass().getName() + "\n" + method.getName());
        callerNode.put("group", method.getContainingClass().getQualifiedName());
        callerNode.put("title", method.getContainingClass().getQualifiedName() + "\n" + method.getName());
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
