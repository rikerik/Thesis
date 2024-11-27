import { jwtDecode } from "jwt-decode";

export const getUserDetailsFromToken = () => {
  const token = sessionStorage.getItem("token");
  if (token) {
    const decodedToken = jwtDecode(token);
    return {
      userId: decodedToken.UserId,
      username: decodedToken.sub, //Username is in subject claim
    };
  }
  return null;
};
