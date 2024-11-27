import React, { useState, useEffect } from "react";
import Navbar from "../components/common/layout/Navbar";
import { updateUserProfile } from "../services/ProfileService";
import { getUserDetailsFromToken } from "../Utils/TokenUtil";
import {
  getWeeklyListeningTime,
  getFavoriteArtists,
} from "../api/ListeningData";
import "bootstrap/dist/css/bootstrap.min.css";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from "chart.js"; //Chart.js libary for rendering charts

ChartJS.register(CategoryScale, LinearScale, BarElement, Title); // Register necessary Chart.js components

const ProfilePage = () => {
  // State to store profile data (first name, last name, etc.)
  const [profileData, setProfileData] = useState({
    firstName: "",
    lastName: "",
    password: "",
    userName: "",
    email: "",
  });

  // State to store profile data (first name, last name, etc.)
  const [weeklyListeningTime, setWeeklyListeningTime] = useState(0);
  const [favoriteArtists, setFavoriteArtists] = useState([]);

  // useEffect hook to fetch user data (listening time and favorite artists) when the component mounts
  useEffect(() => {
    const fetchData = async () => {
      try {
        const userId = getUserDetailsFromToken().userId;
        console.log(`Fetching data for user ID: ${userId}`);

        const time = await getWeeklyListeningTime(userId);
        setWeeklyListeningTime(time);

        const artists = await getFavoriteArtists(userId);
        setFavoriteArtists(artists.slice(0, 3));
      } catch (error) {
        console.error("Failed to fetch profile data", error);
      }
    };

    fetchData();
  }, []);

  // Handle changes to input fields (first name, last name, etc.)
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfileData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(); // FormData to handle file upload
    if (profileData.firstName)
      formData.append("firstName", profileData.firstName);
    if (profileData.lastName) formData.append("lastName", profileData.lastName);
    if (profileData.password) formData.append("password", profileData.password);
    if (profileData.userName) formData.append("userName", profileData.userName);
    if (profileData.email) formData.append("email", profileData.email);

    const userId = getUserDetailsFromToken().userId;
    try {
      await updateUserProfile(userId, formData);
      alert("Profile updated successfully");
    } catch (error) {
      console.error("Error updating profile:", error);
      alert("An error occurred while updating your profile");
    }
  };

  // Prepare data for the chart showing favorite artists
  const artistNames = favoriteArtists.map((artist) => artist[0]); // Extract artist names
  const artistTimes = favoriteArtists.map((artist) => artist[1] / 60); // Convert artist listening time to minutes

  // Data to be used in the bar chart
  const chartData = {
    labels: artistNames, // X-axis labels
    datasets: [
      {
        label: "Minutes Listened", // Label for the dataset
        data: artistTimes, // Y-axis data
        backgroundColor: ["rgba(75, 192, 192, 0.6)"], // Bar color
      },
    ],
  };

  return (
    <div className="content-below-navbar">
      <div className="d-flex flex-column min-vh-100">
        <Navbar />
        <div className="row flex-grow-1 m-3 justify-content-center">
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header text-center">
                <h2>Edit Profile</h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-control"
                      name="userName"
                      value={profileData.userName}
                      onChange={handleInputChange}
                      placeholder="Enter your username"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">First Name</label>
                    <input
                      type="text"
                      className="form-control"
                      name="firstName"
                      value={profileData.firstName}
                      onChange={handleInputChange}
                      placeholder="Enter your first name"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Last Name</label>
                    <input
                      type="text"
                      className="form-control"
                      name="lastName"
                      value={profileData.lastName}
                      onChange={handleInputChange}
                      placeholder="Enter your last name"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-control"
                      name="password"
                      value={profileData.password}
                      onChange={handleInputChange}
                      placeholder="Enter new password"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Email</label>
                    <input
                      type="text"
                      className="form-control"
                      name="email"
                      value={profileData.email}
                      onChange={handleInputChange}
                      placeholder="Enter new email"
                    />
                  </div>
                  <div className="d-grid">
                    <button type="submit" className="btn btn-primary">
                      Save
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header text-center">
                <h3>Your Listening Statistics</h3>
              </div>
              <div className="card-body">
                <p>
                  <strong>Weekly Listening Time:</strong> {weeklyListeningTime}{" "}
                  minutes
                </p>
                <div className="progress mb-3">
                  <div
                    className="progress-bar"
                    role="progressbar"
                    style={{
                      width: `${(weeklyListeningTime / 600) * 100}%`,
                    }}
                    aria-valuenow={weeklyListeningTime}
                    aria-valuemin="0"
                    aria-valuemax="600"
                  ></div>
                </div>

                <h4>Favorite Artists</h4>
                <Bar data={chartData} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
