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

    // --- API HANDLER: Processes human move, plays Engine move, returns bundled data ---
    static class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String humanMoveStr = new String(is.readAllBytes()).trim();
                System.out.println("Received move from frontend: " + humanMoveStr);

                String lastMoveStr = "";

                if (humanMoveStr.equals("RESET")) {
                    board = new Board();
                } else if (humanMoveStr.length() >= 4) {
                    int startSq = parseSquare(humanMoveStr.substring(0, 2));
                    int targetSq = parseSquare(humanMoveStr.substring(2, 4));
                    
                    List<Move> legalMoves = generator.getLegalMoves(board, true);
                    Move chosenMove = null;
                    for (Move m : legalMoves) {
                        if (m.startSquare == startSq && m.targetSquare == targetSq) {
                            chosenMove = m; break;
                        }
                    }

                    if (chosenMove != null) {
                        board.makeMove(chosenMove, true);
                        lastMoveStr = humanMoveStr; // Default to human move
                        
                        // Engine's Turn
                        Move aiMove = ai.getBestMove(board, 5, false);
                        if (aiMove != null) {
                            board.makeMove(aiMove, false);
                            lastMoveStr = squareToAlgebraic(aiMove.startSquare) + squareToAlgebraic(aiMove.targetSquare);
                        }
                    }
                }

                // Generate legal moves for the human's NEXT turn (White)
                StringBuilder movesStr = new StringBuilder();
                List<Move> humanLegalMoves = generator.getLegalMoves(board, true);
                for (Move m : humanLegalMoves) {
                    movesStr.append(squareToAlgebraic(m.startSquare))
                            .append(squareToAlgebraic(m.targetSquare))
                            .append(",");
                }

                // Bundle the data: FEN | LAST_MOVE | LEGAL_MOVES
                String finalResponse = board.toFEN() + "|" + lastMoveStr + "|" + movesStr.toString();
                sendResponse(exchange, finalResponse);
            }
        }

        private int parseSquare(String sq) {
            int file = sq.charAt(0) - 'a';
            int rank = sq.charAt(1) - '1';
            return rank * 8 + file;
        }

        // Helper to convert index (e.g., 12) to text (e.g., "e2")
        private String squareToAlgebraic(int sq) {
            int file = sq % 8;
            int rank = sq / 8;
            return "" + (char)('a' + file) + (char)('1' + rank);
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
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