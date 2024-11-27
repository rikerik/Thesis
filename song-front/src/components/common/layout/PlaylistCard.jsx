import React from "react";
import { useNavigate } from "react-router-dom";
import "../../../styles/PlaylistCard.css";

const PlaylistCard = ({ id, title, description, imageUrl }) => {
  //Hook for navigation
  const navigate = useNavigate();

  // Function to handle card click and navigate to the playlist page
  const handleClick = () => {
    navigate(`/playlist/${id}`);
  };

  return (
    <div className="playlist-card" onClick={handleClick}>
      <div className="playlist-card-image-wrapper">
        <img src={imageUrl} alt={title} className="playlist-card-image" />
      </div>
      <div className="playlist-card-content">
        <h6>{title}</h6>
        <p>{description}</p>
      </div>
    </div>
  );
};

export default PlaylistCard;
