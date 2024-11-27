import api from "./api";

//Function to fetch all playlists
export const getPlaylists = () => api.get("/music/playlists");

// Function to fetch all tracks
export const getTracks = () => api.get("/music/tracks");

// Retrieves tracks by playlist ID
export const getTracksByPlaylistId = (playlistId) =>
  api.get(`/music/tracks/playlist/${playlistId}`);

// Function to fetch a specific track by its id
export const getTrackById = (id) =>
  api.get(`/music/tracks/${id}`, { responseType: "blob" });
