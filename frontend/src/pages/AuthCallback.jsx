import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

export default function AuthCallback() {
  const navigate = useNavigate();
  const processed = useRef(false); // prevent double execution

  useEffect(() => {
    if (processed.current) return; // StrictMode guard
    processed.current = true;

    const fullUrl = window.location.href;
    console.log("AuthCallback URL:", fullUrl);

    // Extract token via regex — most reliable method
    const match = fullUrl.match(/[?&]token=([^&]+)/);
    const token = match ? match[1] : null;

    console.log("Token found:", token ? "YES, length=" + token.length : "NO");

    if (!token || token.split(".").length !== 3) {
      console.error("No valid token in URL");
      navigate("/login?error=no_token", { replace: true });
      return;
    }

    sessionStorage.setItem("jwt", token);
    console.log("Token stored successfully");
    navigate("/dashboard", { replace: true });

  }, []); // empty deps — run once only

  return (
    <div style={{
      minHeight: "100vh", display: "flex", alignItems: "center",
      justifyContent: "center", flexDirection: "column", gap: 16,
      fontFamily: "sans-serif", background: "#f5f5f5"
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: "50%",
        border: "3px solid #3b82f6", borderTopColor: "transparent",
        animation: "spin 0.8s linear infinite"
      }}/>
      <p style={{ color: "#666" }}>Signing you in...</p>
      <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
    </div>
  );
}