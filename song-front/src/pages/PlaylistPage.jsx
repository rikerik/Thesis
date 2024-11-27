import React, { useEffect, useState } from "react";
import { getTracksByPlaylistId } from "../api/musicApi";
import SongCard from "../components/common/layout/SongCard";
import Navbar from "../components/common/layout/Navbar";

// Component for rendering the PlaylistPage

const PlaylistPage = ({ playlistId }) => {
  const [tracks, setTracks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!playlistId) {
      console.error("Playlist ID is missing"); // Log an error if no playlist ID is provided
      return;
    }

    const fetchTracks = async () => {
      try {
        // Fetch the tracks for the playlist by playlist ID
        const response = await getTracksByPlaylistId(playlistId);
        setTracks(response.data);
      } catch (error) {
        console.error("Error fetching tracks for playlist:", error);
      } finally {
        // Set loading to false
        setLoading(false);
      }
    };

    fetchTracks(); // Call the function to fetch tracks
  }, [playlistId]); // Dependency on playlistId to refetch tracks when the ID changes

  return (
    <div className="content-below-navbar">
      <div className="d-flex flex-column min-vh-100">
        <Navbar />

        <div className="row flex-grow-1 m-1">
          <div className="col-md-12">
            <div className="mt-4">
              <div className="row"></div>
            </div>

            <div className="row">
              {loading ? (
                <div
                  className="d-flex justify-content-center align-items-center"
                  style={{ height: "200px" }}
                >
                  <div
                    className="spinner-border text-info p-lg-4"
                    role="status"
                  >
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              ) : (
                tracks.map((track) => (
                  <div className="col-md-2 col-lg-2 mb-5" key={track.id}>
                    <SongCard
                      title={track.title}
                      artist={track.artist}
                      imageUrl={track.base64Image}
                      id={track.id}
                    />
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PlaylistPage;
