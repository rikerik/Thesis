import React, { useState, useEffect } from "react";
import { getUserById } from "./UserService";
import { useParams } from "react-router-dom";
import ClipLoader from "react-spinners/ClipLoader";

// Component for displaying details of a specific user
const UserDetail = () => {
  const { id } = useParams(); // Get the user ID from the route parameters
  const [user, setUser] = useState(null); // State to store the user details

  // Fetch user details when the component mounts or ID changes
  useEffect(() => {
    fetchUser();
  }, [id]);

  // Function to fetch user details from the API and update state
  const fetchUser = async () => {
    const response = await getUserById(id); // Call the service function with the user ID
    setUser(response.data); // Update state with the fetched user details
  };

  return (
    <div className="container mt-5">
      <h2 className="mb-4">User Detail</h2>
      {user ? (
        <table className="table table-bordered">
          <thead className="thead-dark">
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>First Name</th>
              <th>Last Name</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{user.id}</td>
              <td>{user.username}</td>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
            </tr>
          </tbody>
        </table>
      ) : (
        <div className="d-flex justify-content-center">
          <ClipLoader color="#000" size={50} />
        </div>
      )}
    </div>
  );
};

export default UserDetail;
