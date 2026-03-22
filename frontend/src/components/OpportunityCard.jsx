import { useState } from "react";
import { ExternalLink, Trash2, Calendar, Clock } from "lucide-react";

const TYPE_COLORS = {
  INTERNSHIP:    { bg:"#dbeafe", text:"#1d4ed8", border:"#bfdbfe" },
  CERTIFICATION: { bg:"#ede9fe", text:"#6d28d9", border:"#ddd6fe" },
  HACKATHON:     { bg:"#fef3c7", text:"#b45309", border:"#fde68a" },
  OTHER:         { bg:"#f1f5f9", text:"#475569", border:"#e2e8f0" },
};

const STATUS_OPTS = ["PENDING","APPLIED","SAVED","REJECTED","EXPIRED"];

export default function OpportunityCard({ opportunity: o, onStatusChange, onDelete }) {
  const [deleting, setDeleting] = useState(false);

  const daysLeft = o.deadline
    ? Math.ceil((new Date(o.deadline) - new Date()) / 86400000)
    : null;
  const isExpired = daysLeft !== null && daysLeft < 0;
  const isUrgent  = daysLeft !== null && daysLeft >= 0 && daysLeft <= 3;

  const deadlineColor = isExpired ? "#ef4444" : isUrgent ? "#f97316" : "#64748b";
  const deadlineLabel = isExpired
    ? "Expired"
    : daysLeft === 0 ? "Due today!"
    : daysLeft === 1 ? "1 day left"
    : daysLeft !== null ? `${daysLeft} days left`
    : null;

  const tc = TYPE_COLORS[o.type] || TYPE_COLORS.OTHER;

  const handleDelete = async () => {
    setDeleting(true);
    await onDelete(o.id);
  };

  return (
    <div style={{ ...S.card, opacity: deleting ? 0.5 : 1 }}>
      <div style={S.cardTop}>
        <span style={{ ...S.typeBadge, background: tc.bg, color: tc.text, border: `1px solid ${tc.border}` }}>
          {o.type}
        </span>
        <button style={S.deleteBtn} onClick={handleDelete} title="Delete">
          <Trash2 size={13} />
        </button>
      </div>

      <h3 style={S.title} title={o.title}>{o.title}</h3>

      {deadlineLabel && (
        <div style={{ ...S.deadline, color: deadlineColor }}>
          {isUrgent || isExpired
            ? <Clock size={12} style={{ marginRight:4, flexShrink:0 }} />
            : <Calendar size={12} style={{ marginRight:4, flexShrink:0 }} />}
          <span>{deadlineLabel}</span>
          {o.deadline && (
            <span style={{ marginLeft:4, opacity:0.7 }}>
              · {new Date(o.deadline).toLocaleDateString("en-IN", { day:"numeric", month:"short", year:"numeric" })}
            </span>
          )}
        </div>
      )}

      {isUrgent && !isExpired && (
        <div style={S.urgentBar} />
      )}

      <div style={S.cardBottom}>
        <select style={S.select} value={o.status}
          onChange={e => onStatusChange(o.id, e.target.value)}>
          {STATUS_OPTS.map(s => <option key={s} value={s}>{s}</option>)}
        </select>

        {o.applicationLink && (
          <a href={o.applicationLink} target="_blank" rel="noopener noreferrer"
            style={S.applyBtn}>
            Apply
            <ExternalLink size={11} style={{ marginLeft:4 }} />
          </a>
        )}
      </div>
    </div>
  );
}

const S = {
  card: {
    background:"#fff", borderRadius:10, padding:"14px",
    border:"1px solid #f1f5f9", transition:"box-shadow 0.15s, transform 0.15s",
    cursor:"default",
  },
  cardTop:   { display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:8 },
  typeBadge: { fontSize:10, fontWeight:700, padding:"3px 8px", borderRadius:20, letterSpacing:"0.04em" },
  deleteBtn: { background:"none", border:"none", cursor:"pointer", color:"#cbd5e1", padding:2, display:"flex", borderRadius:6, transition:"color 0.15s" },
  title:     { fontSize:13, fontWeight:500, color:"#1e293b", margin:"0 0 8px", lineHeight:1.5, overflow:"hidden", display:"-webkit-box", WebkitLineClamp:2, WebkitBoxOrient:"vertical" },
  deadline:  { display:"flex", alignItems:"center", fontSize:11, fontWeight:500, marginBottom:10 },
  urgentBar: { height:2, background:"linear-gradient(90deg,#f97316,#ef4444)", borderRadius:2, marginBottom:10 },
  cardBottom:{ display:"flex", justifyContent:"space-between", alignItems:"center", gap:8 },
  select:    { fontSize:11, padding:"5px 8px", borderRadius:6, border:"1px solid #e2e8f0", background:"#f8fafc", color:"#374151", cursor:"pointer", flex:1, fontWeight:500 },
  applyBtn:  { display:"flex", alignItems:"center", fontSize:11, fontWeight:600, color:"#fff", background:"#6366f1", padding:"5px 10px", borderRadius:6, textDecoration:"none", whiteSpace:"nowrap" },
};

