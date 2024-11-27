import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import UserPage from "./pages/UserPage";
import HomePage from "./pages/HomePage";
import { MusicPlayerProvider } from "./components/context/MusicPlayerContext";
import MusicController from "./components/Music/MusicController";
import PlaylistContainer from "./components/Music/PlaylistContainer";
import LibraryPage from "./pages/LibraryPage";
import ProfilePage from "./pages/ProfilePage";

// Component to conditionally render MusicController
const ConditionalMusicController = () => {
  const location = useLocation();
  const shouldShowMusicController = !(
    location.pathname === "/" || location.pathname === "/register"
  );

  return shouldShowMusicController ? <MusicController /> : null;
};

function App() {
  return (
    <Router>
      <MusicPlayerProvider>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/users" element={<UserPage />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/playlist/:id" element={<PlaylistContainer />} />
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Routes>
        <ConditionalMusicController />
      </MusicPlayerProvider>
    </Router>
  );
}

export default App;
