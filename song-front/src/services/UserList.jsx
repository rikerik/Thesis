import React, { useEffect, useState } from "react";
import { getAllUsers } from "./UserService";

// Component for displaying a list of users
const UserList = () => {
  const [users, setUsers] = useState([]); // State to store the list of users

  // Fetch users when the component mounts
  useEffect(() => {
    fetchUsers();
  }, []);

  // Function to fetch users from the API and update state
  const fetchUsers = async () => {
    try {
      const response = await getAllUsers();

      // Parse the JSON string if the response data is a string
      let usersData = response.data;
      if (typeof usersData === "string") {
        usersData = JSON.parse(usersData); // Parse the string to an array
      }

      if (Array.isArray(usersData)) {
        setUsers(usersData);
      } else {
        console.error("Expected an array of users but got:", usersData);
      }
    } catch (error) {
      console.error("Failed to fetch users:", error);
    }
  };

  return (
    <div className="container mt-5">
      <h2 className="mb-4">User List</h2>
      <table className="table table-bordered">
        <thead className="thead-dark">
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.username}</td>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
              <td>
                <button className="btn btn-primary btn-sm me-2">Edit</button>
                <button className="btn btn-danger btn-sm">Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserList;
