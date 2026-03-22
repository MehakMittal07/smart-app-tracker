import { useState } from "react";
import { opportunityApi } from "../services/api";

export default function SyncButton({ onSynced }) {
  const [syncing, setSyncing] = useState(false);
  const [result,  setResult]  = useState(null);

  const handleSync = async () => {
    setSyncing(true); setResult(null);
    try {
      const res = await opportunityApi.syncGmail();
      const { found, message } = res.data;
      setResult({ ok: true, msg: found > 0 ? `${found} new found!` : "Already up to date" });
      if (found > 0) onSynced();
    } catch (e) {
      const data = e.response?.data;
      if (e.response?.status === 401 && data?.action === "please_relogin") {
        setResult({ ok: false, msg: "Re-login required..." });
        setTimeout(() => { sessionStorage.removeItem("jwt"); window.location.href = "/login"; }, 1800);
      } else {
        setResult({ ok: false, msg: data?.message || "Sync failed" });
      }
    } finally {
      setSyncing(false);
      setTimeout(() => setResult(null), 4000);
    }
  };

  return (
    <div style={{ display:"flex", alignItems:"center", gap:10 }}>
      {result && (
        <span style={{
          fontSize:13, fontWeight:500,
          color: result.ok ? "#059669" : "#dc2626",
          background: result.ok ? "#ecfdf5" : "#fef2f2",
          padding:"5px 10px", borderRadius:8,
          border: `1px solid ${result.ok ? "#a7f3d0" : "#fecaca"}`
        }}>
          {result.msg}
        </span>
      )}
      <button style={{ ...S.btn, opacity: syncing ? 0.8 : 1 }}
        onClick={handleSync} disabled={syncing}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"
          style={{ animation: syncing ? "spin 0.8s linear infinite" : "none" }}>
          <path d="M23 4v6h-6"/><path d="M1 20v-6h6"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
        </svg>
        {syncing ? "Syncing..." : "Sync Gmail"}
      </button>
      <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
    </div>
  );
}

const S = {
  btn: {
    display:"flex", alignItems:"center", gap:8,
    padding:"9px 18px", background:"#6366f1", color:"#fff",
    border:"none", borderRadius:10, fontSize:14, fontWeight:500,
    cursor:"pointer", boxShadow:"0 1px 8px rgba(99,102,241,0.35)"
  },
};

