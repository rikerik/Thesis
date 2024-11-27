import React, { useState } from "react";
import { Link } from "react-router-dom";
import { register } from "../services/AuthService";
import "../styles/Form.css";

// Component for handling the user registration form
const RegisterForm = () => {
  // State variables to store user input
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [lastName, setLastName] = useState("");
  const [firstName, setFirstName] = useState("");

  // Function to handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent the default form submission
    try {
      // Make a call to the register function with the entered user data
      const response = await register({
        username,
        email,
        password,
        firstName,
        lastName,
      });

      // If registration is successful, store the token in sessionStorage and redirect to the home page
      if (response.status === 200) {
        const { token } = response.data;
        sessionStorage.setItem("token", token); //Save token
        window.location.href = "/home"; // Redirect to the home page after successful registration
      } else {
        console.error("Registration failed", response.status);
      }
    } catch (error) {
      console.error("Registration failed:", error);
    }
  };

  return (
    <div className="form-container">
      <div className="card shadow p-4 mt-5">
        <h2 className="mb-4 text-center">Register</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">
              Username
            </label>
            <input
              type="text"
              className="form-control"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="email" className="form-label">
              Email:
            </label>
            <input
              type="text"
              className="form-control"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="firstName" className="form-label">
              First Name
            </label>
            <input
              type="text"
              className="form-control"
              id="firstName"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="lastName" className="form-label">
              Last Name
            </label>
            <input
              type="text"
              className="form-control"
              id="lastName"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="password" className="form-label">
              Password
            </label>
            <input
              type="password"
              className="form-control"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary w-100">
            Register
          </button>
        </form>
        <div className="mt-3 text-center">
          Have an account? <Link to="/">Log in now</Link>.
        </div>
      </div>
    </div>
  );
};

export default RegisterForm;
