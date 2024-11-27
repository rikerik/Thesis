import React from "react";
import { useParams } from "react-router-dom";
import PlaylistPage from "../../pages/PlaylistPage";
// This functional component acts as a container for displaying a specific playlist
const PlaylistContainer = () => {
  const { id } = useParams(); // Extract playlist ID from the URL parameters

  // Render the PlaylistPage component, passing the extracted playlistId as a prop
  return <PlaylistPage playlistId={id} />;
};

export default PlaylistContainer;
