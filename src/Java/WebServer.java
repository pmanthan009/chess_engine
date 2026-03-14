import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WebServer {

    // Global engine instances for the active game
    private static Board board = new Board();
    private static MoveGen generator = new MoveGen();
    private static Search ai = new Search();

    public static void main(String[] args) throws Exception {
        // Create server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1. Serve the frontend files (HTML, CSS, SVGs)
        server.createContext("/", new StaticFileHandler());

        // 2. Handle the AI move logic
        server.createContext("/api/move", new MoveHandler());

        server.setExecutor(null);
        System.out.println("Current Working Directory: " + System.getProperty("user.dir"));
        server.start();
        System.out.println("Engine Web Server running! Open http://localhost:8080/EnPassant.html in your browser.");
    }

    // --- API HANDLER: Processes human move, plays Engine move, returns FEN ---
    static class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the "e2e4" string from Javascript
                InputStream is = exchange.getRequestBody();
                String humanMoveStr = new String(is.readAllBytes()).trim();
                System.out.println("Received move from frontend: " + humanMoveStr);

                // Restart the game if they send "RESET"
                if (humanMoveStr.equals("RESET")) {
                    board = new Board();
                    sendResponse(exchange, board.toFEN());
                    return;
                }

                // 1. Parse and validate human move
                if (humanMoveStr.length() >= 4) {
                    int startSq = parseSquare(humanMoveStr.substring(0, 2));
                    int targetSq = parseSquare(humanMoveStr.substring(2, 4));

                    List<Move> legalMoves = generator.getLegalMoves(board, true); // White's turn
                    Move chosenMove = null;

                    for (Move m : legalMoves) {
                        if (m.startSquare == startSq && m.targetSquare == targetSq) {
                            chosenMove = m;
                            break;
                        }
                    }

                    if (chosenMove != null) {
                        // Play Human Move
                        board.makeMove(chosenMove, true);

                        // Let the Engine Think (Depth 5 for maximum speed + strength)
                        Move aiMove = ai.getBestMove(board, 5, false);
                        if (aiMove != null) {
                            board.makeMove(aiMove, false);
                        }
                    } else {
                        System.out.println("Illegal move attempted!");
                    }
                }

                // Return the updated FEN string to JavaScript
                sendResponse(exchange, board.toFEN());
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }

        private int parseSquare(String sq) {
            int file = sq.charAt(0) - 'a';
            int rank = sq.charAt(1) - '1';
            return rank * 8 + file;
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            // Allow CORS just in case you run the frontend separately later
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

   // --- STATIC FILE HANDLER: Serves HTML, images, SVGs, etc. ---
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            if (uri.equals("/"))
                uri = "/EnPassant.html"; // Default page

            // 1. Strip the leading slash so Java doesn't get confused about root direct
            // ries!
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            
            Path filePath = Paths.get("../../web", uri); 

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                
                // 2. MIME Types: We MUST tell the browser what kind of file we are sending.
                String mimeType = "text/plain";
                if (uri.endsWith(".html")) mimeType = "text/html";
                else if (uri.endsWith(".css")) mimeType = "text/css";
                else if (uri.endsWith(".svg")) mimeType = "image/svg+xml"; // Crucial for your custom pieces!
                else if (uri.endsWith(".png")) mimeType = "image/png";
                else if (uri.endsWith(".js")) mimeType = "application/javascript";
                
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                
                byte[] bytes = Files.readAllBytes(filePath);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}