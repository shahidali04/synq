import { useState } from "react";
import { login } from "./authService";
import { connectWebSocket } from "../services/websocketService";

function Login() {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // Handles user login.
    const handleLogin = async (event) => {

        event.preventDefault();

        setError("");
        setLoading(true);

        try {

            await login(email, password);

            console.log("Login successful.");

            connectWebSocket();

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
                    onChange={(event) => setEmail(event.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

                {error && <p>{error}</p>}

            </form>
        </div>
    );
}

export default Login;