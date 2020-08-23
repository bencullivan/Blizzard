package com.bencullivan.blizzard;

public class BlizzardTest {

    private final BlizzardServer app;

    public BlizzardTest() {
        app = new BlizzardServer(4, 20, 20,
                20, 2048);
    }

    private void addRoutes() {
        app.get("/", (req, res) -> res.sendText("Yay, it worked!").sendStatus(200));
        app.get("/main_page", (req, res) -> res.sendText("You have reached the main page of the app.")
                .sendStatus(200));
        app.get("/test_html", (req, res) -> {
            res.sendFile(System.getProperty("user.dir") + "/src/test/resources/html-res-test.html")
                    .sendStatus(200);
        });
    }

    private void listen() {
        app.listen(3000);
    }

    public static void main(String[] args) {

        // Go to localhost:3000 in the browser and see the GET routes in action

        BlizzardTest test = new BlizzardTest();
        test.addRoutes();
        test.listen();
    }
}
