import { Client } from "@stomp/stompjs";
import { getAccessToken, getUserId } from "../auth/authService";

const websocketClient = new Client({
    brokerURL: "ws://localhost:8080/ws",

    // Show useful connection information during development.
    debug: (message) => {
        console.log(message);
    },

    // Automatically try to reconnect if the connection is lost.
    reconnectDelay: 5000,
});

// Connect to the WebSocket server.
export const connectWebSocket = (onConnected) => {

    // Get the latest access token when connecting.
    const accessToken = getAccessToken();

    websocketClient.connectHeaders = {
        Authorization: `Bearer ${accessToken}`,
    };

    websocketClient.onConnect = () => {
        console.log("Websocket connected successfully.");

        const userId = getUserId();

        if (userId) {
            websocketClient.subscribe(
                `/topic/user/${userId}/notifications`,
                (message) => {
                    console.log("Received notification:", message.body);
                }
            );
        }

        if (onConnected) {
            onConnected();
        }
    };

    websocketClient.onStompError = (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
        console.error("Details:", frame.body);
    };

    websocketClient.activate();
};

// Subscribe to real-time messages and system events for a conversation.
export const subscribeToConversation = (conversationId, onMessage) => {

    if (!websocketClient.connected) {
        console.error("WebSocket is not connected.");
        return null;
    }

    const destination = `/topic/conversation/${conversationId}`;

    const subscription = websocketClient.subscribe(
        destination,
        (message) => {
            const messageData = JSON.parse(message.body);

            if (onMessage) {
                onMessage(messageData);
            }
        }
    );

    console.log(`Subscribed to conversation: ${conversationId}`);

    return subscription;
};

// Disconnect from the WebSocket server.
export const disconnectWebSocket = () => {
    if (websocketClient.active) {
        websocketClient.deactivate();
    }
};

// Send a message to the WebSocket server.
export const sendMessage = (message) => {
    if (!websocketClient.connected) {
        console.error("WebSocket is not connected.");
        return;
    }

    websocketClient.publish({
        destination: "/app/chat",
        body: message,
    });
};