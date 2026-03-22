import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { opportunityApi } from "../services/api";
import OpportunityCard from "../components/OpportunityCard";
import SyncButton from "../components/SyncButton";

const COLUMNS = ["PENDING", "APPLIED", "SAVED", "REJECTED"];
const TYPE_COLORS = {
  INTERNSHIP:    { bg: "#e8f4fd", text: "#1a6fa8", dot: "#2196f3" },
  CERTIFICATION: { bg: "#f3e8fd", text: "#6a1ab5", dot: "#9c27b0" },
  HACKATHON:     { bg: "#fff8e1", text: "#b07d00", dot: "#ffc107" },
  OTHER:         { bg: "#f1f5f9", text: "#475569", dot: "#94a3b8" },
};
const COL_META = {
  PENDING:  { label: "Pending",  color: "#f59e0b", bg: "#fffbeb" },
  APPLIED:  { label: "Applied",  color: "#3b82f6", bg: "#eff6ff" },
  SAVED:    { label: "Saved",    color: "#8b5cf6", bg: "#f5f3ff" },
  REJECTED: { label: "Rejected", color: "#ef4444", bg: "#fef2f2" },
};

export default function Dashboard() {
  const navigate = useNavigate();
  const [opportunities, setOpportunities] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [search,   setSearch]   = useState("");
  const [typeFilter,   setTypeFilter]   = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [page, setPage] = useState(1);
  const PER_PAGE = 20;

  const load = async () => {
    setLoading(true); setError(null);
    try {
      const res = await opportunityApi.getAll();
      setOpportunities(res.data);
    } catch (e) {
      if (e.response?.status === 401) { sessionStorage.removeItem("jwt"); navigate("/login"); }
      else setError(e.response?.data?.message || "Failed to load");
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleStatus = async (id, s) => {
    await opportunityApi.updateStatus(id, s);
    setOpportunities(p => p.map(o => o.id === id ? { ...o, status: s } : o));
  };

  const handleDelete = async (id) => {
    await opportunityApi.delete(id);
    setOpportunities(p => p.filter(o => o.id !== id));
  };

  const filtered = opportunities.filter(o =>
    (!search || o.title.toLowerCase().includes(search.toLowerCase())) &&
    (!typeFilter || o.type === typeFilter) &&
    (!statusFilter || o.status === statusFilter)
  );

  const paginated = filtered.slice(0, page * PER_PAGE);
  const hasMore = filtered.length > page * PER_PAGE;

  const stats = {
    total:   opportunities.length,
    pending: opportunities.filter(o => o.status === "PENDING").length,
    applied: opportunities.filter(o => o.status === "APPLIED").length,
    expiring: opportunities.filter(o => {
      if (!o.deadline || o.status !== "PENDING") return false;
      return Math.ceil((new Date(o.deadline) - new Date()) / 86400000) <= 7;
    }).length,
  };

  const groups = {};
  COLUMNS.forEach(s => { groups[s] = paginated.filter(o => o.status === s); });

  return (
    <div style={S.page}>
      {/* Header */}
      <header style={S.header}>
        <div style={S.headerLeft}>
          <div style={S.logo}>
            <span style={S.logoIcon}>⚡</span>
            <span style={S.logoText}>Smart App Tracker</span>
          </div>
          <p style={S.tagline}>Your opportunities, organized.</p>
        </div>
        <div style={S.headerRight}>
          <SyncButton onSynced={load} />
          <button style={S.logoutBtn} onClick={() => { sessionStorage.removeItem("jwt"); navigate("/login"); }}>
            Sign out
          </button>
        </div>
      </header>

      {/* Stats */}
      <div style={S.statsRow}>
        {[
          { label: "Total tracked", value: stats.total,   accent: "#6366f1" },
          { label: "Pending",       value: stats.pending,  accent: "#f59e0b" },
          { label: "Applied",       value: stats.applied,  accent: "#3b82f6" },
          { label: "Expiring soon", value: stats.expiring, accent: "#ef4444" },
        ].map(s => (
          <div key={s.label} style={{ ...S.statCard, borderTop: `3px solid ${s.accent}` }}>
            <div style={{ ...S.statVal, color: s.accent }}>{s.value}</div>
            <div style={S.statLabel}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={S.filterRow}>
        <div style={S.searchWrap}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2" style={{ position:"absolute", left:12, top:"50%", transform:"translateY(-50%)" }}>
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input style={S.searchInput} placeholder="Search opportunities..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} />
        </div>
        <select style={S.select} value={typeFilter}
          onChange={e => { setTypeFilter(e.target.value); setPage(1); }}>
          <option value="">All types</option>
          <option value="INTERNSHIP">Internship</option>
          <option value="CERTIFICATION">Certification</option>
          <option value="HACKATHON">Hackathon</option>
          <option value="OTHER">Other</option>
        </select>
        <select style={S.select} value={statusFilter}
          onChange={e => { setStatusFilter(e.target.value); setPage(1); }}>
          <option value="">All statuses</option>
          {COLUMNS.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        {(search || typeFilter || statusFilter) && (
          <button style={S.clearBtn} onClick={() => { setSearch(""); setTypeFilter(""); setStatusFilter(""); setPage(1); }}>
            Clear filters
          </button>
        )}
        <span style={S.resultCount}>{filtered.length} result{filtered.length !== 1 ? "s" : ""}</span>
      </div>

      {/* Content */}
      {loading && (
        <div style={S.center}>
          <div style={S.spinner} />
          <p style={{ color:"#94a3b8", marginTop:16 }}>Loading your opportunities...</p>
        </div>
      )}

      {!loading && error && (
        <div style={S.errorBox}>
          <span>{error}</span>
          <button style={S.retryBtn} onClick={load}>Retry</button>
        </div>
      )}

      {!loading && !error && filtered.length === 0 && (
        <div style={S.emptyState}>
          <div style={S.emptyIcon}>📭</div>
          <h3 style={S.emptyTitle}>
            {opportunities.length === 0 ? "No opportunities yet" : "No results found"}
          </h3>
          <p style={S.emptyText}>
            {opportunities.length === 0
              ? "Click Sync Gmail to fetch internships and certifications from your inbox."
              : "Try adjusting your search or filters."}
          </p>
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <>
          <div style={S.board}>
            {COLUMNS.map(status => (
              <div key={status} style={S.column}>
                <div style={{ ...S.colHeader, borderBottom: `2px solid ${COL_META[status].color}` }}>
                  <div style={S.colLeft}>
                    <div style={{ ...S.colDot, background: COL_META[status].color }} />
                    <span style={S.colTitle}>{COL_META[status].label}</span>
                  </div>
                  <span style={{ ...S.colBadge, background: COL_META[status].bg, color: COL_META[status].color }}>
                    {groups[status].length}
                  </span>
                </div>
                <div style={S.cardList}>
                  {groups[status].length === 0 && (
                    <div style={S.emptyCol}>No items</div>
                  )}
                  {groups[status].map(opp => (
                    <OpportunityCard key={opp.id} opportunity={opp}
                      onStatusChange={handleStatus} onDelete={handleDelete} />
                  ))}
                </div>
              </div>
            ))}
          </div>

          {hasMore && (
            <div style={{ textAlign:"center", marginTop:24 }}>
              <button style={S.loadMore} onClick={() => setPage(p => p + 1)}>
                Load more ({filtered.length - page * PER_PAGE} remaining)
              </button>
            </div>
          )}
        </>
      )}
      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes fadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:none; } }
        * { box-sizing: border-box; }
        body { margin: 0; background: #f8fafc; font-family: 'DM Sans', -apple-system, sans-serif; }
        input:focus, select:focus { outline: none; border-color: #6366f1 !important; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap');
      `}</style>
    </div>
  );
}

const S = {
  page:       { maxWidth:1440, margin:"0 auto", padding:"0 24px 48px", minHeight:"100vh" },
  header:     { display:"flex", justifyContent:"space-between", alignItems:"center", padding:"20px 0 24px", borderBottom:"1px solid #e2e8f0", marginBottom:24 },
  headerLeft: { display:"flex", flexDirection:"column", gap:4 },
  logo:       { display:"flex", alignItems:"center", gap:10 },
  logoIcon:   { fontSize:22 },
  logoText:   { fontSize:22, fontWeight:700, fontFamily:"'Space Grotesk', sans-serif", color:"#0f172a", letterSpacing:"-0.5px" },
  tagline:    { margin:0, fontSize:13, color:"#94a3b8", paddingLeft:32 },
  headerRight:{ display:"flex", alignItems:"center", gap:12 },
  logoutBtn:  { padding:"8px 16px", border:"1px solid #e2e8f0", borderRadius:8, background:"#fff", fontSize:13, color:"#64748b", cursor:"pointer", fontWeight:500 },
  statsRow:   { display:"grid", gridTemplateColumns:"repeat(4, 1fr)", gap:16, marginBottom:24 },
  statCard:   { background:"#fff", borderRadius:12, padding:"20px 24px", boxShadow:"0 1px 3px rgba(0,0,0,0.06)" },
  statVal:    { fontSize:32, fontWeight:700, fontFamily:"'Space Grotesk', sans-serif", lineHeight:1 },
  statLabel:  { fontSize:13, color:"#94a3b8", marginTop:6, fontWeight:500 },
  filterRow:  { display:"flex", gap:10, alignItems:"center", marginBottom:24, flexWrap:"wrap" },
  searchWrap: { position:"relative", flex:1, minWidth:220 },
  searchInput:{ width:"100%", padding:"10px 12px 10px 38px", border:"1px solid #e2e8f0", borderRadius:10, fontSize:14, background:"#fff", color:"#0f172a" },
  select:     { padding:"10px 14px", border:"1px solid #e2e8f0", borderRadius:10, fontSize:14, background:"#fff", color:"#374151", cursor:"pointer" },
  clearBtn:   { padding:"10px 14px", border:"1px solid #fecaca", borderRadius:10, fontSize:13, background:"#fef2f2", color:"#ef4444", cursor:"pointer", fontWeight:500 },
  resultCount:{ fontSize:13, color:"#94a3b8", marginLeft:"auto", whiteSpace:"nowrap" },
  board:      { display:"grid", gridTemplateColumns:"repeat(4, minmax(0,1fr))", gap:16 },
  column:     { background:"#fff", borderRadius:14, padding:16, boxShadow:"0 1px 3px rgba(0,0,0,0.06)", minHeight:300, animation:"fadeIn 0.3s ease" },
  colHeader:  { display:"flex", justifyContent:"space-between", alignItems:"center", paddingBottom:12, marginBottom:12 },
  colLeft:    { display:"flex", alignItems:"center", gap:8 },
  colDot:     { width:8, height:8, borderRadius:"50%" },
  colTitle:   { fontSize:13, fontWeight:600, color:"#374151", textTransform:"uppercase", letterSpacing:"0.05em" },
  colBadge:   { fontSize:12, fontWeight:600, padding:"2px 8px", borderRadius:20 },
  cardList:   { display:"flex", flexDirection:"column", gap:10 },
  emptyCol:   { fontSize:12, color:"#cbd5e1", textAlign:"center", padding:"24px 0" },
  center:     { display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", minHeight:300 },
  spinner:    { width:32, height:32, border:"3px solid #e2e8f0", borderTopColor:"#6366f1", borderRadius:"50%", animation:"spin 0.8s linear infinite" },
  errorBox:   { background:"#fef2f2", border:"1px solid #fecaca", borderRadius:10, padding:"16px 20px", display:"flex", justifyContent:"space-between", alignItems:"center" },
  retryBtn:   { background:"#ef4444", color:"#fff", border:"none", borderRadius:8, padding:"8px 16px", cursor:"pointer", fontSize:13, fontWeight:500 },
  emptyState: { textAlign:"center", padding:"80px 20px" },
  emptyIcon:  { fontSize:48, marginBottom:16 },
  emptyTitle: { fontSize:20, fontWeight:600, color:"#374151", margin:"0 0 8px", fontFamily:"'Space Grotesk', sans-serif" },
  emptyText:  { fontSize:14, color:"#94a3b8", margin:0 },
  loadMore:   { padding:"12px 28px", background:"#6366f1", color:"#fff", border:"none", borderRadius:10, fontSize:14, fontWeight:500, cursor:"pointer" },
};

