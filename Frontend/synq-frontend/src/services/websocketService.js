import { Client } from "@stomp/stompjs";

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
    const accessToken = localStorage.getItem("accessToken");

    websocketClient.connectHeaders = {
        Authorization: `Bearer ${accessToken}`,
    };

    websocketClient.onConnect = () => {
        console.log("Websocket connected successfully.");

        // Subscribe to messages for the selected conversation.
        websocketClient.subscribe(
            "/topic/conversation/a9744cec-1ac9-487b-a74a-0777ddaa77b8",
            (message) => {
                console.log("Received WebSocket message:", message.body);
            }
        );

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