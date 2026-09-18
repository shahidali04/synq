import { Client } from "@stomp/stompjs";
import { getAccessToken, getUserId } from "../auth/authService";

// Stores active conversation subscriptions by conversation ID.
const conversationSubscriptions = new Map();

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

        // Listen for private notifications sent to the current user.
        if (userId) {
            websocketClient.subscribe(
                `/topic/user/${userId}/notifications`,
                (message) => {
                    console.log("Received notification:", message.body);
                }
            );
        }

        // Listen for WebSocket events that require changes to the user's subscriptions.
        if (userId) {
            websocketClient.subscribe(
                `/topic/user/${userId}/conversation-events`,
                (message) => {

                    const event = JSON.parse(message.body);

                    // Remove the user's subscription when they are no longer
                    // a member of the conversation.
                    if (event.type === "FORCE_UNSUBSCRIBE") {

                        const subscription =
                            conversationSubscriptions.get(event.conversationId);

                        if (subscription) {
                            subscription.unsubscribe();

                            conversationSubscriptions.delete(
                                event.conversationId
                            );

                            console.log(
                                `Unsubscribed from conversation: ${event.conversationId}`
                            );
                        }
                    }
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

// Subscribe to a conversation and keep track of the active subscription.
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

    // Store the subscription so it can be removed later if
    // the user is removed from or leaves the conversation.
    conversationSubscriptions.set(
        conversationId,
        subscription
    );

    console.log(
        `Subscribed to conversation: ${conversationId}`
    );

    return subscription;
};

// Disconnect from the WebSocket server.
export const disconnectWebSocket = () => {

    // Remove all active conversation subscriptions.
    conversationSubscriptions.forEach((subscription) => {
        subscription.unsubscribe();
    });

    conversationSubscriptions.clear();

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