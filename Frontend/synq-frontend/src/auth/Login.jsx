import { useState } from "react";
import { login } from "./authService";
import {
    connectWebSocket,
    subscribeToConversation
} from "../services/websocketService";

function renderMessageContent(content) {
    const parts = content.split(/(@[a-zA-Z0-9_]+)/g);

    return parts.map((part, index) => {

        if (part.startsWith("@")) {
            return (
                <span
                    key={index}
                    style={{
                        fontWeight: "bold",
                        backgroundColor: "#e8f0fe",
                        padding: "2px 4px",
                        borderRadius: "4px"
                    }}
                >
                    {part}
                </span>
            );
        }

        return <span key={index}>{part}</span>;
    });
}

function Login() {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [messages, setMessages] = useState([]);

    // Handles user login.
    const handleLogin = async (event) => {

        event.preventDefault();

        setError("");
        setLoading(true);

        try {

            await login(email, password);

            console.log("Login successful.");

            connectWebSocket(() => {

                subscribeToConversation(
                    "542feed6-7517-4090-a9bf-9a67e30f7463",
                    (message) => {

                        console.log(
                            "Conversation event:",
                            message
                        );

                        setMessages((previousMessages) => [
                            ...previousMessages,
                            message
                        ]);
                    }
                );
            });

        } catch (error) {

            console.error("Login failed:", error);

            setError("Invalid email or password.");

        } finally {

            setLoading(false);
        }
    };

    return (
        <div>

            <h1>Synq Login</h1>

            <form onSubmit={handleLogin}>

                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(event) =>
                        setEmail(event.target.value)
                    }
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(event) =>
                        setPassword(event.target.value)
                    }
                    required
                />

                <button
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Logging in..." : "Login"}
                </button>

                {error && <p>{error}</p>}

            </form>

            <div>
                <h2>Messages</h2>

                {messages.map((message) => (
                    <p key={message.id}>
                        {renderMessageContent(message.content)}
                    </p>
                ))}
            </div>

        </div>
    );
}

export default Login;