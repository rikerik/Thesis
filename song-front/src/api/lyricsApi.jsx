import api from "./api";

// Retrieves lyrics based on the provided id.

export const getLyrics = (id) => {
  return api.get("/lyrics/getLyrics", {
    params: {
      id: id,
    },
  });
};
