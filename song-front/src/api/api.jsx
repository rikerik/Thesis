import axios from "axios";

// Create an instance of axios with custom configuration
const api = axios.create({
  baseURL: "http://localhost:8080", //Base url for all requests
  headers: {
    "Content-Type": "application/json", //Default content type for requests
  },
});

const token = sessionStorage.getItem("token"); // Get token from local storage

// Add a request interceptor to include an authorization token if it exists
api.interceptors.request.use((config) => {
  if (token) {
    //If a token exists, add it to the Authorization header
    config.headers["Authorization"] = `Bearer ${token}`; // Add token to headers
  }
  //return the updated config
  return config;
});

export default api;
