import api from "../api/api";

// Fetch all users from the API
export const getAllUsers = () => api.get("/users");
// Fetch a user by their ID from the API
export const getUserById = (id) => api.get(`/users/${id}`);
