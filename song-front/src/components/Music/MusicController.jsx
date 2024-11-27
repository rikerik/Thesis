import React, { useRef, useState, useEffect } from "react";
import { useMusicPlayer } from "../context/MusicPlayerContext";
import { Modal, Button, Spinner } from "react-bootstrap";
import { FiMusic, FiShuffle } from "react-icons/fi";
import { FaPlay, FaPause, FaStepForward, FaStepBackward } from "react-icons/fa";
import { getLyrics } from "../../api/lyricsApi";
import Visualizer from "./Visualizer";

// The MusicController component controls playback, volume, and displays track info
const MusicController = () => {
  // audioRef is a reference to the audio element to manage playback
  const audioRef = useRef(null);

  // Destructure various state values and functions from MusicPlayerContext
  const {
    currentTrack, // Current track being played
    isPlaying, // Boolean indicating if a track is playing
    playTrack, // Function to play the current track
    pauseTrack, // Function to pause the current track
    audioSrc, // Audio source for the current track
    nextTrack, // Function to skip to the next track
    previousTrack, // Function to go back to the previous track
    shuffle, // Boolean indicating shuffle state
    toggleShuffle, // Function to toggle shuffle mode
  } = useMusicPlayer();

  // Component state for volume, current playback time, track duration, lyrics, etc
  const [volume, setVolume] = useState(0.5); // Audio volume
  const [currentTime, setCurrentTime] = useState(0); // Current playback time
  const [duration, setDuration] = useState(0); // Duration of the track
  const [showLyrics, setShowLyrics] = useState(false); // State to control lyrics modal visibility
  const [lyrics, setLyrics] = useState(""); // Lyrics of the current track
  const [loading, setLoading] = useState(false); // Loading state for lyrics fetch

  // Effect to handle audio playback when play/pause and track change happens
  useEffect(() => {
    const audio = audioRef.current;

    if (audio) {
      // Set the volume for the audio element
      audio.volume = volume;

      // Load a new track if the audioSrc changes
      if (audioSrc !== audio.src) {
        audio.src = audioSrc;
        audio.load();
        if (isPlaying) {
          // Play the track if the isPlaying flag is true
          audio
            .play()
            .catch((error) => console.error("Playback error:", error));
        }
      } else if (isPlaying) {
        audio.play().catch((error) => console.error("Playback error:", error));
      } else {
        audio.pause();
      }
      // Event listener to move to the next track when the current one ends
      const handleEnded = () => {
        nextTrack();
      };
      audio.addEventListener("ended", handleEnded);

      // Cleanup the event listener when component unmounts
      return () => {
        audio.removeEventListener("ended", handleEnded);
      };
    }
  }, [isPlaying, audioSrc, volume, nextTrack]);

  // Effect to handle updating the currentTime and duration when the audio plays or loads
  useEffect(() => {
    const audio = audioRef.current;

    if (audio) {
      // Updates currentTime as the track progresses

      const updateTime = () => setCurrentTime(audio.currentTime);
      // Sets the audio data (duration, current time) when the track loads
      const setAudioData = () => {
        setDuration(audio.duration);
        setCurrentTime(audio.currentTime);
      };

      // Event listeners for time updates and track data loading
      audio.addEventListener("timeupdate", updateTime);
      audio.addEventListener("loadeddata", setAudioData);

      // Cleanup event listeners on component unmount
      return () => {
        audio.removeEventListener("timeupdate", updateTime);
        audio.removeEventListener("loadeddata", setAudioData);
      };
    }
  }, [audioSrc]);

  // Handle changes to the volume slider.
  const handleVolumeChange = (e) => {
    setVolume(parseFloat(e.target.value));
  };

  // Handle seeking within the track using the progress bar
  const handleSeek = (e) => {
    const seekTime = parseFloat(e.target.value);
    if (audioRef.current) {
      audioRef.current.currentTime = seekTime;
      setCurrentTime(seekTime);
    }
  };

  // Handle showing the lyrics modal and fetching lyrics data.
  const handleShowLyrics = async () => {
    setShowLyrics(true);
    setLoading(true);

    try {
      if (currentTrack?.id) {
        const response = await getLyrics(currentTrack.id);
        if (response.data) {
          setLyrics(response.data || "Lyrics not available.");
        } else {
          setLyrics("Lyrics not available.");
        }
      }
    } catch (error) {
      console.error("Error fetching lyrics:", error);
      setLyrics("Lyrics not available.");
    } finally {
      setLoading(false);
    }
  };
  // Close the lyrics modal.
  const handleCloseLyrics = () => setShowLyrics(false);

  return (
    <div className="music-controller bg-dark text-light d-flex justify-content-between align-items-center p-1 fixed-bottom">
      {currentTrack && (
        <>
          <div className="track-info">
            <p className="mb-0">
              {currentTrack.title} - {currentTrack.artist}
            </p>
          </div>

          {/* Visualizer */}
          <Visualizer audioRef={audioRef} />
          <audio ref={audioRef} />
          <div className="controls d-flex align-items-center">
            <button onClick={previousTrack} className="btn btn-light mx-2">
              <FaStepBackward size={24} />
            </button>
            <button
              onClick={() => {
                isPlaying ? pauseTrack() : playTrack(currentTrack);
              }}
              className="btn btn-light mx-2"
            >
              {isPlaying ? <FaPause size={24} /> : <FaPlay size={24} />}
            </button>
            <button onClick={nextTrack} className="btn btn-light mx-2">
              <FaStepForward size={24} />
            </button>

            {/* Shuffle Button */}
            <FiShuffle
              className="mx-2"
              size={24}
              style={{ cursor: "pointer", color: shuffle ? "green" : "white" }}
              onClick={toggleShuffle}
            />

            <input
              type="range"
              min="0"
              max={duration || 1}
              value={currentTime}
              onChange={handleSeek}
              className="mx-2"
              style={{ flex: 1 }}
            />
            {/* Lyrics Icon */}
            <FiMusic
              className="mx-2"
              size={24}
              style={{ cursor: "pointer" }}
              onClick={handleShowLyrics}
            />
            <div className="time-info mx-2">
              <span>{formatTime(currentTime)}</span> /{" "}
              <span>{formatTime(duration)}</span>
            </div>
            <input
              type="range"
              min="0"
              max="1"
              step="0.01"
              value={volume}
              onChange={handleVolumeChange}
              className="mx-2"
              style={{ width: "100px" }}
            />
          </div>

          {/* Modal for Lyrics */}
          <Modal
            show={showLyrics}
            onHide={handleCloseLyrics}
            size="lg"
            className="custom-modal"
            centered
            scrollable
          >
            <Modal.Header closeButton>
              <Modal.Title>Lyrics</Modal.Title>
            </Modal.Header>
            <Modal.Body className="modal-lyrics-body">
              {loading ? (
                <div className="d-flex justify-content-center">
                  <Spinner animation="border" role="status">
                    <span className="sr-only"></span>
                  </Spinner>
                </div>
              ) : (
                <div className="lyrics-content">{lyrics}</div>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={handleCloseLyrics}>
                Close
              </Button>
            </Modal.Footer>
          </Modal>
        </>
      )}
    </div>
  );
};

const formatTime = (time) => {
  const minutes = Math.floor(time / 60);
  const seconds = Math.floor(time % 60);
  return `${minutes}:${seconds < 10 ? "0" : ""}${seconds}`;
};

export default MusicController;
