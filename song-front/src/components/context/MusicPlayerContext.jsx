import React, {
  createContext,
  useState,
  useContext,
  useEffect,
  useMemo,
} from "react";
import { getTrackById, getTracks } from "../../api/musicApi";
import { getUserDetailsFromToken } from "../../Utils/TokenUtil";
import { sendListeningData } from "../../api/ListeningData";

// Create a Context for the Music Player, allowing components to access music player state
const MusicPlayerContext = createContext();

// Custom hook to use the MusicPlayerContext
export const useMusicPlayer = () => useContext(MusicPlayerContext);

// Provider component to wrap around parts of the app that need access to music player context
export const MusicPlayerProvider = ({ children }) => {
  const [currentTrack, setCurrentTrack] = useState(null); // Currently playing track
  const [isPlaying, setIsPlaying] = useState(false); // Whether the track is playing
  const [audioSrc, setAudioSrc] = useState(null); // URL for the audio source
  const [trackList, setTrackList] = useState([]); // List of tracks fetched from the server
  const [currentTrackIndex, setCurrentTrackIndex] = useState(0); // Index of the current track
  const [shuffle, setShuffle] = useState(false); // Whether shuffle mode is on
  const [listeningStartTime, setListeningStartTime] = useState(null); // When the current track started playing
  const [userId, setUserId] = useState(null); // Store the user's ID

  // Fetch user details from the token when the component mounts
  useEffect(() => {
    const token = sessionStorage.getItem("token"); // Get the token from sessionStorage

    if (token) {
      // Only try to retrieve user details if a token exists
      const userDetails = getUserDetailsFromToken(token);
      if (userDetails) {
        setUserId(userDetails.userId);
      }
    }
  }, []);

  // Function to play a specific track
  const playTrack = async (track) => {
    // Check if the selected track is already playing, just resume it
    if (currentTrack && currentTrack.id === track.id) {
      setIsPlaying(true);
      return;
    }

    // Send listening data for the currently playing track
    if (currentTrack) {
      const listeningDuration = listeningStartTime
        ? Math.floor(Date.now() / 1000) - listeningStartTime
        : 0;

      const listeningData = {
        userId: userId,
        artist: currentTrack.artist,
        duration: listeningDuration,
      };

      console.log(`User ID: ${userId}, Sending listening data:`, listeningData); // Log the data being sent
      await sendListeningData(listeningData);
    }

    // Set start time for the new track
    setListeningStartTime(Math.floor(Date.now() / 1000));

    try {
      // Stop any currently playing track
      setIsPlaying(false);
      // Fetch the track data by its ID
      const response = await getTrackById(track.id);
      // Create a URL for the audio data
      const audioUrl = URL.createObjectURL(response.data);

      // Find the index of the new track in the trackList
      const newIndex = trackList.findIndex((t) => t.id === track.id);

      // Update state with the new track, audio source, and index
      setCurrentTrack(track);
      setAudioSrc(audioUrl);
      setIsPlaying(true);
      setCurrentTrackIndex(newIndex);
    } catch (error) {
      console.error("Error fetching the track:", error);
    }
  };
  // Function to pause the current track
  const pauseTrack = () => {
    setIsPlaying(false);
  };
  // Function to skip to the next track
  const nextTrack = async () => {
    if (trackList.length > 0) {
      // Call sendListeningData for the current track before switching
      if (currentTrack) {
        const listeningDuration = listeningStartTime
          ? Math.floor(Date.now() / 1000) - listeningStartTime
          : 0;

        const listeningData = {
          userId: userId,
          artist: currentTrack.artist,
          duration: listeningDuration,
        };

        console.log(
          `User ID: ${userId}, Sending listening data:`,
          listeningData
        ); // Log the data being sent
        await sendListeningData(listeningData); // Send data to the server
      }

      let nextIndex;
      if (shuffle) {
        // Pick a random track if shuffle mode is enabled
        nextIndex = Math.floor(Math.random() * trackList.length);
      } else {
        // Otherwise, go to the next track in the list
        nextIndex = (currentTrackIndex + 1) % trackList.length;
      }

      // Update the current track index and play the new track
      setCurrentTrackIndex(nextIndex);
      playTrack(trackList[nextIndex]);
    }
  };

  // Function to jump back to the previous track
  const previousTrack = async () => {
    if (trackList.length > 0) {
      // Send listening data for the current track before switching
      if (currentTrack) {
        const listeningDuration = listeningStartTime
          ? Math.floor(Date.now() / 1000) - listeningStartTime
          : 0;

        const listeningData = {
          userId: userId,
          artist: currentTrack.artist,
          duration: listeningDuration,
        };

        console.log(
          `User ID: ${userId}, Sending listening data:`,
          listeningData
        ); // Log the data being sent
        await sendListeningData(listeningData);
      }
      // Calculate the index for the previous track
      const prevIndex =
        (currentTrackIndex - 1 + trackList.length) % trackList.length;
      setCurrentTrackIndex(prevIndex);
      playTrack(trackList[prevIndex]);
    }
  };

  // Toggle shuffle mode on/off
  const toggleShuffle = () => {
    setShuffle(!shuffle);
  };

  // Fetch the list of tracks when the component mounts
  useEffect(() => {
    const fetchTracks = async () => {
      try {
        const response = await getTracks();
        setTrackList(response.data.sort((a, b) => a.id - b.id)); // Sort tracks by ID
      } catch (error) {
        console.error("Error fetching tracks:", error);
      }
    };

    fetchTracks();
  }, []);

  // Memoize the value object to avoid unnecessary re-renders of consumers
  const value = useMemo(
    () => ({
      currentTrack,
      currentTrackId: currentTrack ? currentTrack.id : null,
      isPlaying,
      playTrack,
      pauseTrack,
      nextTrack,
      previousTrack,
      audioSrc,
      shuffle,
      toggleShuffle,
    }),
    [
      currentTrack,
      isPlaying,
      playTrack,
      pauseTrack,
      nextTrack,
      previousTrack,
      audioSrc,
      shuffle,
    ]
  );

  return (
    // Provide the context value to child components
    <MusicPlayerContext.Provider value={value}>
      {children}
    </MusicPlayerContext.Provider>
  );
};
