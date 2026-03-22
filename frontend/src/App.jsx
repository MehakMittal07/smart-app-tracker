import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AuthCallback from "./pages/AuthCallback";
import Dashboard from "./pages/Dashboard";

function PrivateRoute({ children }) {
  const token = sessionStorage.getItem("jwt");
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login"         element={<Login />} />
      <Route path="/auth/callback" element={<AuthCallback />} />
      <Route path="/dashboard"     element={
        <PrivateRoute><Dashboard /></PrivateRoute>
      }/>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
// ```

// ---

// ## Also clear browser storage before testing

// Open Chrome DevTools → Application tab → Storage → click "Clear site data" for `localhost:5173`. Old failed attempts leave stale state that confuses the flow.

// ---

// After these three changes — removing StrictMode, adding the `useRef` guard, and clearing browser storage — the flow will be:
// ```
// Google login completes
//     → /auth/callback?token=eyJ...   (first and only run)
//     → token stored in sessionStorage
//     → navigate to /dashboard
//     → Dashboard loads with JWT ✓