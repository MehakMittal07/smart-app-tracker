import axios from "axios";

// const api = axios.create({
//   baseURL: "http://localhost:8085",
// });
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8085",
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("jwt");
  console.log("API request to:", config.url, "| Token present:", !!token);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error("API error:", err.config?.url, err.response?.status, err.response?.data);
    if (err.response?.status === 401) {
      sessionStorage.removeItem("jwt");
      window.location.href = "/login?error=session_expired";
    }
    return Promise.reject(err);
  }
);

export const opportunityApi = {
  getAll:       ()             => api.get("/api/opportunities"),
  filter:       (type, status) => api.get("/api/opportunities/filter", { params: { type, status } }),
  search:       (keyword)      => api.get("/api/opportunities/search", { params: { keyword } }),
  updateStatus: (id, status)   => api.patch(`/api/opportunities/${id}/status`, { status }),
  delete:       (id)           => api.delete(`/api/opportunities/${id}`),
  syncGmail:    ()             => api.post("/api/gmail/sync"),
};

export default api;