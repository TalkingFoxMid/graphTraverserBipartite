package ru.traverser;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import com.google.common.primitives.Ints;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main  {
    public static void main(String[] args) throws IOException {
        AtomicBoolean notBipartite = new AtomicBoolean(false);
        var colors = new HashMap<Integer, Integer>();
        var scanner = new Scanner(new File("input"));

        FileOutputStream outputStream = new FileOutputStream("output");
        MutableGraph<Integer> graph = GraphBuilder.undirected().build();
        Streams.zip(Stream.generate(scanner::nextLine)
                .limit(Ints.tryParse(scanner.nextLine()))
                .map(Splitter.on(" ")::split)
                .map(words -> StreamSupport.stream(words.spliterator(), false)),
                Stream.iterate(0, n->n+1),
                (row, rowIndex) -> Streams.zip(
                        row,
                        Stream.iterate(0, n->n+1),
                        (value, colIndex) -> new Pair<String, Pair<Integer, Integer>>(
                                value,
                                new Pair<Integer, Integer>(rowIndex, colIndex))
                ).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .filter(x -> x.getKey().equals("1"))
                .map(Pair::getValue)
                .forEach(x -> graph.putEdge(x.getKey(), x.getValue()));
      
        colors.put(0, 1);
        Traverser.forGraph(graph).depthFirstPreOrder(0).forEach(
                x -> {
                    var xcolor = colors.getOrDefault(x, 0);
                    if (graph.adjacentNodes(x).stream().
                            anyMatch(y -> xcolor.equals(colors.getOrDefault(y,0)))) {
                        notBipartite.set(true);
                    }
                    graph.adjacentNodes(x).forEach(y -> colors.put(y, -colors.get(x)));
                }
        );
        if (notBipartite.get()) {
            outputStream.write("N".getBytes());
        } else {
            var result = new ArrayList<>();
            colors
                .keySet().stream()
                .collect(Collectors.groupingBy(x -> colors.getOrDefault(x, 0)))
                .values().stream()
                .sorted(Comparator.comparingInt(x -> x.stream().min(Comparator.naturalOrder()).get()))
                .forEach(x -> result.add(String.join(" ",
                        x.stream().map(y -> y.toString()).collect(Collectors.toList()))));
            outputStream.write(result.get(0).toString().getBytes());
            outputStream.write(Integer.valueOf(0).toString().getBytes());
            System.out.println(result.get(1).toString().getBytes());
        }
    }
}

