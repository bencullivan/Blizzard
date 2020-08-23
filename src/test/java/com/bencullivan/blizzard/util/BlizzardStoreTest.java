package com.bencullivan.blizzard.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlizzardStoreTest {

    private BlizzardStore store = new BlizzardStore(10, 10, 10);

    @AfterEach
    public void resetStore() {
        store = new BlizzardStore(10, 10, 10);
    }

    @Test
    public void getTrieTest() {
        String expected = "/\n" +
                "Has parameters: n Has callback: y\n" +
                "/days/\n" +
                "Has parameters: n Has callback: n\n" +
                "/days/tuesday/\n" +
                "Has parameters: n Has callback: y\n" +
                "/days/monday/\n" +
                "Has parameters: n Has callback: n\n" +
                "/days/monday/morning/\n" +
                "Has parameters: n Has callback: y\n" +
                "/posts/\n" +
                "Has parameters: n Has callback: y\n" +
                "/posts/all/\n" +
                "Has parameters: n Has callback: n\n" +
                "/posts/all/tuesday/\n" +
                "Has parameters: n Has callback: y\n" +
                "/posts/one/\n" +
                "Has parameters: y Has callback: n\n" +
                "/posts/one/:postId/\n" +
                "Has parameters: y Has callback: y\n";
        store.insertGetRoute(new String[0], (req, res) -> System.out.println("yay"));
        store.insertGetRoute(new String[] {"posts", "all", "tuesday"}, (req, res) -> System.out.println("yay"));
        store.insertGetRoute(new String[] {"posts", "one", ":postId"}, (req, res) -> System.out.println("yay"));
        store.insertGetRoute(new String[] {"days", "monday", "morning"}, (req, res) -> System.out.println("yay"));
        store.insertGetRoute(new String[] {"days", "tuesday"}, (req, res) -> System.out.println("yay"));
        store.insertGetRoute(new String[] {"posts"}, ((req, res) -> System.out.println("yay")));
        StringBuilder actual = new StringBuilder();
        displayTrie(store.getGetRoot(), "/", actual);
        assertEquals(expected, actual.toString());
    }

    public void displayTrie(PathNode node, String path, StringBuilder sb) {
        sb.append(path).append('\n');
        sb.append("Has parameters: ").append(node.hasParams() ? 'y' : 'n').append(" Has callback: ")
                .append(node.getCallback() != null ? 'y' : 'n').append('\n');
        if (node.hasParams() && node.hasChildren()) {
            displayTrie(node.getChild(""), path + ":" + node.getChild("").getParamName() + "/", sb);
        }
        else if (node.hasChildren()) {
            for (String suffix: node.getChildren().keySet()) {
                displayTrie(node.getChild(suffix), path + suffix + "/", sb);
            }
        }
    }
}
