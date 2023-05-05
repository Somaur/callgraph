package callgraph.callgraph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiMethodUtil;
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
        for (PsiClass anInterface : method.getContainingClass().getInterfaces()) {
            PsiMethod methodBySignature = anInterface.findMethodBySignature(method, false);
            if (methodBySignature != null) {
                allReferences.addAll(ReferencesSearch.search(methodBySignature).findAll());
            }
        }
        for (PsiReference reference : allReferences) {
            PsiElement element = reference.getElement();
            PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (caller == null || caller.equals(method) || !caller.getProject().equals(method.getProject())) continue;

            // todo: this should be slow, find a better way to do this (maybe use a map?)
            final boolean nodeNotExists = nodes.stream().noneMatch(node -> ((JSONObject) node).get("id").equals(caller.hashCode()));

            if (nodeNotExists) {
                JSONObject callerNode = createMethodNode(caller, depth);
                nodes.add(callerNode);
                createGroupIfNotExists(caller);
            }

            // todo: this should be slow, find a better way to do this (maybe use a map?)
            final boolean edgeNotExists = edges.stream().noneMatch(edge -> {
                JSONObject edgeObject = (JSONObject) edge;
                return edgeObject.get("from").equals(caller.hashCode()) && edgeObject.get("to").equals(method.hashCode());
            });

            if (edgeNotExists) {
                JSONObject edge = new JSONObject();
                edge.put("from", caller.hashCode());
                edge.put("to", method.hashCode());
                edges.add(edge);
            }

            // tried to avoid stackoverflows for methods that call each other recursively, not sure if this works, seems like so
            // todo: check properly if this works
            if (nodeNotExists || edgeNotExists) {
                findAndAddCallers(caller, depth + 1);
            }
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

            JSONObject hoverAndHighlightColor = new JSONObject();
            hoverAndHighlightColor.put("background", Utils.darkenHexColor(randomColorFromPalette, 0.1));
            hoverAndHighlightColor.put("border", Utils.darkenHexColor(randomColorFromPalette, 0.2));

            color.put("highlight", hoverAndHighlightColor);
            color.put("hover", hoverAndHighlightColor);

            JSONObject font = new JSONObject();
            font.put("color", Utils.getTextColorFromBackground(randomColorFromPalette));

            group.put("color", color);
            group.put("font", font);

            groups.put(method.getContainingClass().getQualifiedName(), group);
        }
    }
}
