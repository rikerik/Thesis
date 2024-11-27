import api from "../api/api";

// Updates user profile with form data
export const updateUserProfile = (userId, formData) =>
  api.put(`/profile/update/${userId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
