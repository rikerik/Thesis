import api from "./api";

// Sends listening data to the server
export const sendListeningData = async (data) => {
  try {
    const response = await api.post("/listening/statistics", data);
    return response.data;
  } catch (error) {
    console.error("Error sending listening data:", error);
    throw error;
  }
};
// Gets the weekly listening time for a user
export const getWeeklyListeningTime = async (userId) => {
  try {
    const response = await api.get(
      `/listening/users/${userId}/listening-time/week`
    );
    console.log(userId);
    return response.data;
  } catch (error) {
    console.error("Error fetching listening time:", error);
    throw error;
  }
};
// Gets the favorite artists for a user
export const getFavoriteArtists = async (userId) => {
  try {
    const response = await api.get(
      `/listening/users/${userId}/favorite-artists`
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching top artists:", error);
    throw error;
  }
};
