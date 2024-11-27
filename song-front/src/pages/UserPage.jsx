import React from "react";
import { Route, Routes } from "react-router-dom";
import UserList from "../services/UserList";
import UserDetail from "../services/UserDetail";
import NavBar from "../components/common/layout/Navbar";

const UserPage = () => {
  return (
    <div>
      <NavBar />

      {/* Routes for User Pages */}
      <Routes>
        <Route path="/" element={<UserList />} />
        <Route path="/:id" element={<UserDetail />} />
      </Routes>
    </div>
  );
};

export default UserPage;
