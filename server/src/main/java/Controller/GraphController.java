package Controller;

import Model.Parcel;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.tour.HeldKarpTSP;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.DepthFirstIterator;
import com.mxgraph.layout.*;
import com.mxgraph.swing.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class GraphController {
    private Graph<Parcel, WeightedEdge> graph;

    public static class WeightedEdge extends DefaultWeightedEdge {
        @Override
        public String toString() {
            return String.format("%.2f",getWeight());
        }
    }

    public GraphController(){
        emptyGraph();
    }

    public GraphController(List<Parcel> parcelList){
        emptyGraph();
        if(parcelList != null){
            for (Parcel parcel : parcelList){
                addParcelToCompleteGraph(parcel);
            }
        }
    }

    public void emptyGraph(){
        this.graph = new DefaultUndirectedWeightedGraph<Parcel, WeightedEdge>(WeightedEdge.class);
    }

    private double distance(Point p1, Point p2){
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) *  (p1.y - p2.y));
    }

    public void printGraph(){
        DepthFirstIterator<Parcel, WeightedEdge> depthFirstIterator = new DepthFirstIterator<>(graph);
        List<Parcel> parcels = new ArrayList<Parcel>();
        while(depthFirstIterator.hasNext()) {
            Parcel currentParcel = (Parcel)depthFirstIterator.next();
            parcels.add(currentParcel);

        }
        Iterator it = graph.edgeSet().iterator();
        while(it.hasNext()){
            WeightedEdge currentEdge = (WeightedEdge)it.next();
        }
        for(Parcel parcel : graph.vertexSet()){

        }

    }

    public void removeParcelFromGraph(Parcel parcel){
        int ID = parcel.getID();
        Iterator<WeightedEdge> it = graph.edgeSet().iterator();
        List<WeightedEdge> toBeDeleted = new ArrayList<>(graph.edgeSet().size());
        while(it.hasNext()){
            WeightedEdge edge = it.next();
            if((    (Parcel)graph.getEdgeTarget(edge)).getID() == ID ||
                    ((Parcel)graph.getEdgeSource(edge)).getID() == ID){
                toBeDeleted.add(edge);
            }
        }
        for(WeightedEdge edge : toBeDeleted){
            graph.removeEdge(edge);
        }
        int size = graph.vertexSet().size();
        Object[] parcels = graph.vertexSet().toArray();
        for(int i=0;i<size;i++){
            if(((Parcel)parcels[i]).getID() == ID){
                graph.removeVertex((Parcel)parcels[i]);
            }
        }
        printGraph();
    }

    public void updateParcelInGraph(Parcel parcel){
        removeParcelFromGraph(parcel);
        addParcelToCompleteGraph(parcel);
    }

    private Graph convertGraph(){
        GraphPath<Parcel,WeightedEdge> hamiltonianPath = new HeldKarpTSP<Parcel,WeightedEdge>().getTour(graph);
        DefaultUndirectedWeightedGraph<Parcel, WeightedEdge> auxGraph = new DefaultUndirectedWeightedGraph<Parcel, WeightedEdge>(WeightedEdge.class);
        List<Parcel> parcelList = hamiltonianPath.getVertexList();
        List<WeightedEdge> edgesList = hamiltonianPath.getEdgeList();
        for(Parcel parcel : parcelList){
            auxGraph.addVertex(parcel);
        }
        for(WeightedEdge edge : edgesList){
            WeightedEdge auxEdge = auxGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
            auxGraph.setEdgeWeight(auxEdge, graph.getEdgeWeight(edge));
        }
        ListenableGraph<Parcel, WeightedEdge> g =
                new DefaultListenableGraph<Parcel, WeightedEdge>(auxGraph);

        return g;
    }

    public Graph getGraph(){
        return convertGraph();
    }

    public Object saveGraphAsLayout(){
        printGraph();
        Graph convertedGraph = convertGraph();
        return createContainer(convertedGraph);
    }

    public Object createContainer(Graph graph){
        JGraphXAdapter<Parcel, WeightedEdge> graphAdapter =
                new JGraphXAdapter<Parcel,WeightedEdge>(graph);


        JFrame frame = new JFrame("Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mxGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        mxGraphModel graphModel  = (mxGraphModel)graphComponent.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();
        mxUtils.setCellStyles(graphComponent.getGraph().getModel(),
                cells.toArray(), mxConstants.STYLE_ENDARROW, mxConstants.NONE);


        frame.getContentPane().add(graphComponent,BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame.getContentPane();
    }

    public void addParcelToCompleteGraph(Parcel newVertex){
        this.graph.addVertex(newVertex);
        DepthFirstIterator<Parcel, WeightedEdge> depthFirstIterator = new DepthFirstIterator<>(graph);
        while(depthFirstIterator.hasNext()){
            Parcel currentVertex = (Parcel)depthFirstIterator.next();
            if(!currentVertex.equals(newVertex)) {
                WeightedEdge edge = graph.addEdge(currentVertex, newVertex);
                graph.setEdgeWeight(edge, distance(currentVertex.getCoordinates(), newVertex.getCoordinates()));
            }
        }
    }
}
