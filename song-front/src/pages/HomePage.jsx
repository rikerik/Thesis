import React, { useEffect, useState } from "react";
import Navbar from "../components/common/layout/Navbar";
import PlaylistCard from "../components/common/layout/PlaylistCard";
import SongCard from "../components/common/layout/SongCard";
import { getPlaylists, getTracks } from "../api/musicApi";
import { getSavedTracks } from "../api/FavoriteApi";
import { getUserDetailsFromToken } from "../Utils/TokenUtil";

// Component for displaying the HomePage

const HomePage = () => {
  // State variables to store fetched playlists, tracks, saved track IDs, and the loading status
  const [tracks, setTracks] = useState([]);
  const [playlists, setPlaylists] = useState([]);
  const [savedTrackIds, setSavedTrackIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState(""); // Stores the search query from the user input

  // Fetch playlists, tracks, and saved tracks when the component is mounted
  useEffect(() => {
    const fetchPlaylistsAndTracks = async () => {
      try {
        // Fetch playlists
        const playlistResponse = await getPlaylists();
        setPlaylists(playlistResponse.data); // Set playlists in state

        // Fetch tracks
        const trackResponse = await getTracks();
        setTracks(trackResponse.data);

        // Fetch saved tracks
        const userId = getUserDetailsFromToken().userId; // Get user ID from token

        // If the user is logged in, fetch their saved tracks
        if (userId) {
          const savedTracksResponse = await getSavedTracks(userId);
          setSavedTrackIds(savedTracksResponse.data);
        } else {
          console.warn(
            "userId is null or undefined, cannot fetch saved tracks"
          );
        }
      } catch (error) {
        console.error("Error fetching saved tracks:", error);
      } finally {
        // The loading spinner turns off, once the fetching is complete
        setLoading(false);
      }
    };

    // Call the function to fetch playlists and tracks
    fetchPlaylistsAndTracks();
  }, []);

  // Filter playlists based on search query
  const filteredPlaylists = playlists.filter((playlist) => {
    if (searchQuery.trim() === "") {
      return true; // If search is empty, display all playlists
    }
    // Check if the playlist title matches the search query (case-insensitive)
    return playlist.title.toLowerCase().includes(searchQuery.toLowerCase());
  });

  // Filter tracks based on search query (by track title or artist)
  const filteredTracks = tracks.filter((track) => {
    if (searchQuery.trim() === "") {
      return true; // If search is empty, display all tracks
    }
    // Check if the track title or artist matches the search query (case-insensitive)
    return (
      track.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      track.artist.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <div className="content-below-navbar">
      <div className="d-flex flex-column min-vh-100">
        <Navbar searchQuery={searchQuery} onSearchChange={setSearchQuery} />

        <div className="row flex-grow-1 m-1">
          <div className="col-md-12">
            {!loading && (
              <>
                <div className="mt-4">
                  <h5 className="mb-3 text-center">Featured Playlists</h5>
                  <div className="row">
                    {filteredPlaylists.length > 0 ? (
                      filteredPlaylists.map((playlist) => (
                        <div className="col-md-3 mb-3" key={playlist.id}>
                          <PlaylistCard
                            id={playlist.id}
                            title={playlist.title}
                            description={playlist.description}
                            imageUrl={playlist.image}
                          />
                        </div>
                      ))
                    ) : (
                      <div className="text-center">No playlists found</div>
                    )}
                  </div>
                </div>

                <div className="mt-5 mb-5">
                  <h5 className="mb-3 text-center">All Tracks</h5>
                  <div className="row">
                    {filteredTracks.length > 0 ? (
                      filteredTracks.map((track) => (
                        <div className="col-md-2 col-lg-2 mb-5" key={track.id}>
                          <SongCard
                            title={track.title}
                            artist={track.artist}
                            imageUrl={track.base64Image}
                            id={track.id}
                            isFavorited={savedTrackIds.includes(track.id)}
                          />
                        </div>
                      ))
                    ) : (
                      <div className="text-center">No tracks found</div>
                    )}
                  </div>
                </div>
              </>
            )}

            {loading && (
              <div
                className="d-flex justify-content-center align-items-center"
                style={{ height: "200px" }}
              >
                <div className="spinner-border text-info p-lg-4" role="status">
                  <span className="visually-hidden">Loading...</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
