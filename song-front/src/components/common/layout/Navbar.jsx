import React, { useState } from "react";
import { Navbar, Nav, Form, FormControl, Dropdown } from "react-bootstrap";
import LogoutButton from "./Logout";
import { Link, useLocation } from "react-router-dom";
import { FaHome, FaBook, FaUser, FaComments } from "react-icons/fa";
import "../../../styles/NavBar.css";
import ChatWindow from "./ChatWindow";
const logoSrc = "/logo.png";

const NavBar = ({ searchQuery, onSearchChange, onSearchSubmit }) => {
  const location = useLocation(); // Hook to get the current location (URL path)
  const isHomePage = location.pathname === "/home"; // Check if on the homepage

  const [showChats, setShowChats] = useState(false); // State to control the visibility of the chat dropdown menu
  const [openChats, setOpenChats] = useState([]); // State to track opened chat windows

  // Function for opening window for a specific chatgroup
  const openChatWindow = (chatGroup) => {
    if (!openChats.includes(chatGroup)) {
      setOpenChats([...openChats, chatGroup]); // Add chat group to openChats state
    }
  };

  // Function for closing window
  const closeChatWindow = (chatGroup) => {
    setOpenChats(openChats.filter((chat) => chat !== chatGroup)); // Remove chat group from openChats state
  };

  return (
    <>
      <Navbar
        bg="dark"
        variant="dark"
        expand="lg"
        className="navbar navbar-custom fixed-top"
      >
        <Navbar.Brand className="d-flex align-items-center ms-1">
          <img src={logoSrc} alt="Logo" className="circular-logo" />
          <span className="gradient-text">TuneWave</span>
        </Navbar.Brand>

        <Nav className="ms-auto">
          <Nav.Link
            as={Link}
            to="/home"
            className="nav-link nav-link-orange nav-link-large"
          >
            <FaHome className="react-icons" title="Home" />
          </Nav.Link>
          <Nav.Link
            as={Link}
            to="/library"
            className="nav-link nav-link-orange nav-link-large"
          >
            <FaBook className="react-icons" title="Library" />
          </Nav.Link>
          <Nav.Link
            as={Link}
            to="/profile"
            className="nav-link nav-link-orange nav-link-large"
          >
            <FaUser className="react-icons" title="Profile" />
          </Nav.Link>
        </Nav>

        <Nav className="mx-auto d-flex justify-content-center align-items-center">
          <div className="search-container mx-1">
            {/* Search Form */}
            <Form className="d-flex" onSubmit={onSearchSubmit}>
              <FormControl
                type="text"
                className={`me-2 search-bar ${!isHomePage ? "disabled" : ""}`}
                placeholder="Ready to set the mood?"
                value={searchQuery}
                onChange={
                  isHomePage ? (e) => onSearchChange(e.target.value) : undefined
                } // Disable input on non-homepage
              />
              {!isHomePage && <div className="cordon-effect" />}{" "}
            </Form>
          </div>
          <Dropdown
            onMouseEnter={() => setShowChats(true)}
            onMouseLeave={() => setShowChats(false)}
            show={showChats}
            className="nav-link nav-link-large"
          >
            <Dropdown.Toggle
              as="div"
              id="chat-dropdown"
              className="nav-link chat-dropdown"
            >
              <FaComments className="react-icons" title="Chats" />
            </Dropdown.Toggle>

            <Dropdown.Menu align="end" className="chat-groups-dropdown">
              <Dropdown.Item onClick={() => openChatWindow("Workout")}>
                Workout
              </Dropdown.Item>
              <Dropdown.Item onClick={() => openChatWindow("Focus")}>
                Focus
              </Dropdown.Item>
              <Dropdown.Item onClick={() => openChatWindow("Chill vibes")}>
                Chill vibes
              </Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </Nav>

        <Nav className="ms-auto">
          <LogoutButton className="btn-logout" />
        </Nav>
      </Navbar>

      <div className="chat-windows-container">
        {openChats.map((chatGroup) => (
          <ChatWindow
            key={chatGroup}
            chatGroup={chatGroup}
            onClose={closeChatWindow}
          />
        ))}
      </div>
    </>
  );
};

export default NavBar;
