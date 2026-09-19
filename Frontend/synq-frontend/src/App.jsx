import Login from "./auth/Login";
import { sendMessage } from "./services/websocketService";

function App() {

    // Sends a test message through the authenticated WebSocket connection.
    const handleSendMessage = () => {

        const message = {
            conversationId: "542feed6-7517-4090-a9bf-9a67e30f7463",
            content: "Hello @testuser3"
        };

        // Send the message to the backend.
        sendMessage(JSON.stringify(message));
    };

    return (
        <div>
            <Login />

            <hr />

            <button onClick={handleSendMessage}>
                Send Message
            </button>
        </div>
    );
}

export default App;