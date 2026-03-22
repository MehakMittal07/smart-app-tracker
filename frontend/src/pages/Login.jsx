export default function Login() {
  const handleLogin = () => {
    window.location.href = "http://localhost:8085/oauth2/authorization/google";
  };

  const params = new URLSearchParams(window.location.search);
  const error  = params.get("error");

  return (
    <div style={S.page}>
      <div style={S.left}>
        <div style={S.leftContent}>
          <div style={S.badge}>✦ Smart Application Tracker</div>
          <h1 style={S.headline}>
            Never miss an<br />
            <span style={S.accent}>opportunity</span><br />
            again.
          </h1>
          <p style={S.desc}>
            Automatically scans your Gmail for internships, certifications, and hackathons.
            Tracks deadlines. Sends reminders. All in one place.
          </p>
          <div style={S.features}>
            {["📧  Gmail integration", "📅  Deadline tracking", "🔔  Smart reminders", "⚡  Instant sync"].map(f => (
              <div key={f} style={S.feature}>{f}</div>
            ))}
          </div>
        </div>
      </div>

      <div style={S.right}>
        <div style={S.card}>
          <div style={S.cardHeader}>
            <div style={S.logoMark}>⚡</div>
            <h2 style={S.cardTitle}>Sign in</h2>
            <p style={S.cardSub}>Connect your Gmail to get started</p>
          </div>

          {error && (
            <div style={S.errorBox}>
              {error === "gmail_reauth_required"
                ? "Gmail access expired. Please sign in again."
                : "Something went wrong. Please try again."}
            </div>
          )}

          <button style={S.googleBtn} onClick={handleLogin}>
            <GoogleIcon />
            <span>Continue with Google</span>
          </button>

          <p style={S.disclaimer}>
            By signing in, you authorize read-only access to your Gmail
            to scan for opportunities. We never send emails on your behalf.
          </p>
        </div>
      </div>

      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&family=Space+Grotesk:wght@600;700&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { background: #0f172a; }
        button:hover { opacity: 0.92; transform: translateY(-1px); }
        button:active { transform: translateY(0); }
        button { transition: all 0.15s; }
      `}</style>
    </div>
  );
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 48 48" style={{ flexShrink:0 }}>
      <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
      <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
      <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
      <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
    </svg>
  );
}

const S = {
  page:       { display:"flex", minHeight:"100vh", fontFamily:"'DM Sans', sans-serif" },
  left:       { flex:1, background:"linear-gradient(135deg,#0f172a 0%,#1e1b4b 50%,#0f172a 100%)", padding:"60px 80px", display:"flex", alignItems:"center", position:"relative", overflow:"hidden" },
  leftContent:{ position:"relative", zIndex:1 },
  badge:      { display:"inline-block", fontSize:12, fontWeight:600, color:"#818cf8", background:"rgba(99,102,241,0.15)", border:"1px solid rgba(99,102,241,0.3)", padding:"6px 14px", borderRadius:20, marginBottom:32, letterSpacing:"0.04em" },
  headline:   { fontSize:56, fontWeight:700, color:"#f1f5f9", lineHeight:1.1, fontFamily:"'Space Grotesk', sans-serif", marginBottom:24, letterSpacing:"-1.5px" },
  accent:     { color:"#818cf8" },
  desc:       { fontSize:16, color:"#94a3b8", lineHeight:1.7, marginBottom:40, maxWidth:420 },
  features:   { display:"flex", flexDirection:"column", gap:12 },
  feature:    { fontSize:14, color:"#cbd5e1", fontWeight:400 },
  right:      { width:480, background:"#fff", display:"flex", alignItems:"center", justifyContent:"center", padding:40 },
  card:       { width:"100%", maxWidth:380 },
  cardHeader: { textAlign:"center", marginBottom:32 },
  logoMark:   { fontSize:32, marginBottom:16 },
  cardTitle:  { fontSize:28, fontWeight:700, color:"#0f172a", fontFamily:"'Space Grotesk', sans-serif", marginBottom:8 },
  cardSub:    { fontSize:14, color:"#94a3b8" },
  errorBox:   { background:"#fef2f2", border:"1px solid #fecaca", borderRadius:10, padding:"12px 16px", fontSize:13, color:"#dc2626", marginBottom:20, textAlign:"center" },
  googleBtn:  { display:"flex", alignItems:"center", justifyContent:"center", gap:12, width:"100%", padding:"14px 24px", border:"1.5px solid #e2e8f0", borderRadius:12, background:"#fff", fontSize:15, fontWeight:500, color:"#1e293b", cursor:"pointer", marginBottom:24, boxShadow:"0 1px 4px rgba(0,0,0,0.06)" },
  disclaimer: { fontSize:11, color:"#94a3b8", textAlign:"center", lineHeight:1.6 },
};

