import api from "./api";

// Updates the favorite status of a song for a user
export const updateFavoriteStatus = (songId, isFavorited, userId) => {
  return api.post(
    "/favorite/favorites",
    {
      songId: songId, // Payload: ID of the song to update
      isFavorited: isFavorited, // Payload: New favorite status (true/false)
    },
    {
      params: { userId },
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
};
// Gets all favorite tracks for a user
export const getFavoriteTracksByUserId = (userId) => {
  return api.get(`/favorite/favorites/${userId}`);
};
// Gets all saved tracks for a user
export const getSavedTracks = (userId) => {
  return api.get(`/favorite/favorites/saved/${userId}`);
};
