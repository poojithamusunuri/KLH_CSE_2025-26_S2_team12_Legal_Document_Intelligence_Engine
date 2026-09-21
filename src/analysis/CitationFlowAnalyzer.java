package analysis;

import structures.SimpleList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CitationFlowAnalyzer {

    public static class CitationFlowResult {

        private final int judgmentCount;
        private final int actCount;
        private final int edgeCount;
        private final int totalReferenceCount;
        private final int maxFlow;
        private final SimpleList<String> topJudgments;
        private final SimpleList<String> topActs;

        public CitationFlowResult(
                int judgmentCount,
                int actCount,
                int edgeCount,
                int totalReferenceCount,
                int maxFlow,
                SimpleList<String> topJudgments,
                SimpleList<String> topActs) {

            this.judgmentCount = judgmentCount;
            this.actCount = actCount;
            this.edgeCount = edgeCount;
            this.totalReferenceCount = totalReferenceCount;
            this.maxFlow = maxFlow;
            this.topJudgments = topJudgments;
            this.topActs = topActs;
        }

        public int getJudgmentCount() {
            return judgmentCount;
        }

        public int getActCount() {
            return actCount;
        }

        public int getEdgeCount() {
            return edgeCount;
        }

        public int getTotalReferenceCount() {
            return totalReferenceCount;
        }

        public int getMaxFlow() {
            return maxFlow;
        }

        public SimpleList<String> getTopJudgments() {
            return topJudgments;
        }

        public SimpleList<String> getTopActs() {
            return topActs;
        }
    }

    private static class Node {

        String id;
        String type;
        int totalOutgoing;
        int totalIncoming;
        SimpleList<Edge> outgoing;

        Node(String id, String type) {
            this.id = id;
            this.type = type;
            this.totalOutgoing = 0;
            this.totalIncoming = 0;
            this.outgoing = new SimpleList<>();
        }
    }

    private static class Edge {

        String fromId;
        String toId;
        int capacity;

        Edge(
                String fromId,
                String toId,
                int capacity) {

            this.fromId = fromId;
            this.toId = toId;
            this.capacity = capacity;
        }
    }

    private static class FlowEdge {

        int to;
        int capacity;
        int reverseIndex;

        FlowEdge(int to, int capacity) {
            this.to = to;
            this.capacity = capacity;
            this.reverseIndex = -1;
        }
    }

    public CitationFlowResult analyze(String csvPath) throws IOException {

        Path path = Paths.get(csvPath);

        if (!Files.exists(path)) {
            throw new IOException(
                    "Citation relationship file not found: "
                            + path.toAbsolutePath()
            );
        }

        SimpleList<Node> nodes = new SimpleList<>();
        SimpleList<Edge> edges = new SimpleList<>();

        BufferedReader reader = Files.newBufferedReader(path);

        String header = reader.readLine();

        if (header == null) {
            reader.close();
            throw new IOException(
                    "Citation relationship file is empty: "
                            + path.toAbsolutePath()
            );
        }

        String line;
        int totalReferenceCount = 0;

        while ((line = reader.readLine()) != null) {

            if (line.trim().isEmpty()) {
                continue;
            }

            String[] row = parseCsvLine(line);

            if (row.length < 7) {
                continue;
            }

            String judgmentId = row[0].trim();
            String actId = row[1].trim();
            String actTitle = row[2].trim();
            String aliasesUsed = row[5].trim();
            String sections = row[6].trim();

            int referenceCount = safeParseInt(row[4].trim(), 1);

            Node judgmentNode = getOrCreateNode(
                    nodes,
                    judgmentId,
                    "judgment"
            );

            Node actNode = getOrCreateNode(
                    nodes,
                    actId,
                    "act"
            );

            Edge edge = new Edge(
                    judgmentId,
                    actId,
                    referenceCount
            );

            edges.add(edge);

            judgmentNode.totalOutgoing += referenceCount;
            actNode.totalIncoming += referenceCount;
            judgmentNode.outgoing.add(edge);

            totalReferenceCount += referenceCount;
        }

        reader.close();

        int maxFlow = computeMaxFlow(nodes, edges);

        SimpleList<String> topJudgments =
                buildTopJudgments(nodes, 5, true);

        SimpleList<String> topActs =
                buildTopActs(nodes, 5, false);

        return new CitationFlowResult(
                countNodes(nodes, "judgment"),
                countNodes(nodes, "act"),
                edges.size(),
                totalReferenceCount,
                maxFlow,
                topJudgments,
                topActs
        );
    }

    private int computeMaxFlow(
            SimpleList<Node> nodes,
            SimpleList<Edge> edges) {

        SimpleList<String> nodeIds = collectNodeIds(nodes);

        int sourceIndex = nodeIds.size();
        int sinkIndex = sourceIndex + 1;

        SimpleList<SimpleList<FlowEdge>> adjacency =
                new SimpleList<>();

        for (int i = 0; i < nodeIds.size(); i++) {
            adjacency.add(new SimpleList<FlowEdge>());
        }

        adjacency.add(new SimpleList<FlowEdge>());
        adjacency.add(new SimpleList<FlowEdge>());

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            int nodeIndex = findIndex(nodeIds, node.id);

            if (node.type.equals("judgment")) {
                addEdge(
                        adjacency,
                        sourceIndex,
                        nodeIndex,
                        node.totalOutgoing
                );
            }

            if (node.type.equals("act")) {
                addEdge(
                        adjacency,
                        nodeIndex,
                        sinkIndex,
                        node.totalIncoming
                );
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);

            int fromIndex = findIndex(nodeIds, edge.fromId);
            int toIndex = findIndex(nodeIds, edge.toId);

            addEdge(
                    adjacency,
                    fromIndex,
                    toIndex,
                    edge.capacity
            );
        }

        return edmondsKarp(
                adjacency,
                sourceIndex,
                sinkIndex
        );
    }

    private int edmondsKarp(
            SimpleList<SimpleList<FlowEdge>> adjacency,
            int sourceIndex,
            int sinkIndex) {

        int flow = 0;

        while (true) {

            boolean[] visited = new boolean[adjacency.size()];
            int[] parentNode = new int[adjacency.size()];
            int[] parentEdge = new int[adjacency.size()];

            SimpleList<Integer> queue = new SimpleList<>();
            queue.add(sourceIndex);
            visited[sourceIndex] = true;

            int queuePointer = 0;
            boolean foundPath = false;

            while (queuePointer < queue.size()) {

                int current = queue.get(queuePointer);
                queuePointer++;

                SimpleList<FlowEdge> currentEdges =
                        adjacency.get(current);

                for (int i = 0; i < currentEdges.size(); i++) {

                    FlowEdge edge = currentEdges.get(i);

                    if (edge.capacity <= 0) {
                        continue;
                    }

                    if (visited[edge.to]) {
                        continue;
                    }

                    visited[edge.to] = true;
                    parentNode[edge.to] = current;
                    parentEdge[edge.to] = i;

                    if (edge.to == sinkIndex) {
                        foundPath = true;
                        break;
                    }

                    queue.add(edge.to);
                }

                if (foundPath) {
                    break;
                }
            }

            if (!foundPath) {
                break;
            }

            int pathFlow = Integer.MAX_VALUE;

            for (int v = sinkIndex;
                 v != sourceIndex;
                 v = parentNode[v]) {

                int u = parentNode[v];
                int index = parentEdge[v];

                FlowEdge edge =
                        adjacency.get(u).get(index);

                if (edge.capacity < pathFlow) {
                    pathFlow = edge.capacity;
                }
            }

            for (int v = sinkIndex;
                 v != sourceIndex;
                 v = parentNode[v]) {

                int u = parentNode[v];
                int index = parentEdge[v];

                FlowEdge forward =
                        adjacency.get(u).get(index);
                FlowEdge reverse =
                        adjacency.get(v).get(forward.reverseIndex);

                forward.capacity -= pathFlow;
                reverse.capacity += pathFlow;
            }

            flow += pathFlow;
        }

        return flow;
    }

    private void addEdge(
            SimpleList<SimpleList<FlowEdge>> adjacency,
            int from,
            int to,
            int capacity) {

        FlowEdge forward = new FlowEdge(to, capacity);
        FlowEdge backward = new FlowEdge(from, 0);

        forward.reverseIndex = adjacency.get(to).size();
        backward.reverseIndex = adjacency.get(from).size();

        adjacency.get(from).add(forward);
        adjacency.get(to).add(backward);
    }

    private static Node getOrCreateNode(
            SimpleList<Node> nodes,
            String id,
            String type) {

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            if (node.id.equalsIgnoreCase(id)) {
                return node;
            }
        }

        Node node = new Node(id, type);
        nodes.add(node);
        return node;
    }

    private static int countNodes(
            SimpleList<Node> nodes,
            String type) {

        int count = 0;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).type.equals(type)) {
                count++;
            }
        }

        return count;
    }

    private static SimpleList<String> buildTopJudgments(
            SimpleList<Node> nodes,
            int limit,
            boolean descending) {

        SimpleList<Node> judgments = new SimpleList<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            if (node.type.equals("judgment")) {
                judgments.add(node);
            }
        }

        for (int i = 0; i < judgments.size(); i++) {
            for (int j = i + 1; j < judgments.size(); j++) {
                Node left = judgments.get(i);
                Node right = judgments.get(j);

                if (left.totalOutgoing < right.totalOutgoing) {
                    judgments.set(i, right);
                    judgments.set(j, left);
                }
            }
        }

        SimpleList<String> topJudgments = new SimpleList<>();
        int count = 0;

        for (int i = 0; i < judgments.size() && count < limit; i++) {
            Node node = judgments.get(i);
            topJudgments.add(
                    node.id + " (" + node.totalOutgoing + ")"
            );
            count++;
        }

        return topJudgments;
    }

    private static SimpleList<String> buildTopActs(
            SimpleList<Node> nodes,
            int limit,
            boolean descending) {

        SimpleList<Node> acts = new SimpleList<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            if (node.type.equals("act")) {
                acts.add(node);
            }
        }

        for (int i = 0; i < acts.size(); i++) {
            for (int j = i + 1; j < acts.size(); j++) {
                Node left = acts.get(i);
                Node right = acts.get(j);

                if (left.totalIncoming < right.totalIncoming) {
                    acts.set(i, right);
                    acts.set(j, left);
                }
            }
        }

        SimpleList<String> topActs = new SimpleList<>();
        int count = 0;

        for (int i = 0; i < acts.size() && count < limit; i++) {
            Node node = acts.get(i);
            topActs.add(
                    node.id + " (" + node.totalIncoming + ")"
            );
            count++;
        }

        return topActs;
    }

    private static SimpleList<String> collectNodeIds(
            SimpleList<Node> nodes) {

        SimpleList<String> ids = new SimpleList<>();

        for (int i = 0; i < nodes.size(); i++) {
            ids.add(nodes.get(i).id);
        }

        return ids;
    }

    private static int findIndex(
            SimpleList<String> ids,
            String target) {

        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equalsIgnoreCase(target)) {
                return i;
            }
        }

        return -1;
    }

    private static String[] parseCsvLine(String line) {

        SimpleList<String> fields = new SimpleList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {

                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    inQuotes = !inQuotes;
                }

            } else if (currentChar == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);

            } else {
                current.append(currentChar);
            }
        }

        fields.add(current.toString().trim());

        String[] result = new String[fields.size()];

        for (int i = 0; i < fields.size(); i++) {
            result[i] = fields.get(i);
        }

        return result;
    }

    private static int safeParseInt(String value, int fallback) {

        if (value == null || value.isEmpty()) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
