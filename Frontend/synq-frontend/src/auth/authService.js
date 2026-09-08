import axios from "axios";

// Backend authentication API.
const AUTH_API_URL = "http://localhost:8080/api/auth";

// Authenticates the user and stores the received tokens.
export const login = async (email, password) => {

    const response = await axios.post(
        `${AUTH_API_URL}/login`,
        {
            email,
            password
        }
    );

    const { accessToken, refreshToken } = response.data;

    // Store tokens so they can be used for authenticated requests.
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    return response.data;
};

// Returns the currently stored access token.
export const getAccessToken = () => {
    return localStorage.getItem("accessToken");
};

// Returns the currently stored refresh token.
export const getRefreshToken = () => {
    return localStorage.getItem("refreshToken");
};

// Removes stored authentication tokens.
export const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
};