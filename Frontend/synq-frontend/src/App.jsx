import Login from "./auth/Login";
import { sendMessage } from "./services/websocketService";

function App() {

    // Sends a test message through the authenticated WebSocket connection.
    const handleSendMessage = () => {

        const message = {
            conversationId: "b95d7089-7539-42f4-915d-668c65f73118",
            content: "Isolation test - Conversation C"
        };

        // Send the message to the backend.
        sendMessage(JSON.stringify(message));
    };

    return (
        <div>
            <Login />

            <hr />

            <button onClick={handleSendMessage}>
                Send WebSocket Message
            </button>
        </div>
    );
}

export default App;